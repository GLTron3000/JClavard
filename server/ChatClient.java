package server;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

public class ChatClient {
    SocketChannel socket;
    ArrayBlockingQueue queue;
    String pseudo;
    boolean accepted;

    public ChatClient(ArrayBlockingQueue queue, String pseudo) {
        this.queue = queue;
        this.pseudo = pseudo;
        this.accepted = false;
    }

    public ChatClient(SocketChannel socket) {
        this.socket = socket;
        this.queue = null;
        this.pseudo = null;
        this.accepted = false;
    }
    
    public void accept(String pseudo){
        queue = new ArrayBlockingQueue(1024);
        this.pseudo = pseudo;
        accepted = true;
    }

    
}
