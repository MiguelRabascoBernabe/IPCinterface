/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;import javafx.scene.shape.Circle;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;

/**
 * FXML Controller class
 *
 * @author pipec
 */
public class UserController implements Initializable {

    @FXML
    private Button saveButton;
    @FXML
    private Button editButton;

    /**
     * Initializes the controller class.
     */
    
    public boolean editmode;
    @FXML
    private TextField usernameInput;
    @FXML
    private TextField emailInput;
    @FXML
    private TextField passInput;
    @FXML
    private DatePicker birthdateInput;
    @FXML
    private Button editAvatar;
    @FXML
    private ImageView profileImage;
    @FXML
    private Circle profileCircle;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //buttons
        editmode = false;
        saveButton.setDisable(true);
        //text inputs
        usernameInput.setDisable(true);
        emailInput.setDisable(true);
        passInput.setDisable(true);
        birthdateInput.setDisable(true);
        //edit avatar
        editAvatar.setDisable(true);
        
        //clip circle
        profileImage.setFitWidth(150);
        profileImage.setFitHeight(150);
        profileImage.setPreserveRatio(false);
    
        Circle clip = new Circle(75, 75, 75);
        profileImage.setClip(clip);
        
        //image default
        profileImage.setImage(new Image("resources/default_user_icon.jpg"));

        

        
    }    

    @FXML
    private void about(ActionEvent event) {
    }

    @FXML
    private void saveClick(ActionEvent event) {
        editmode = false;
        editButton.setDisable(false);
        saveButton.setDisable(true);
        emailInput.setDisable(true);
        passInput.setDisable(true);
        birthdateInput.setDisable(true);
        editAvatar.setDisable(true);


   
    }

    @FXML
    private void editClick(ActionEvent event) {
        editmode = true;
        editButton.setDisable(true);
        saveButton.setDisable(false);
        
        emailInput.setDisable(false);
        passInput.setDisable(false);
        birthdateInput.setDisable(false);
        editAvatar.setDisable(false);

    }

    @FXML
    private void avatarSelect(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png",
                                    "*.jpg","*.jpeg"));
        File file = fc.showOpenDialog(profileImage.getScene().getWindow());
        if (file != null){
            profileImage.setImage(new Image(file.toURI().toString()));
            }
    }
    
}
