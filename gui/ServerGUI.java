package gui;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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
    private TextField textFieldPort;
    Thread serverThread;
    ToggleGroup group;
    int serverMode;
    
    private Button startBT(){
        Button button = new Button("Starto");
        
        button.setOnAction((ActionEvent event) -> {            
            String port = textFieldPort.getText();
            if(port.equals("")){
                return;
            }else{
                int portINT = Integer.parseInt(port);
                serverThread = new Thread(new ServerRun(portINT, serverMode));
            }
            serverThread.start();
            status.setText("Server running");

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
        
        Label mode = new Label("Type :");
        group = new ToggleGroup();
        RadioButton simpleRB = new RadioButton("Simple");
        simpleRB.setUserData("Simple");
        simpleRB.setToggleGroup(group);
        RadioButton federeRB = new RadioButton("Fédéré");
        federeRB.setUserData("Fédéré");
        federeRB.setToggleGroup(group);
        RadioButton federerRB = new RadioButton("Fédéré Robuste");
        federerRB.setUserData("Fédéré Robuste");
        federerRB.setToggleGroup(group);
        federerRB.setSelected(true);
        serverMode=2;
        
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
            if (group.getSelectedToggle() != null) {
                    switch(group.getSelectedToggle().getUserData().toString()){
                        case "Simple" : serverMode=0; break;
                        case "Fédéré" : serverMode=1; break;
                        case "Fédéré Robuste" : serverMode=2; break;
                        
                    }
                }    
        });

        
        Button startBT = startBT();
        Button stopBT = stopBT();
        Button quitBT = quitBT();

        textFieldPort = new TextField("12345");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(chat);
        scrollPane.setPadding(new Insets(10));
        
        VBox vboxBT = new VBox();
        vboxBT.getChildren().addAll(status, startBT, stopBT, quitBT);
        vboxBT.setSpacing(10);
        vboxBT.setPadding(new Insets(10));
        vboxBT.setAlignment(Pos.TOP_LEFT);
        
        VBox vboxSet = new VBox();
        vboxSet.getChildren().addAll(mode, simpleRB, federeRB, federerRB, textFieldPort);
        vboxSet.setSpacing(10);
        vboxSet.setPadding(new Insets(10));
        vboxSet.setAlignment(Pos.TOP_LEFT);
        
        
        borderPChat.setCenter(scrollPane);
        borderPChat.setRight(vboxSet);
        borderPChat.setLeft(vboxBT);
    }
    
    public ServerGUI() {
        initChat();

        scene_chat = new Scene(borderPChat, 500, 200);             
        


        stage = new Stage();
        stage.setTitle("JClavardAMU Server");
        stage.setScene(scene_chat);
    
    }
}
