/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author DPORDEZ
 */
public class InitialSceneController implements Initializable {

    @FXML
    private Button signIn;
    @FXML
    private Button register;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void signInGo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signInForm.fxml"));
            Parent root = loader.load();
            SignInFormController controller = loader.getController();

            Stage newStage = new Stage();

            newStage.setScene(new Scene(root));
            newStage.setTitle("Sign In");
            newStage.show();
            Stage stage = (Stage) signIn.getScene().getWindow();
            stage.close();
            } catch (IOException ex) {
            ex.printStackTrace();
        }
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
        Stage stage = (Stage) register.getScene().getWindow();
        stage.close();
        } catch (IOException ex) {
        ex.printStackTrace();
        }
    }
    
}
