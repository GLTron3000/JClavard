package gui;

import client.ClientClavarde;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class ClientGUI {
    Stage stage;
    private Scene scene_pseudo;
    private Scene scene_chat;
    private Scene scene_error;
    private BorderPane borderPPseudo;
    private BorderPane borderPChat;
    private VBox vBoxError;
    private TextField textFieldPseudo;
    private TextField textFieldAdress;
    private TextField textFieldPort;
    private TextField textFieldChat;
    public String pseudo;
    private String adress;
    private int port;
    private Text chat;
    public Semaphore semaphore;
    
    private ClientClavarde client;
    
    public void addMessage(String message){
        String current_chat = chat.getText();
        chat.setText(current_chat.concat(message+"\n"));
    }
    
    private Button initBTSend() {
        Button button = new Button("Envoyer");
        
        button.setOnAction((ActionEvent event) -> {
            String message = textFieldChat.getText();
            textFieldChat.clear();
                        
            if(!message.isEmpty()){
                try {
                    client.sendMessage(message);
                } catch (IOException ex) {
                    System.err.println("Erreur connection serveur "+adress);
                    stage.setScene(scene_error);
                }

                try {
                    semaphore.acquire();
                    addMessage(pseudo+"> "+message);
                    semaphore.release();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        return button;
    }
    
    private Button initQuitBT(){
        Button button = new Button("Quit");
        
        button.setOnAction((ActionEvent event) -> {
            System.exit(0);
        });
        return button;
    }
     
    private void initChat(){
        borderPChat = new BorderPane();
        
        chat = new Text();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(chat);
        scrollPane.setPadding(new Insets(10));
        
        Button buttonSend = initBTSend();
        Button buttonQuit = initQuitBT();
        textFieldChat = new TextField();
        
        HBox hboxBottom = new HBox();
        hboxBottom.getChildren().addAll(textFieldChat, buttonSend, buttonQuit);
        hboxBottom.setAlignment(Pos.CENTER);
        hboxBottom.setSpacing(10);
        hboxBottom.setPadding(new Insets(5));
        
        
        borderPChat.setCenter(scrollPane);
        borderPChat.setBottom(hboxBottom);
    }
    
    private Button initBTValidPseudo() {
        Button button = new Button("Valider");
        
        button.setOnAction((ActionEvent event) -> {
            pseudo = textFieldPseudo.getText();
            adress = textFieldAdress.getText();
            String portStr = textFieldPort.getText();
            port = Integer.parseInt(portStr);
            
            if(pseudo.isEmpty() || adress.isEmpty()){
                
            }else{
                
                try {
                    client = new ClientClavarde(port,adress, this);
                    stage.setScene(scene_chat);
                    client.startListen();
                }catch (IOException ex) {
                    System.err.println("Erreur connection serveur "+adress);
                    stage.setScene(scene_error);
                }
            }
        });
        return button;
    }
    
    private void initPseudo(){
        borderPPseudo = new BorderPane();
        
        textFieldPseudo = new TextField("zorg");
        textFieldAdress = new TextField("localhost");
        textFieldPort = new TextField("12345");
        
        Label labelPseudo = new Label("Pseudo :");
        Label labelAdress = new Label("Adresse serveur :");
        Label labelPort = new Label("Port serveur :");
        
        Button buttonValidPseudo = initBTValidPseudo();
        
        HBox hboxButton = new HBox();
        HBox hboxPseudo = new HBox();
        HBox hboxAdress = new HBox();
        HBox hboxPort = new HBox();
        
        hboxButton.getChildren().add(buttonValidPseudo);
        hboxButton.setAlignment(Pos.CENTER);
        hboxPseudo.getChildren().addAll(labelPseudo, textFieldPseudo);
        hboxPseudo.setAlignment(Pos.CENTER);     
        hboxAdress.getChildren().addAll(labelAdress, textFieldAdress);
        hboxAdress.setAlignment(Pos.CENTER);
        hboxPort.getChildren().addAll(labelPort, textFieldPort);
        hboxPort.setAlignment(Pos.CENTER);


        VBox vboxCenter = new VBox();
        vboxCenter.setAlignment(Pos.CENTER);
        vboxCenter.setSpacing(10);
        vboxCenter.getChildren().addAll(hboxPseudo, hboxAdress, hboxPort);
        
        borderPPseudo.setPadding(new Insets(10));
        borderPPseudo.setCenter(vboxCenter);
        borderPPseudo.setBottom(hboxButton);
               
        
    }
    
    private void initError(){
        vBoxError = new VBox();
        
        Text text = new Text("Erreur communication serveur");
        Button button = initQuitBT();
        
        vBoxError.setAlignment(Pos.CENTER);
        vBoxError.setPadding(new Insets(10));
        vBoxError.getChildren().addAll(text, button);
        
    }
        
    public ClientGUI() {
        semaphore = new Semaphore(1);
        
        initChat();
        initPseudo();
        initError();
        
        scene_pseudo = new Scene(borderPPseudo, 500, 500);
        scene_chat = new Scene(borderPChat, 500, 500);    
        scene_error = new Scene(vBoxError, 300, 200);
        
        stage = new Stage();
        stage.setTitle("JClavardAMU Client");
        stage.setScene(scene_pseudo);
    
    }
}
