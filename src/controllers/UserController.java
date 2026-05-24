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
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.effect.GaussianBlur;
import upv.ipc.sportlib.*;
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
    
    public boolean editmode = false, avatarChanged = false;
    public boolean emailValid = true, passValid = true;
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
    
    private SportActivityApp app = SportActivityApp.getInstance();
    @FXML
    private Text emailError;
    @FXML
    private Text passError;
    @FXML
    private Circle profileCircle;
    
    public User user = app.getCurrentUser();
    
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
        //labels
        passError.setVisible(false);
        emailError.setVisible(false);
        
        //clip circle
        profileImage.setFitWidth(150);
        profileImage.setFitHeight(150);
        profileImage.setPreserveRatio(false);
    
        Circle clip = new Circle(75, 75, 75);
        profileImage.setClip(clip);
        
        //image default
        String avatarPath = user.getAvatarPath();
        try{
            if (avatarPath != null && !avatarPath.isEmpty()) {
                profileImage.setImage(new Image(avatarPath));
            } else {
             profileImage.setImage(new Image("/resources/default_user_icon.jpg"));
            }
        }
        catch (Exception e) {
            profileImage.setImage(new Image(getClass().getResourceAsStream("/resources/default_user_icon.jpg")));
        }
                
        //test login
        
        
        configParameter(app);
        verifyParameter(app);
        
        //verify parameters
        //testingUser123-
        
        
        
    }
    public void configParameter(SportActivityApp App){
        if (user == null) return;

        usernameInput.setText(user.getNickName());
        emailInput.setText(user.getEmail());
        passInput.setText(user.getPassword());
        birthdateInput.setValue(user.getBirthDate());
        
        
    }
    public void verifyParameter(SportActivityApp App){
        emailInput.textProperty().addListener((obs, old, val) -> {
            if (emailError == null) return;
            if (!User.validateEmail(val)) {
                //emailError.setText("Email no válido");
                emailError.setVisible(true);
                emailValid = false;
            } else {
                emailError.setVisible(false);
                emailValid = true;
            }
        });
        passInput.textProperty().addListener((obs, old, val) -> {
            if (passError == null) return;

            if (!User.validatePassword(val)) {
                //passError.setText("Contraseña no válida");
                passError.setVisible(true);
                passValid = false;
            } else {
                passError.setVisible(false);
                passValid = true;
            }
    });
        
        
    }

    
    private void disableInputs(){
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
    private void saveClick(ActionEvent event) {
        //reload validation
        verifyParameter(app);
        
        //buttons and textinput manipulation
        //save will only toggle back if valid
        
        if(passValid && emailValid){
            disableInputs();
            updateEntry();
        }
    }
    
    private void updateEntry() {
        String password = passInput.getText();

        if (password == null || password.isBlank()) {
            password = user.getPassword();
        }

        app.updateCurrentUser(
            emailInput.getText(),
            password,
            birthdateInput.getValue(),
            profileImage.getImage().getUrl()
        );
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
        //paths seem to be local, so you cannot upload an image to the DB
        //avat images can NOT persistent
        avatarChanged = true;
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png",
                                    "*.jpg","*.jpeg"));
        File file = fc.showOpenDialog(profileImage.getScene().getWindow());
        
        if (file != null){
            profileImage.setImage(new Image(file.toURI().toString()));
            
            }
        
    }

    @FXML
    private void closeEdit(ActionEvent event) {
        emailInput.getScene().getWindow().hide();
    }
    
}
