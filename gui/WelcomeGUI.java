package gui;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class WelcomeGUI extends Application {
    private Stage stage;
    private Stage stage_client;
    private Stage stage_server;
    private Scene scene_choice;
    private BorderPane borderPChoice;
    
    private Button initBTServer(){
        Button button = new Button("Serveur");
        
        button.setOnAction((ActionEvent event) -> {
            stage_server.show();
            stage.hide();
        });
        return button;
    }
    
    private Button initBTClient(){
        Button button = new Button("Client");
        
        button.setOnAction((ActionEvent event) -> {
            stage_client.show();
            stage.hide();
            
        });
        return button;
    }
    
    private void initChoice(){
        borderPChoice = new BorderPane();
        
        Button buttonServer = initBTServer();
        Button buttonClient = initBTClient();
        
        HBox hboxButton = new HBox();
        
        hboxButton.setAlignment(Pos.CENTER);
        hboxButton.setSpacing(10);
        hboxButton.setPadding(new Insets(10));
        hboxButton.getChildren().addAll(buttonServer, buttonClient);
        
        borderPChoice.setCenter(hboxButton);
        
    }
    
    @Override
    public void start(Stage primaryStage) {
        ClientGUI clientGUI = new ClientGUI();
        ServerGUI serverGUI = new ServerGUI();
        stage = primaryStage;
        
        initChoice();
        
        scene_choice = new Scene(borderPChoice, 200, 100);
        
        stage_client = clientGUI.stage;
        stage_server = serverGUI.stage;
        
        primaryStage.setTitle("JClavardAMU");
        primaryStage.setScene(scene_choice);
        primaryStage.show();
    }

    public void launchGUI(){
        launch();
    }
}
