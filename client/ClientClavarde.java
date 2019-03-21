package client;

import gui.ClientGUI;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientClavarde {
    Socket socket;
    int port;
    String adress;
    Sender sender;
    Listener listener;
    Thread listenThread;

    public ClientClavarde(int port, String adress, ClientGUI gui) {
        this.port = port;
        this.adress = adress;
        
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(adress, port));
        } catch (IOException ex) {
            Logger.getLogger(ClientClavarde.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sender = new Sender(socket, gui);
        //sender.run();
        
        listener = new Listener(socket, gui);
        listenThread = new Thread(listener);
        
        sender.connectToServer(gui.pseudo);
    }
    
    public void sendMessage(String message){
        sender.messageToServer(message);
    }
    
    public void startListen(){
        listenThread.start();
    }
    
    
}
