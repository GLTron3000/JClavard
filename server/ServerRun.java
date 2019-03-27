package server;


public class ServerRun implements Runnable{
    private ServerClavarde server;

    public ServerRun(int port) {
        server = new ServerClavarde(port);
    }

    @Override
    public void run() {
        server.start();
    }
    
    public void kickAll(){
        server.kickAll();
    }
}
