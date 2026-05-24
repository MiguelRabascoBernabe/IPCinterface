/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author victo
 */
public class AddMapController implements Initializable {

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
    @FXML
    private Button addBtn;
    
    SportActivityApp app = SportActivityApp.getInstance();
    String fileName;
    File selectedFile;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        mapViewer.setVisible(false);
        
        addBtn.disableProperty().bind(
            minLatF.textProperty().isEmpty()
            .or(minLongF.textProperty().isEmpty())
            .or(maxLatF.textProperty().isEmpty())
            .or(maxLongF.textProperty().isEmpty())
            .or(mapViewer.visibleProperty().not())
        );
    }    

    @FXML
    private void uploadFunction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Image file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image file", "*.jpg"));
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        fileName = selectedFile.getName();
        
        if(selectedFile != null){          
            mapViewer.setVisible(true);
            mapViewer.setImage(new Image(selectedFile.toURI().toString()));
            //filenameLabel.setText(selectedFile.getName());
        }
    }
    
    

    @FXML
    private void cancelFunction(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void confirmFunction(ActionEvent event) {
        app.addMapRegion(fileName, selectedFile, Double.parseDouble(minLatF.getText()), Double.parseDouble(maxLatF.getText()), Double.parseDouble(minLongF.getText()), Double.parseDouble(maxLatF.getText()));
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void validarNumero(KeyEvent event) {
        TextField txt = (TextField) event.getSource();
        if (!txt.getText().matches("-?[0-9]*(\\.[0-9]*)?")) {
            txt.setText(txt.getText().replaceAll("[^0-9.-]", ""));
            txt.end();
        }
    }
    
}
