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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

/**
 *
 * @author Miguel
 */
public class ListActivitiesController implements Initializable{

    @FXML
    private Button newButton;
    @FXML
    private ListView<Activity> listview;
    
    private Activity selectedActivity;
    
    public Activity getSelectedActivity(){
        return selectedActivity;
    }
    
    SportActivityApp app = SportActivityApp.getInstance();
    
    @FXML
    private void loadNewActivity(ActionEvent e) throws IOException {
        FXMLLoader loader =
            new FXMLLoader(getClass().getResource("/fxml/NewActivity.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            this.getClass()
                .getResource("/css/newActivityStyles.css")
                .toExternalForm()
        );

        Stage stage = new Stage();
        stage.setTitle("New activity");
        stage.setScene(scene);

        // Wait until the window closes
        stage.showAndWait();

        // Refresh activities after closing
        listview.getItems().setAll(
            app.getUserActivities()
        );
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listview.setPlaceholder(
            new Label("No activities imported yet")
        );
//        System.out.println(
//            app.getUserActivities().size()
//        );
        listview.getItems().setAll(
            app.getUserActivities()
        );

        // Custom text for each activity
        listview.setCellFactory(lv -> new ListCell<Activity>() {
            @Override
            protected void updateItem(Activity activity, boolean empty) {
                super.updateItem(activity, empty);

                if (empty || activity == null) {
                    setText(null);
                } else {
                    setText(
                        activity.getName()
                        + " | "
                        + String.format("%.2f km",
                            activity.getTotalDistance() / 1000.0)
                    );
                }
            }
        });

        listview.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                confirmSelection();
            }
        });
    }
    
    @FXML
    private void confirmSelection() {
        selectedActivity =
            listview.getSelectionModel().getSelectedItem();

        if (selectedActivity == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("You must select the activity to load");
            alert.show();
            return;
        }

        Stage stage =
            (Stage) listview.getScene().getWindow();

        stage.close();
    }
}
