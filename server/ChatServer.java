package server;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

public class ChatServer {
    SocketChannel socket;
    int broadcast;
    ArrayBlockingQueue queue;

    public ChatServer(SocketChannel socket) {
        this.socket = socket;
        this.broadcast = 0;
        this.queue = new ArrayBlockingQueue(1024);
    }
    
    
}
