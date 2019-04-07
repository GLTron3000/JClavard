package server;

import jclavard.ClavardAMUUtils;
import jclavard.Peer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ServerRun implements Runnable{
    private ServerClavarde server;
    
    public ServerRun(int port, int type) {
        switch(type){
            case 0 : initSimple(port); break;
            case 1 : initFederated(port); break;
            case 2 : initDecentral(port); break;
        }
    }
    
    private void initFederated(int port){
        ArrayList<Peer> peers = ClavardAMUUtils.readConfig();
        Peer master;
        for(Peer p: peers ){
            if(p.isMaster){
                master=p;

                try {
                    InetSocketAddress masterAddr = new InetSocketAddress(master.adress,master.port);
                    InetSocketAddress localAddr = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), port);
                    System.out.println(masterAddr);
                    System.out.println(localAddr);
                    if(masterAddr.equals(localAddr)){
                        server = new ServerClavardMaster(port);
                    }else {
                        server = new ServerClavardSlave(port, masterAddr);
                    }
                    return;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void initSimple(int port){
        server = new ServerClavardSimple(port);
    }

    private void initDecentral(int port){
        server = new ServerClavardDecentral(port);
    }
    
    @Override
    public void run() {
        server.start();
    }
}
