package jclavard;

public class ConnectSyntaxException extends Exception {
    public String errorMessage(){
        return "ERROR CONNECT aborting clavardamu protocol";
    }
}
