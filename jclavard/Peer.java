package jclavard;

public class Peer {
    public boolean isMaster;
    public String adress;
    public int port;

    public Peer(String adress, int port) {
        this.adress = adress;
        this.port = port;
        this.isMaster = false;
    }

    public void setMaster(){
        isMaster = true;
    }
}
