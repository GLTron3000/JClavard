package gui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import server.ServerRun;


public class ServerGUI {
    Stage stage;
    private Scene scene_chat;
    private BorderPane borderPChat;
    private Text chat;
    private Label status;
    //ServerClavarde server;
    Thread serverThread;
    
    private Button startBT(){
        Button button = new Button("Starto");
        
        button.setOnAction((ActionEvent event) -> {
            status.setText("Server running");
            serverThread.start();
        });
        return button;
    }
    
    private Button stopBT(){
        Button button = new Button("Stop");
        
        button.setOnAction((ActionEvent event) -> {
            status.setText("Server stoped");
            serverThread.interrupt();
        });
        return button;
    }
    
    private Button quitBT(){
        Button button = new Button("Quit");
        
        button.setOnAction((ActionEvent event) -> {
            System.exit(0);
        });
        return button;
    }
     
    private void initChat(){
        borderPChat = new BorderPane();
        
        chat = new Text();
        status = new Label("Server stoped");
        
        Button startBT = startBT();
        Button stopBT = stopBT();
        Button quitBT = quitBT();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(chat);
        scrollPane.setPadding(new Insets(10));
        
        VBox vboxBT = new VBox();
        vboxBT.getChildren().addAll(status, startBT, stopBT, quitBT);
        vboxBT.setSpacing(10);
        vboxBT.setPadding(new Insets(10));
        vboxBT.setAlignment(Pos.TOP_CENTER);
        
        borderPChat.setCenter(scrollPane);
        borderPChat.setLeft(vboxBT);
    }
    
    public ServerGUI() {
        initChat();

        scene_chat = new Scene(borderPChat, 500, 500);             
        
        serverThread = new Thread(new ServerRun());

        stage = new Stage();
        stage.setTitle("JClavardAMU Server");
        stage.setScene(scene_chat);
    
    }
}
