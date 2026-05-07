package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import upv.ipc.sportlib.User;
/**
 * FXML Controller class
 *
 * @author DPORDEZ
 */
public class LoginFormController implements Initializable {

    @FXML
    private TextField username;
    @FXML
    private Text usernameError;
    @FXML
    private Text usernameInstructions;
    @FXML
    private TextField email;
    @FXML
    private Text emailError;
    @FXML
    private PasswordField password;
    @FXML
    private Text passwordError;
    @FXML
    private Text passwordInstructions;
    @FXML
    private DatePicker birthdate;
    @FXML
    private Text birthdateError;
    @FXML
    private Button cancelButton;
    @FXML
    private Button createButton;
    @FXML
    private Hyperlink signInGo;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void cancelAction(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void createAccount(ActionEvent event) {
        boolean mistake=false;
        if(User.checkEmail(email.getText())&&
           User.checkPassword(password.getText())&&
           User.checkNickName(username.getText())&&
           User.isOlderThan(birthdate.getValue(),12)){
                if(mistake){
                    emailError.setText("");passwordError.setText("");passwordInstructions.setText("");
                    usernameError.setText("");usernameInstructions.setText("");birthdateError.setText("");
                    mistake=false;
                }
            }
        else{mistake=true;
            if(!User.checkEmail(email.getText())){
                if(email.getText().equals("")){emailError.setText("This field is mandatory");}
                else{emailError.setText("Non-valid E-mail address");}}
            else{emailError.setText("");}
            if(!User.checkPassword(password.getText())){
                if(password.getText().equals("")){passwordError.setText("This field is mandatory");}
                else{passwordError.setText("Non-valid password");}
                passwordInstructions.setText("8 to 20 characters\nAt least one uppercase letter\nAt least one lowercase letter\nOne digit\nOne symbol (!@#$%&*()-+=)");}
            else{passwordError.setText("");passwordInstructions.setText("");}
            if(!User.checkNickName(username.getText())){
                if(username.getText().equals("")){usernameError.setText("This field is mandatory");}
                else{usernameError.setText("Non-valid nickname");}
                usernameInstructions.setText("6 to 15 characters\nLetters, digits, hyphen or underscore only");}
            else{usernameError.setText("");usernameInstructions.setText("");}
            if(!User.isOlderThan(birthdate.getValue(),12)){
                if(birthdate.getValue()==null){birthdateError.setText("This field is mandatory");}
                else{birthdateError.setText("User must be older than 12 years old");}
            }else{birthdateError.setText("");}
        }
    }

    @FXML
    private void signInChange(ActionEvent event) {
        
    }
    
}
