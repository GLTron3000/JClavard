package jclavard;

import gui.WelcomeGUI;
import server.ServerRun;


public class JClavard{
    
    public static void main(String[] args) {
        if(args.length != 0){
            String help = "JClavard \n -h : cette aide\n -sfr 12345 : serveur fédéré robuste sans gui";
            if(args.length != 2){
                System.out.println(help);
                return;
            }
            switch(args[0]){
                case "-h" : System.out.println(help); break;
                case "-s" : 
                    Thread serverThread = new Thread(new ServerRun(Integer.parseInt(args[1]), 2));
                    serverThread.start();
                    break;
                default : System.err.println(help);
            }   
        }else{
            WelcomeGUI gui = new WelcomeGUI();
            gui.launchGUI();
        }
    }
    
}
