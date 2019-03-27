package server;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

public class ChatClient {
    SocketChannel socket;
    ArrayBlockingQueue queue;
    String pseudo;
    boolean accepted;
    boolean server;

    public ChatClient(SocketChannel socket, String pseudo) {
        this.socket = socket;
        this.pseudo = pseudo;
        this.accepted = false;
        this.server = false;
        queue = new ArrayBlockingQueue(1024);
        accepted = true;
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

    public void acceptServer(){
        queue = new ArrayBlockingQueue(1024);
        server = true;
        accepted = true;
    }

    
}
