package server;

import java.net.InetSocketAddress;
import java.util.Map;

class WaitingMessage {
    String message;
    Map<InetSocketAddress,Integer> vector;
    ChatServer server;

    public WaitingMessage(String message, Map<InetSocketAddress, Integer> vector, ChatServer server) {
        this.message = message;
        this.vector = vector;
        this.server = server;
    }
    
    
}
