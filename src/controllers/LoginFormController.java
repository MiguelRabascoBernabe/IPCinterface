package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import upv.ipc.sportlib.User;
import app.*;

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
    String userChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    public void initialize(URL url, ResourceBundle rb) {
        username.textProperty().addListener((obs, o, n)->{
            int w=n.length();
            if(w>0){
            if(!checkLetter(n.charAt(w-1))){
                username.setText(o);
                usernameInstructions.setText("6 to 15 characters\nLetters, digits, hyphen or underscore only");
            }
            else if(w>15){username.setText(o);
                usernameInstructions.setText("6 to 15 characters\nLetters, digits, hyphen or underscore only");
            }
            else {usernameInstructions.setText("");}
            }
        });
    }    

    @FXML
    private void cancelAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InitialScene.fxml"));
            Parent root = loader.load();
            InitialSceneController controller = loader.getController();

            Stage newStage = new Stage();

            newStage.setScene(new Scene(root));
            newStage.setTitle("Welcome");
            newStage.show();
            Stage stage = (Stage) signInGo.getScene().getWindow();
            stage.close();
            } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void createAccount(ActionEvent event) {
        if(User.checkEmail(email.getText())&&
           User.checkPassword(password.getText())&&
           User.checkNickName(username.getText())&&
           User.isOlderThan(birthdate.getValue(),12)){
               emailError.setText("");passwordError.setText("");passwordInstructions.setText("");
               usernameError.setText("");usernameInstructions.setText("");birthdateError.setText("");
               if(MapaDemoAppDani.getContext().getApp().registerUser(username.getText(), email.getText(), password.getText(), birthdate.getValue(), "/resources/default_User_Icon.jpg")){
                   MapaDemoAppDani.getContext().getApp().login(username.getText(),password.getText());
                   System.out.println(MapaDemoAppDani.getContext().getApp().getCurrentUser().getEmail());
                   try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainSceneFXML.fxml"));
                        Parent root = loader.load();
                        MainSceneController controller = loader.getController();

                        Stage newStage = new Stage();

                        newStage.setScene(new Scene(root));
                        newStage.setTitle("Estraba");
                        newStage.show();
                        Stage stage = (Stage) signInGo.getScene().getWindow();
                        stage.close();
                        } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }else{
                   usernameError.setText("This username already exists, try signing in!");
                }
            }
        else{
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signInForm.fxml"));
            Parent root = loader.load();
            SignInFormController controller = loader.getController();

            Stage newStage = new Stage();

            newStage.setScene(new Scene(root));
            newStage.setTitle("Sign In");
            newStage.show();
            Stage stage = (Stage) signInGo.getScene().getWindow();
            stage.close();
            } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private boolean checkLetter(char c){
        return userChar.contains(""+c);
    }
}
