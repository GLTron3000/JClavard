package client;

import gui.ClientGUI;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Listener implements Runnable{
    Socket socket;
    ClientGUI gui;

    public Listener(Socket socket, ClientGUI gui) {
        this.socket = socket;
        this.gui = gui;
    }
    
    @Override
    public void run() {
        try {
            byte[] buffer = new byte[9000];
            int read;
            InputStream inputStream = socket.getInputStream();
            while((read = inputStream.read(buffer)) != -1){    
                String message = new String(buffer, 0, read);
                System.out.println(message.trim());
                
                try {
                    gui.semaphore.acquire();
                    gui.addMessage(message.split(" ")[1], message.split(" ")[2]);
                    gui.semaphore.release();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (SocketException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
        
