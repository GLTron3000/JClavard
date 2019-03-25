package client;

import gui.ClientGUI;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientClavarde {
    Socket socket;
    int port;
    String adress;
    Sender sender;
    Listener listener;
    Thread listenThread;

    public ClientClavarde(int port, String adress, ClientGUI gui) throws IOException{
        this.port = port;
        this.adress = adress;
        
        socket = new Socket();
        socket.connect(new InetSocketAddress(adress, port));
        
        
        sender = new Sender(socket, gui);
        //sender.run();
        
        listener = new Listener(socket, gui);
        listenThread = new Thread(listener);
        
        sender.connectToServer(gui.pseudo);
    }
    
    public void sendMessage(String message) throws IOException{
        sender.messageToServer(message);
    }
    
    public void startListen(){
        listenThread.start();
    }
    
    
}
