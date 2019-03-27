package jclavard;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ClavardAMUUtils {
    public static String checkConnectionSyntaxe(String message) throws ConnectSyntaxException{
        String[] splited = message.split(" ");

        if(splited.length != 2) throw new ConnectSyntaxException();
        if(splited[0].equals("CONNECT")) return splited[1];
        throw new ConnectSyntaxException();
    }

    public static String checkMessageSyntaxe(String message) throws MessageSyntaxException {
        String[] splited = message.split(" ", 2);

        if(splited.length < 2) throw new MessageSyntaxException();
        if(splited[0].equals("MSG")) return splited[1];
        throw new MessageSyntaxException();
    }

    public static boolean isServerConnect(String message){
        return message.equals("SERVERCONNECT");
    }

    public static ArrayList<Peer> readConfig(){
        File file = new File("pairs.cfg");
        ArrayList<Peer> peers = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            String line;
            while(scanner.hasNextLine()){
                line = scanner.nextLine();
                System.out.println("");
                String[] splitedLine = line.split(" ");

                Peer peer = new Peer(splitedLine[2], Integer.parseInt(splitedLine[3]));

                if(splitedLine[0].equals("master")){
                    peer.setMaster();
                }

                peers.add(peer);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return peers;
    }
}
