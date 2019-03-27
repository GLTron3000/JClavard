package server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jclavard.ClavardAMUUtils;
import jclavard.ConnectSyntaxException;
import jclavard.MessageSyntaxException;
import jclavard.Peer;

public class ServerClavarde {
    ServerSocketChannel serverSocketChannel;
    //private final Executor executor;
    private ArrayList<ChatClient> clients;
    private ArrayList<Socket> servers;
    private Selector selector;
    private int port;

    public ServerClavarde(int port) {
        this.port = port;
        //executor = Executors.newWorkStealingPool();
        clients = new ArrayList<>();
        servers = new ArrayList<>();
    }

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


        System.out.println("Connecting to peers...");
        connectToServers();

        System.out.println("Waiting for client...");
        while(true){
            try {
                selector.select();
            } catch (IOException ex) {
                System.err.println("Erreur selector");
            }
            Set<SelectionKey> selectedKeys;
            selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if(!key.isValid()){
                    continue;
                }

                if (key.isAcceptable()) {
                    acceptable(selector);
                }else if (key.isReadable()) {
                    readable(key, selector, selectedKeys);
                }else if(key.isWritable()){
                    writable(key, selector, selectedKeys);
                }

                iterator.remove();
            }
        }
       
    }

    public void broadcastMessage(String message, ChatClient currentClient){
        clients.forEach( client ->{
            if(!client.equals(currentClient)) client.queue.add(currentClient.pseudo+"> "+message); 
        });
    }

    public void setAllWrite(Selector selector){
        clients.forEach(client ->{
            try {
                client.socket.register(selector, SelectionKey.OP_WRITE, client);
            } catch (ClosedChannelException ex) {
                Logger.getLogger(ServerClavarde.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private void acceptable(Selector selector){
        SocketChannel client;
        try {
            System.out.println("Client connected");
            client = serverSocketChannel.accept();
            client.configureBlocking(false);
            
            ChatClient chatClient = new ChatClient(client);

            client.register(selector, SelectionKey.OP_READ, chatClient);  
        } catch (IOException ex) {
            System.err.println("Erreur connection client");
            ex.printStackTrace();
        }
    }
    
    private void readable(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys){
        SocketChannel client = (SocketChannel) key.channel();
        ChatClient chatClient = (ChatClient) key.attachment();
        
        if(chatClient.accepted){
            readAndBroadcast(key, selector, selectedKeys);
        }else{
            tryToAccept(client, chatClient);
        }
        
    }
    
    private void tryToAccept(SocketChannel client, ChatClient chatClient){
        try {
            
            ByteBuffer buffer = ByteBuffer.allocate(256);
            client.read(buffer);
            String message = new String(buffer.array()).trim();

            if(ClavardAMUUtils.isServerConnect(message)){
                chatClient.acceptServer();
                System.out.println("Server registered");
            }else{
                String pseudo;
                pseudo = ClavardAMUUtils.checkConnectionSyntaxe(message);

                chatClient.accept(pseudo);

                clients.add(chatClient);
                System.out.println("Client accepted "+pseudo);
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
    
    private void readAndBroadcast(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys){
        SocketChannel client = (SocketChannel) key.channel();
        try{
            ByteBuffer buffer = ByteBuffer.allocate(256);
            if(client.read(buffer)==-1){
                ChatClient cc = (ChatClient)key.attachment();
                client.close();
                clients.remove(cc);  
                System.out.println(cc.pseudo+" disconnected");
                return;
            }
            
            
            String unconfirmedMessage = new String(buffer.array()).trim();
            ChatClient cc = (ChatClient)key.attachment();
            
            System.out.println("Message from "+cc.pseudo+" : "+unconfirmedMessage);  
            
            if(!unconfirmedMessage.equals("")){
                String message = ClavardAMUUtils.checkMessageSyntaxe(unconfirmedMessage);
                System.out.println(cc.pseudo+"> "+message);
                broadcastMessage(message, (ChatClient) key.attachment());
                setAllWrite(selector);
            }
        }catch (IOException e) {
            System.err.println("Client communication error");
        } catch (MessageSyntaxException ex) {
            System.err.println("Erreur protocole MSG");
            sendMessage(client, ex.errorMessage());
        }
    }
    
    private void writable(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys){
        SocketChannel client = (SocketChannel) key.channel();
        ChatClient chatClient = (ChatClient) key.attachment();
        try{
            if(chatClient.queue.isEmpty()){
                client.register(selector, SelectionKey.OP_READ, key.attachment());
                return;
            }

            String message = (String) chatClient.queue.poll();
            sendMessage(client, message);
        }catch (IOException e) {
            System.err.println("Client communication error");
        }
    }
    
    private void sendMessage(SocketChannel client, String message){
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes()); 
            client.write(buffer);
        } catch (IOException ex) {
            System.err.println("Erreur envoi message");
            ex.printStackTrace();
        }
    }
    
    public void kickAll(){
        clients.forEach(client ->{
            try {
                client.socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerClavarde.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        clients.clear();
    }

    public void connectToServers(){
        ArrayList<Peer> peers = ClavardAMUUtils.readConfig();

        peers.forEach(peer -> {
            try {
                System.out.println(peer.adress+" "+peer.port+" "+serverSocketChannel.getLocalAddress());
                InetSocketAddress targetAdress = new InetSocketAddress(peer.adress, peer.port);
                if(targetAdress.equals(serverSocketChannel.getLocalAddress())){
                    System.out.println("UI");
                    return;
                }

                Socket socket = new Socket();

                socket.connect(targetAdress);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("SERVERCONNECT");
                System.out.println("CONNECT TO SERV");
                servers.add(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
