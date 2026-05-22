/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author victo
 */
public class IntialSceneController implements Initializable {

    @FXML
    private Button uploadBtn;
    @FXML
    private TextField minLatF;
    @FXML
    private TextField minLongF;
    @FXML
    private TextField maxLatF;
    @FXML
    private TextField maxLongF;
    @FXML
    private ImageView mapViewer;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void uploadFunction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Image file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image file", "*.jpg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        
        if(selectedFile != null){
            // Read
//            SportActivityApp app = SportActivityApp.getInstance();
//            Activity activity = app.importActivity(selectedFile);
//            
//            System.out.println(activity);
            
            mapViewer.setVisible(true);
            mapViewer.setImage(new Image("resources/calderona.jpg"));
            //filenameLabel.setText(selectedFile.getName());
        }
    }
    
    

    @FXML
    private void cancelFunction(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void confirmFunction(ActionEvent event) {
    }
    
}
