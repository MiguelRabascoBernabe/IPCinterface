/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import java.net.URL;
import java.time.format.*;
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
import upv.ipc.sportlib.User;

/**
 * FXML Controller class
 *
 * @author pipec
 */
public class SessionController implements Initializable {

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
    private TableColumn<Activity, String> startColumn;

    @FXML
    private TableColumn<Activity, String> endColumn;

    @FXML
    private TableColumn<Activity, String> durationColumn;

    
    private final SportActivityApp app = SportActivityApp.getInstance();
    public User user = app.getCurrentUser();
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
        
       
        startColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getStartTime().format(
                                DateTimeFormatter.ofPattern("dd/MM HH:mm")
                )
            )
        );
        
        endColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getEndTime().format(
                                DateTimeFormatter.ofPattern("dd/MM HH:mm")
                )
            )
        );
        
        durationColumn.setCellValueFactory(data -> {

            Duration d = data.getValue().getDuration();

            String texto =
                    d.toHours() + "h "
                    + (d.toMinutes() % 60) + "m";

            return new SimpleStringProperty(texto);
        });
        startNumbers();
        
        sessionTable.getItems().addAll(app.getUserActivities());


        
    }    
    private void startNumbers(){
        Duration total = Duration.ZERO;
        if(user.getActivities().size()>0){
            totalSessions.setText("Total Sessions: "+ user.getActivities().size());
            //get total time
            for(Activity a: user.getActivities()){
                total = total.plus(a.getDuration());}
            totalTime.setText("Total Time: " + total.toHours() + "h " + (total.toMinutes() % 60) + "m");

            imported.setText("Imported: 0");
            viewed.setText("Viewed: 0");
            annotations.setText("Annotations: 0");
        }
    }

    @FXML
    private void closeSession(ActionEvent event) {
            sessionTable.getScene().getWindow().hide();

    }
    

    
}
