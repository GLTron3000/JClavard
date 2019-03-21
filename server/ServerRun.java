package server;


public class ServerRun implements Runnable{
    private ServerClavarde server;

    public ServerRun() {
        server = new ServerClavarde(); 
    }

    @Override
    public void run() {
        server.start(12345);
    }
    
    public void kickAll(){
        server.kickAll();
    }
}
