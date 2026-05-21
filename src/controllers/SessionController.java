/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author pipec
 */
public class SessionController implements Initializable {

    @FXML
    private MenuItem sessionHistory;
    @FXML
    private TableView<?> sessionTable;
    @FXML
    private Text totalSessions;
    @FXML
    private Text totalTime;
    @FXML
    private Text imported;
    @FXML
    private Text viewed;
    @FXML
    private Text annotations;
    @FXML
    private DatePicker dayPicker;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        totalSessions.setText("Total Sessions: 0");
        totalTime.setText("Total Time: 0h");

        imported.setText("Imported: 0");
        viewed.setText("Viewed: 0");
        annotations.setText("Annotations: 0");

        dayPicker.setValue(LocalDate.now());
    }    

    @FXML
    private void about(ActionEvent event) {
    }
    
}
