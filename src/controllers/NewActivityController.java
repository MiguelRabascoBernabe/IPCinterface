package controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class NewActivityController implements Initializable {
    @FXML
    private Text filenameLabel;
    @FXML
    private Button uploadButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button createButton;
    
    SportActivityApp context;
    File fileToUpload;
    
    
    private void toggleVisibility(boolean visibility){
        // Hide the fields we do not want to show until a file has been submitted
        filenameLabel.setVisible(visibility);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        toggleVisibility(false);
        
        cancelButton.setOnMouseClicked(this::handleCancel);
        uploadButton.setOnMouseClicked(this::handleFileUpload);
        createButton.setOnMouseClicked(this::handleDBActivityRegister);
        createButton.setDisable(true);
    }
    
    private void handleDBActivityRegister(MouseEvent event){
        if(createButton.disableProperty().get() == true) return;
        
        context.importActivity(fileToUpload);
        System.out.println("File uploaded!");
        System.exit(0);
    }
    
    private void handleCancel(MouseEvent event){
        System.exit(0);
    }
    
    private void handleFileUpload(MouseEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload GPX file");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("GPX file", "*.gpx"));
        fileToUpload = fileChooser.showOpenDialog(null);
        
        if(fileToUpload != null){
            context = SportActivityApp.getInstance();
            // placeholder login in order to test the functionalities
            context.login("testing", "Ul12345$");
            
            createButton.setDisable(false);
            createButton.setCursor(Cursor.HAND);
            
            System.out.println(context.getUserActivities());
            filenameLabel.setText(fileToUpload.getName());
            toggleVisibility(true);
        }
    }
}
