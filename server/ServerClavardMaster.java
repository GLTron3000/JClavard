package server;

import jclavard.ClavardAMUUtils;
import jclavard.ConnectSyntaxException;
import jclavard.MessageSyntaxException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerClavardMaster implements ServerClavarde{
    ServerSocketChannel serverSocketChannel;
    ArrayList<ChatClient> clients;
    Selector selector;
    final int port;

    public ServerClavardMaster(int port) {
        this.port = port;
        clients = new ArrayList<>();
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


        System.out.println("Starting Master Server...");

        System.out.println("Waiting for client and servers...");
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

    void broadcastMessage(String message, ChatClient currentClient){
        clients.forEach( client ->{
            if(currentClient.server) client.queue.add(message.split(" ",2)[0]+"> "+message.split(" ",2)[1]);
            else client.queue.add(currentClient.pseudo+"> "+message);
        });
    }

    void setAllWrite(Selector selector){
        clients.forEach(client ->{
            try {
                client.socket.register(selector, SelectionKey.OP_WRITE, client);
            } catch (ClosedChannelException ex) {
                Logger.getLogger(ServerClavarde.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    void acceptable(Selector selector){
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

    void readable(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys){
        SocketChannel client = (SocketChannel) key.channel();
        ChatClient chatClient = (ChatClient) key.attachment();

        if(chatClient.accepted){
            readAndBroadcast(key, selector, selectedKeys);
        }else{
            tryToAccept(client, chatClient);
        }

    }

    void tryToAccept(SocketChannel client, ChatClient chatClient){
        try {

            ByteBuffer buffer = ByteBuffer.allocate(256);
            client.read(buffer);
            String message = new String(buffer.array()).trim();

            if(ClavardAMUUtils.isServerConnect(message)){
                chatClient.acceptServer();
                chatClient.pseudo = "serveur";
                clients.add(chatClient);
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

    void readAndBroadcast(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys){
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
                String finalMessage;
                if(cc.pseudo.equals("serveur")){
                    String splitedMessage[] = unconfirmedMessage.split(" ",3);
                    finalMessage = splitedMessage[1]+" "+splitedMessage[2];
                    System.out.println("From server | "+finalMessage);
                }else{
                    finalMessage = message;
                    System.out.println(cc.pseudo+"> "+message);
                }
                broadcastMessage(finalMessage, (ChatClient) key.attachment());
                setAllWrite(selector);
            }
        }catch (IOException e) {
            System.err.println("Client communication error");
        } catch (MessageSyntaxException ex) {
            System.err.println("Erreur protocole MSG");
            sendMessage(client, ex.errorMessage());
            System.exit(1);
        }
    }

    void writable(SelectionKey key, Selector selector, Set<SelectionKey> selectedKeys){
        SocketChannel client = (SocketChannel) key.channel();
        ChatClient chatClient = (ChatClient) key.attachment();
        try{
            if(chatClient.queue.isEmpty()){
                client.register(selector, SelectionKey.OP_READ, key.attachment());
                return;
            }

            String message = (String) chatClient.queue.poll();
            ClavardAMUUtils.sendMessageClavardamu(client, message);
        }catch (IOException e) {
            System.err.println("Client communication error");
        }
    }

    void sendMessage(SocketChannel client, String message){
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            client.write(buffer);
        } catch (IOException ex) {
            System.err.println("Erreur envoi message");
            ex.printStackTrace();
        }
    }

}
