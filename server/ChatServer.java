package server;

import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public class ChatServer {
    SocketChannel socket;
    int broadcast;
    ArrayBlockingQueue queue;
    int ogPort;

    public ChatServer(SocketChannel socket, int ogPort) {
        this.socket = socket;
        this.broadcast = 0;
        this.queue = new ArrayBlockingQueue(1024);
        this.ogPort = ogPort;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.socket);
        return hash;
    }
    
}
