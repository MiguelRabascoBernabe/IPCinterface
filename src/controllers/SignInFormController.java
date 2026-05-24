/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import app.MapaDemoAppDani;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author DPORDEZ
 */
public class SignInFormController implements Initializable {

    @FXML
    private TextField username;
//    @FXML
//    private Text usernameError;
    @FXML
    private PasswordField password;
    @FXML
    private Text passwordError;
    @FXML
    private Hyperlink registerGo;
    @FXML
    private Button cancelButton;
    @FXML
    private Button signInButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void registerGo(ActionEvent event) {
        try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loginForm.fxml"));
        Parent root = loader.load();
        LoginFormController controller = loader.getController();
        
        Stage newStage = new Stage();
        
        newStage.setScene(new Scene(root));
        newStage.setTitle("Register");
        newStage.show();
        Stage stage = (Stage) registerGo.getScene().getWindow();
        stage.close();
        } catch (IOException ex) {
        ex.printStackTrace();
        }
    }

    @FXML
    private void cancelClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InitialScene.fxml"));
            Parent root = loader.load();
            InitialSceneController controller = loader.getController();

            Stage newStage = new Stage();

            newStage.setScene(new Scene(root));
            newStage.setTitle("Sign In");
            newStage.show();
            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.close();
            } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void signInGo(ActionEvent event) {
        if(MapaDemoAppDani.getContext().getApp().login(username.getText(),password.getText())){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainSceneFXML.fxml"));
                Parent root = loader.load();
                MainSceneController controller = loader.getController();

                Stage newStage = new Stage();

                newStage.setScene(new Scene(root));
                newStage.setTitle("Estraba");
                newStage.show();
                Stage stage = (Stage) registerGo.getScene().getWindow();
                stage.close();
                } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else{passwordError.setText("Username and password do not match");}
    }

    @FXML
    private void enterPressed(KeyEvent event) {
        if(event.getCode()==KeyCode.ENTER){signInGo(new ActionEvent());}
    }
    
}
