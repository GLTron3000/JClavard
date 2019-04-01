package server;

import jclavard.ClavardAMUUtils;
import jclavard.Peer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ServerRun implements Runnable{
    private ServerClavarde server;

    public ServerRun(int port) {
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

    @Override
    public void run() {
        server.start();
    }
}
