/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.Duration;

import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author pipec
 */
public class SessionController implements Initializable {

    @FXML
    private MenuItem sessionHistory;
    @FXML
    private TableView<Activity> sessionTable;
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
    
    @FXML
    private TableColumn<Activity, String> startColumn;

    @FXML
    private TableColumn<Activity, String> endColumn;

    @FXML
    private TableColumn<Activity, String> durationColumn;

    
    private final SportActivityApp app = SportActivityApp.getInstance();

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
        
        startColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getStartTime().toString()
            )
        );
        
        endColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getEndTime().toString()
            )
        );
        
        durationColumn.setCellValueFactory(data -> {

            Duration d = data.getValue().getDuration();

            String texto =
                    d.toHours() + "h "
                    + (d.toMinutes() % 60) + "m";

            return new SimpleStringProperty(texto);
        });
        
        sessionTable.getItems().addAll(app.getUserActivities());


        
    }    

    @FXML
    private void about(ActionEvent event) {
    }
    
}
