/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

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
import java.util.Locale;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.effect.GaussianBlur;
import upv.ipc.sportlib.*;
import app.Poi;
import javafx.scene.Scene;
import javafx.scene.text.Text;



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
    private MenuItem sessionHistory;
    
    private SportActivityApp app = SportActivityApp.getInstance();
    @FXML
    private Text emailError;
    @FXML
    private Text passError;
    @FXML
    private Circle profileCircle;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //config datepicker
        birthdateInput.setChronology(java.time.chrono.IsoChronology.INSTANCE);
        Locale.setDefault(Locale.forLanguageTag("es-ES"));
        
        
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
        profileImage.setImage(new Image("/resources/default_user_icon.jpg"));
        //test login
        
        app.login("testing", "Ul12345$");
        configParameter(app);
        verifyParameter(app);
        
        //verify parameters
        
        
        
        
    }
    public void configParameter(SportActivityApp App){
        User thisUser = app.getCurrentUser();
        System.out.println("Usuario: " + thisUser); // añade esto
        if (thisUser == null) return;

        System.out.println("Nick: " + thisUser.getNickName()); // y esto
        usernameInput.setText(thisUser.getNickName());
        emailInput.setText(thisUser.getEmail());
        passInput.setText(thisUser.getPassword());
        
        
    }
    public void verifyParameter(SportActivityApp App){
        emailInput.textProperty().addListener((obs, old, val) -> {
            if (emailError == null) return;
            if (!User.validateEmail(val)) {
                //emailError.setText("Email no válido");
                emailError.setVisible(true);
            } else {
                emailError.setVisible(false);
            }
        });
        passInput.textProperty().addListener((obs, old, val) -> {
            if (passError == null) return;
            //System.out.println("password changed: " + val);

            if (!User.validatePassword(val)) {
                //passError.setText("Contraseña no válida");
                passError.setVisible(true);
            } else {
                passError.setVisible(false);
            }
    });
        
        
    }

    @FXML
    private void about(ActionEvent event){
    }

    @FXML
    private void saveClick(ActionEvent event) {
        //buttons and textinput manipulation
        editmode = false;
        editButton.setDisable(false);
        saveButton.setDisable(true);
        emailInput.setDisable(true);
        passInput.setDisable(true);
        birthdateInput.setDisable(true);
        editAvatar.setDisable(true);
        //passinput blur back
        GaussianBlur passblur = new GaussianBlur();
        passblur.setRadius(10);
        passInput.setEffect(passblur);
        



   
    }

    @FXML
    private void editClick(ActionEvent event) {
        //button toggling
        editmode = true;
        editButton.setDisable(true);
        saveButton.setDisable(false);
        // input effects
        emailInput.setDisable(false);
        passInput.setDisable(false);
        birthdateInput.setDisable(false);
        editAvatar.setDisable(false);
        // pass input effect
        passInput.setEffect(null);
        

    }

    @FXML
    private void avatarSelect(ActionEvent event) throws Exception {
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
