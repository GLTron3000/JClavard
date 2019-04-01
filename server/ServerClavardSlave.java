package server;

import jclavard.ClavardAMUUtils;
import jclavard.ConnectSyntaxException;
import jclavard.MessageSyntaxException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerClavardSlave extends ServerClavardMaster{
    private SocketChannel masterSocket;
    private final InetSocketAddress masterAddress;

    public ServerClavardSlave(int port) {
        super(port);
        this.masterAddress = null;
    }

    public ServerClavardSlave(int port, InetSocketAddress masterAddress) {
        super(port);
        this.masterAddress = masterAddress;
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


        System.out.println("Connecting to master server...");
        connectToMaster(selector);

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

    @Override
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

    @Override
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
                if(cc.pseudo.equals("serveur") || cc.pseudo.equals("masterServer")){
                    String splitedMessage[] = unconfirmedMessage.split(" ",3);
                    finalMessage = splitedMessage[1]+" "+splitedMessage[2];
                    System.out.println("From server | "+finalMessage);
                    broadcastMessageFromMaster(finalMessage);
                }else{
                    finalMessage = message;
                    System.out.println(cc.pseudo+"> "+message);
                    sendMessageToMaster(message, cc.pseudo);
                    broadcastMessage(finalMessage, (ChatClient) key.attachment());
                }
                
                setAllWrite(selector);
            }
        }catch (IOException e) {
            System.err.println("Client communication error");
        } catch (MessageSyntaxException ex) {
            System.err.println("Erreur protocole MSG");
            sendMessage(client, ex.errorMessage());
        }
    }

    private void sendMessageToMaster(String message, String pseudo){
        String finalMessage = "MSG "+pseudo+" "+message;
        sendMessage(masterSocket, finalMessage);
    }
    
    private void connectToMaster(Selector selector){
        try {
            masterSocket = SocketChannel.open(new InetSocketAddress("localhost",12345));
            masterSocket.configureBlocking(false);
            sendMessage(masterSocket, "SERVERCONNECT");

            masterSocket.register(selector, SelectionKey.OP_READ, new ChatClient(masterSocket, "masterServer"));
            System.out.println("Connected to master server !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void broadcastMessageFromMaster(String message){
        clients.forEach( client ->{
            if(!client.socket.equals(masterSocket)){
                client.queue.add(message);
            }
        });
    }
}
