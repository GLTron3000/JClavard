package server;


import jclavard.ClavardAMUUtils;
import jclavard.Peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
                    InetSocketAddress localAddr = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), port);
                    if(masterAddr.equals(localAddr)){
                        server = new ServerClavardMaster(port);
                    }else {
                        server = new ServerClavardSlave(port);
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
