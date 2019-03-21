package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import jclavard.ClavardAMUUtils;
import jclavard.ConnectSyntaxException;


public class Acceptor implements Runnable{
    Selector selector;
    ServerSocketChannel serverSocketChannel;
    ArrayList<ChatClient> clients;

    public Acceptor(Selector selector, ServerSocketChannel serverSocketChannel, ArrayList<ChatClient> clients) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.clients = clients;
    }
    
   
    @Override
    public void run() {
        SocketChannel client = null;
        try {
            System.out.println("Client connected THREAD");
            client = serverSocketChannel.accept();
            
            
            ByteBuffer buffer = ByteBuffer.allocate(256);
            client.read(buffer);
            String message = new String(buffer.array()).trim();
            String pseudo;
            pseudo = ClavardAMUUtils.checkConnectionSyntaxe(message);
            
            ArrayBlockingQueue queue = new ArrayBlockingQueue(1024);
            
            ChatClient chatClient = new ChatClient(queue, pseudo);
            clients.add(chatClient);
            //queues.add(queue);
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ, chatClient);
            
            
        } catch (IOException ex) {
            System.err.println("Erreur connection client");
            ex.printStackTrace();
        } catch (ConnectSyntaxException ex) {
            System.err.println("Erreur protocole");
            try {
                ByteBuffer buffer = ByteBuffer.wrap(ex.errorMessage().getBytes()); 
                client.write(buffer);
                client.close();
            } catch (IOException exp) {
                System.err.println("Erreur envoi");
                exp.printStackTrace();
            }
        }
    }
    
}
