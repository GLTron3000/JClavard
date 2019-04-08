package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.Pair;
import jclavard.ClavardAMUUtils;
import jclavard.ConnectSyntaxException;
import jclavard.MessageSyntaxException;
import jclavard.Peer;

public class ServerClavardDecentral implements ServerClavarde {

    ServerSocketChannel serverSocketChannel;
    ArrayList<ChatClient> clients;
    Selector selector;
    final int port;

    ArrayList<ChatServer> servers;
    ChatServer currentServer;
    ArrayList<WaitingMessage> waitList;

    public ServerClavardDecentral(int port) {
        this.port = port;
        clients = new ArrayList<>();
        servers = new ArrayList<>();
        waitList = new ArrayList<>();
    }

    @Override
    public void start() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("localhost", port));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.err.println("Erreur lancement serveur");
            e.printStackTrace();
        }

        System.out.println("Starting Decentralized server...");
        tryToConnectToServers();

        System.out.println("Waiting for client and servers...");
        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                System.err.println("Erreur selector");
            }
            Set<SelectionKey> selectedKeys;
            selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    acceptable(selector);
                } else if (key.isReadable()) {
                    readable(key, selector, selectedKeys);
                } else if (key.isWritable()) {
                    writable(key, selector, selectedKeys);
                }

                iterator.remove();
            }
        }

    }

    void acceptable(Selector selector) {
        SocketChannel client;
        try {
            System.out.println("Client or server connected");
            client = serverSocketChannel.accept();
            client.configureBlocking(false);

            ChatClient chatClient = new ChatClient(client);

            client.register(selector, SelectionKey.OP_READ, chatClient);
        } catch (IOException ex) {
            System.err.println("Erreur connection client");
            ex.printStackTrace();
        }
    }

    void readable(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys) {
        SocketChannel client = (SocketChannel) key.channel();
        
        if (key.attachment() instanceof ChatServer) {
            serverRead(client, (ChatServer) key.attachment());
        } else if (key.attachment() instanceof ChatClient) {
            ChatClient chatClient = (ChatClient) key.attachment();
            if (chatClient.accepted) {
                clientRead(key, selector, selectedKeys);
            } else {
                tryToAccept(client, chatClient);
            }
        }
    }
    
    void writable(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys) {
        SocketChannel client = (SocketChannel) key.channel();
        Object attachement = key.attachment();

        ArrayBlockingQueue queue;
        if (attachement instanceof ChatClient) {
            ChatClient cc = (ChatClient) attachement;
            queue = cc.queue;
        } else if (attachement instanceof ChatServer) {
            ChatServer cs = (ChatServer) attachement;
            queue = cs.queue;
        } else {
            return;
        }

        try {
            if (queue.isEmpty()) {
                client.register(selector, SelectionKey.OP_READ, key.attachment());
                return;
            }

            String message = (String) queue.poll();
            /*
            * SIMULATEUR DE PANNE ALEATOIRE
            try {
                Random rand = new Random();
                if(port == 12345) TimeUnit.SECONDS.sleep(rand.nextInt(15));
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerClavardDecentral.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            ClavardAMUUtils.sendMessageClavardamu(client, message);
            client.register(selector, SelectionKey.OP_WRITE, key.attachment());
        } catch (IOException e) {
            System.err.println("Communication error");
        }
    }

    void tryToAccept(SocketChannel client, ChatClient chatClient) {
        try {

            ByteBuffer buffer = ByteBuffer.allocate(256);
            client.read(buffer);
            String message = new String(buffer.array()).trim();

            if (ClavardAMUUtils.isServerConnect(message)) {
                connectServer(client, Integer.parseInt(message.split(" ")[1]));
                System.out.println("[\u001B[43mSERVER\u001B[0m] accepted");
            } else {
                String pseudo;
                pseudo = ClavardAMUUtils.checkConnectionSyntaxe(message);

                chatClient.accept(pseudo);

                clients.add(chatClient);
                broadcastMessageToServers(pseudo+" a rejoint la discussion");
                System.out.println("[\u001B[44mCLIENT\u001B[0m] accepted " + pseudo);
                
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerClavarde.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectSyntaxException ex) {
            System.err.println("Erreur protocole CONNECTION");
            sendMessage(client, ex.errorMessage());
            try {
                client.close();
            } catch (IOException ex1) {
                System.err.println("Erreur fermeture");
            }
        }

    }

    void clientRead(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys) {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            if (client.read(buffer) == -1) {
                ChatClient cc = (ChatClient) key.attachment();
                client.close();
                clients.remove(cc);
                broadcastMessageToServers(cc.pseudo+" a quitter la discussion");
                System.out.println("[\u001B[33mWARNING\u001B[0m] Client "+cc.pseudo+" disconnected");
                return;
            }

            String unconfirmedMessage = new String(buffer.array()).trim();
            ChatClient cc = (ChatClient) key.attachment();

            System.out.println("[\u001B[44mCLIENT\u001B[0m] Message from " + cc.pseudo + " : " + unconfirmedMessage);

            if (!unconfirmedMessage.equals("")) {
                String message = ClavardAMUUtils.checkMessageSyntaxe(unconfirmedMessage);
                String finalMessage;
                finalMessage = cc.pseudo + "> " + message;
                broadcastMessageToServers(finalMessage);
                setAllWrite(selector);
            }
        } catch (IOException e) {
            System.err.println("Client communication error");
        } catch (MessageSyntaxException ex) {
            System.err.println("Erreur protocole MSG");
            sendMessage(client, ex.errorMessage());
            System.exit(1);
        }
    }

    void sendMessage(SocketChannel client, String message) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            client.write(buffer);
        } catch (IOException ex) {
            System.err.println("Erreur envoi message");
            ex.printStackTrace();
        }
    }

    void tryToConnectToServers() {
        System.out.println("---------------------[BEGIN CONFIGURATION]---------------------");
        ArrayList<Peer> peers = ClavardAMUUtils.readConfig();
        
        peers.forEach(peer -> {
            try {
                System.out.print("->"+peer.adress+":"+peer.port);
                SocketChannel socket = SocketChannel.open(new InetSocketAddress(peer.adress, peer.port));
                
                connectServer(socket, peer.port);

                int remotePort = socket.socket().getPort();
                SocketAddress remoteAdd = socket.getRemoteAddress();
                
                sendMessage(socket, "SERVERCONNECT "+port);
                
                if(socket.getRemoteAddress().equals(new InetSocketAddress("127.0.0.1", port))){
                    System.out.print(" [\u001B[33mCURRENT\u001B[0m]\n");
                    servers.remove(servers.get(servers.size()-1));
                }else{
                    System.out.print(" [\u001B[32mOK\u001B[0m]\n");
                }
                
                
            } catch (IOException ex) {
                System.out.println(" [\u001B[31mUNREACHABLE\u001B[0m]\n");
            }
        });
        
        System.out.println("---------------------[END CONFIGURATION]---------------------");
    }

    void connectServer(SocketChannel socket, int ogPort) {
        try {
            socket.configureBlocking(false);
            ChatServer cs = new ChatServer(socket, ogPort);
            if (new InetSocketAddress("127.0.0.1", ogPort).equals(new InetSocketAddress("127.0.0.1", port))) {
                currentServer = cs;
            }
            if(!servers.contains(cs)){
                servers.add(cs);
            }
            
            socket.register(selector, SelectionKey.OP_READ, cs);
        } catch (IOException ex) {
            Logger.getLogger(ServerClavardDecentral.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void serverRead(SocketChannel server, ChatServer cs) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            if (server.read(buffer) == -1) {
                cs.socket.close();
                servers.remove(cs);
                System.out.println("[\u001B[31mWARNING\u001B[0m] Server disconnected");
                return;
            }

            String unconfirmedMessage = new String(buffer.array()).trim();

            System.out.println("[\u001B[43mSERVER\u001B[0m] Message from " + cs.socket.getRemoteAddress() + " : " + unconfirmedMessage);
            
            if (!unconfirmedMessage.equals("")) {
                String message = ClavardAMUUtils.checkMessageSyntaxe(unconfirmedMessage);

                Pair<String, Map<InetSocketAddress, Integer>> pair = deserializeVector(message);
                if (new InetSocketAddress("127.0.0.1", cs.ogPort).equals(new InetSocketAddress("127.0.0.1", port))) {
                    System.out.println("CURRENT");
                    cs = currentServer;
                }   
                if (!isReady(pair.getValue())) {
                    System.out.println("[\u001B[31mWARNING\u001B[0m] Vector unsync, message put on hold");
                    waitList.add(new WaitingMessage(pair.getKey(), pair.getValue(), cs));
                } else {
                    cs.broadcast++; 
                    String finalMessage = pair.getKey();
                    broadcastMessageToClients(finalMessage);
                    checkForWaitingMessage();
                    setAllWrite(selector);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerClavardDecentral.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessageSyntaxException ex) {
            Logger.getLogger(ServerClavardDecentral.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    String serializeVector(String message) {
        String vector = "";

        for (ChatServer server : servers) {
            try {
                vector += "§" + (InetSocketAddress) server.socket.getRemoteAddress() +"|"+server.ogPort+ "|" + server.broadcast;
            } catch (IOException ex) {
                Logger.getLogger(ServerClavardDecentral.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return message + vector;
    }

    Pair<String, Map<InetSocketAddress, Integer>> deserializeVector(String message) {
        String[] split = message.split("§");

        Map<InetSocketAddress, Integer> vector = new HashMap<>();

        Pattern p = Pattern.compile("[0-9.]+", Pattern.MULTILINE);
        for (int i = 1; i < split.length; i++) {
            Matcher m = p.matcher(split[i]);

            String[] matchh = new String[20];
            for (int j = 0; m.find(); j++) {
                matchh[j] = m.group();
            }

            vector.put(new InetSocketAddress(matchh[0], Integer.parseInt(matchh[2])), Integer.parseInt(matchh[3]));
        }
        
        //System.out.println("[DESERIALIZE] Vector:"+vector+" Message:"+split[0]);
        return new Pair<>(split[0], vector);
    }
    
    boolean isReady(Map<InetSocketAddress, Integer> vector) {
        System.out.print("[IS READY] ");
        for (ChatServer server : servers) {
            int messageVector = vector.get(new InetSocketAddress("127.0.0.1", server.ogPort));
            int localVector = server.broadcast;

            if(localVector < messageVector){
                System.out.println("-> FALSE");
                return false;
            }
        }
        System.out.println("-> TRUE");
        return true;
    }

    private void checkForWaitingMessage() {
        System.out.println("[CHECK FOR WAITING MESSAGE] "+waitList.size());
        ArrayList<WaitingMessage> toRemove = new ArrayList();
        for(WaitingMessage message : waitList){
            if(isReady(message.vector)){
                broadcastMessageToClients(message.message);
                message.server.broadcast++;
                //waitList.remove(message);
                toRemove.add(message);
            }
        }
        
        waitList.removeAll(toRemove);
    }
      
    void broadcastMessageToClients(String message) {
        clients.forEach(client -> {
            client.queue.add(message);
        });
    }
    
    //CO_BROADCAST
    void broadcastMessageToServers(String message) {
        servers.forEach(server -> {
            if(!new InetSocketAddress("127.0.0.1", server.ogPort).equals(new InetSocketAddress("127.0.0.1", port))) server.queue.add(serializeVector(message));
        });
        setAllWrite(selector);
        currentServer.broadcast++;
        checkForWaitingMessage();
        setAllWrite(selector);
        broadcastMessageToClients(message);
    }
    
    void setAllWrite(Selector selector) {
        clients.forEach(client -> {
            try {
                client.socket.register(selector, SelectionKey.OP_WRITE, client);
            } catch (ClosedChannelException ex) {
                Logger.getLogger(ServerClavarde.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        servers.forEach(server -> {
            try {
                server.socket.register(selector, SelectionKey.OP_WRITE, server);
            } catch (ClosedChannelException ex) {
                Logger.getLogger(ServerClavardDecentral.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    void printLocalVector(){
        for(ChatServer server : servers){
            System.out.println(server.ogPort+" -> "+server.broadcast);
        }
    }
}
