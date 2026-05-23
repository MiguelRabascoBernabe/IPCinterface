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
import upv.ipc.sportlib.Session;

/**
 * FXML Controller class
 *
 * @author pipec
 */
public class SessionController implements Initializable {

    @FXML
    private TableView<Session> sessionTable;
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
    private TableColumn<Session, String> startColumn;

    @FXML
    private TableColumn<Session, String> endColumn;

    @FXML
    private TableColumn<Session, String> durationColumn;

    
    private final SportActivityApp app = SportActivityApp.getInstance();
    public User user = app.getCurrentUser();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    @FXML
    private Text totalActivities;
    @FXML
    private TableColumn<Session, String> importedColumn;
    @FXML
    private TableColumn<Session, String> viewedColumn;
    @FXML
    private TableColumn<Session, String> createdColumn;

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
            new SimpleStringProperty(data.getValue().getStartTime().format(fmt))
        );
        endColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEndTime().format(fmt))
        );
        durationColumn.setCellValueFactory(data -> {
            Duration d = data.getValue().getDuration();
            return new SimpleStringProperty(d.toHours() + "h " + (d.toMinutes() % 60) + "m");
        });
        importedColumn.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getImportedActivities()))
        );
        viewedColumn.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getViewedActivities()))
        );
        createdColumn.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getAnnotationsCreated()))
        );

        sessionTable.getItems().addAll(user.getSessions());
        loadStats();

        
    }    
    private void loadStats() {
        var sessions = user.getSessions();

        Duration total = Duration.ZERO;
        int totalImported = 0;
        int totalViewed = 0;
        int totalAnnotations = 0;

        for (Session s : sessions) {
            total = total.plus(s.getDuration());
            totalImported += s.getImportedActivities();
            totalViewed += s.getViewedActivities();
            totalAnnotations += s.getAnnotationsCreated();
        }

        totalSessions.setText("Total Sessions: " + sessions.size());
        totalTime.setText("Total Time: " + total.toHours() + "h " + (total.toMinutes() % 60) + "m");
        imported.setText("Imported: " + totalImported);
        viewed.setText("Viewed: " + totalViewed);
        annotations.setText("Annotations: " + totalAnnotations);
        totalActivities.setText("Total Activities: " + user.getActivities().size());
        
        
    }

    @FXML
    private void closeSession(ActionEvent event) {
        sessionTable.getScene().getWindow().hide();
    }
    

    
}
