package client;

import gui.ClientGUI;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender implements Runnable{
    Socket socket;

    public Sender(Socket socket, ClientGUI gui) {
        this.socket = socket;
    }
    
    public void connectToServer(String pseudo){
        send("CONNECT "+pseudo);
    }

    public void messageToServer(String message){
        send("MSG "+message);
    }
    
    private void send(String message) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    @Override
    public void run() {
        try {
            while(socket.getKeepAlive()){
                
            }
        } catch (SocketException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
