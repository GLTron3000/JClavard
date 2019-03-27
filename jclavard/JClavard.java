package jclavard;

import gui.WelcomeGUI;
import server.ServerClavarde;


public class JClavard{
    
    public static void main(String[] args) {
        if(args.length != 0){
            String help = "JClavard \n -h : cette aide\n -s : serveur sans gui";
            switch(args[0]){
                case "-h" : System.out.println(help); break;
                case "-s" : 

                    break;
                default : System.err.println(help);
            }   
        }else{
            WelcomeGUI gui = new WelcomeGUI();
            gui.launchGUI();
        }
    }
    
}
