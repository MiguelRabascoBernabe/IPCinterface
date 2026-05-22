package controllers;

import java.net.URL;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import java.time.Duration;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class CumulativeController implements Initializable {
    @FXML
    private Text totalTime;
    @FXML
    private Text accumulatedDistance;
    @FXML
    private Text ascendedMetres;
    @FXML
    private Text descendedMetres;
    @FXML
    private Button calculateButton;
    @FXML
    private ComboBox<String> monthCombobox;
    @FXML
    private ComboBox<String> yearCombobox;
    
    private void initializeCombobox(ComboBox<String> cb, String[] options){
        ObservableList<String> items = cb.getItems();
        
        items.removeAll(items);
        items.addAll(options);
    }
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    
    private void handleSearchActivities(ActionEvent e){
        if(monthCombobox.getSelectionModel().getSelectedItem() == null || yearCombobox.getSelectionModel().getSelectedItem() == null) return;
        
        SportActivityApp app = SportActivityApp.getInstance();
        app.login("testing", "Ul12345$");
        List<Activity> activities = app.getUserActivities();
        
        Month month = Month.valueOf(monthCombobox.getSelectionModel().getSelectedItem().toUpperCase());
        int year = Integer.parseInt(yearCombobox.getSelectionModel().getSelectedItem());
        YearMonth selected = YearMonth.of(year, month);
        double totalDistance = 0, totalGain = 0, totalLoss = 0; 
        Duration totalDuration = Duration.ZERO;

        for(Activity lol : activities){
            if(YearMonth.from(lol.getStartTime().toLocalDate()).equals(selected)){
                totalDistance += lol.getTotalDistance();
                totalGain += lol.getElevationGain();
                totalLoss += lol.getElevationLoss();
                totalDuration = totalDuration.plus(lol.getDuration());
            }
        }
        
        long hours = totalDuration.toHours();
        long minutes = totalDuration.toMinutesPart();
        
        
        totalTime.setText(String.format("%dh %02dmin", hours, minutes));
        accumulatedDistance.setText(round(totalDistance, 2) + "m");
        ascendedMetres.setText(round(totalGain, 2) + "m");
        descendedMetres.setText(round(totalLoss, 2) + "m");
    }
    
    private void handleActivateCalculateButton(ActionEvent e){
        if(monthCombobox.getSelectionModel().getSelectedItem() != null && yearCombobox.getSelectionModel().getSelectedItem() != null) calculateButton.setDisable(false);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ///////////////// COMBOBOXES INITIALIZATION /////////////////
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] years = {"2021", "2022", "2023", "2024", "2025", "2026"};
        initializeCombobox(monthCombobox, months);
        initializeCombobox(yearCombobox, years);
        /////////////////////////////////////////////////////////////
        
        calculateButton.setOnAction(this::handleSearchActivities);
        calculateButton.setDisable(true);
        
        /////////////////////////////////////////////////////////////
        
        monthCombobox.setOnAction(this::handleActivateCalculateButton);
        yearCombobox.setOnAction(this::handleActivateCalculateButton);
    }    
    
}
