package gui;

import client.ClientClavarde;
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
import javafx.stage.Stage;

public class ClientGUI {
    Stage stage;
    private Scene scene_pseudo;
    private Scene scene_chat;
    private BorderPane borderPPseudo;
    private BorderPane borderPChat;
    private TextField textFieldPseudo;
    private TextField textFieldAdress;
    private TextField textFieldChat;
    public String pseudo;
    private String adress;
    private Text chat;
    public Semaphore semaphore;
    
    private ClientClavarde client;
    
    public void addMessage(String pseudo, String message){
        String current_chat = chat.getText();
        chat.setText(current_chat.concat(pseudo+"> "+message+"\n"));
    }
    
    private Button initBTSend() {
        Button button = new Button("Envoyer");
        
        button.setOnAction((ActionEvent event) -> {
            String message = textFieldChat.getText();
            textFieldChat.clear();
                        
            if(!message.isEmpty()){
                client.sendMessage(message);
                try {
                    semaphore.acquire();
                    addMessage(pseudo, message);
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
            
            if(pseudo.isEmpty() || adress.isEmpty()){
                
            }else{
                client = new ClientClavarde(12345,adress, this);
                stage.setScene(scene_chat);
                client.startListen();
            }
        });
        return button;
    }
    
    private void initPseudo(){
        borderPPseudo = new BorderPane();
        
        textFieldPseudo = new TextField();
        textFieldAdress = new TextField();
        
        Label labelPseudo = new Label("Pseudo :");
        Label labelAdress = new Label("Adresse serveur :");
        
        Button buttonValidPseudo = initBTValidPseudo();
        
        HBox hboxButton = new HBox();
        HBox hboxPseudo = new HBox();
        HBox hboxAdress = new HBox();
        
        hboxButton.getChildren().add(buttonValidPseudo);
        hboxButton.setAlignment(Pos.CENTER);
        hboxPseudo.getChildren().addAll(labelPseudo, textFieldPseudo);
        hboxPseudo.setAlignment(Pos.CENTER);     
        hboxAdress.getChildren().addAll(labelAdress, textFieldAdress);
        hboxAdress.setAlignment(Pos.CENTER);
        
        
        VBox vboxCenter = new VBox();
        vboxCenter.setAlignment(Pos.CENTER);
        vboxCenter.setSpacing(10);
        vboxCenter.getChildren().addAll(hboxPseudo, hboxAdress);
        
        borderPPseudo.setPadding(new Insets(10));
        borderPPseudo.setCenter(vboxCenter);
        borderPPseudo.setBottom(hboxButton);
               
        
    }
        
    public ClientGUI() {
        semaphore = new Semaphore(1);
        
        initChat();
        initPseudo();
        
        scene_pseudo = new Scene(borderPPseudo, 500, 500);
        scene_chat = new Scene(borderPChat, 500, 500);             
        
        stage = new Stage();
        stage.setTitle("JClavardAMU Client");
        stage.setScene(scene_pseudo);
    
    }

   

    
    
    
}
