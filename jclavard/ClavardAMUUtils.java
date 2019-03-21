package jclavard;

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
}
