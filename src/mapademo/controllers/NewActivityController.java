package mapademo.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

/**
 * Controller that handles all the funcionalities of creating a new activity.
 *
 * La anotación @FXML conecta automáticamente los campos de esta clase
 * con los elementos declarados en el fichero FXML mediante su atributo fx:id.
 *
 * Implementa {@link Initializable} para poder ejecutar código de
 * inicialización una vez que el FXML ha sido cargado completamente.
 */
public class NewActivityController implements Initializable {

    @FXML
    private Button createButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Text filenameLabel;
    @FXML
    private ImageView mapViewer;
    @FXML
    private Text distance;
    @FXML
    private Text duration;
    @FXML
    private Text avgSpeed;
    @FXML
    private Text avgPace;
    @FXML
    private Text elevationPositive;
    @FXML
    private Text elevationNegative;
    @FXML
    private Text maxAlt;
    @FXML
    private Text minAlt;
    @FXML
    private VBox details;
    @FXML
    private Text title;
    @FXML
    private Button cancelButton;
    
    
    @FXML
    private void toggleVisibility(boolean visibility){
        // Hide the fields we do not want to show until a file has been submitted
        //mapViewer.setVisible(visibility);
        details.setVisible(visibility);
        title.setVisible(visibility);
        filenameLabel.setVisible(visibility);
        
        if(!visibility){
            createButton.setStyle("-fx-background-color: lightgray;");
            createButton.setCursor(Cursor.DEFAULT);
        } else {
            createButton.setStyle("-fx-background-color: #008000;");
            createButton.setCursor(Cursor.HAND);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        toggleVisibility(false);
        
        cancelButton.setOnMouseClicked(this::handleCancel);
        
        uploadButton.setOnMouseClicked(this::handleFileUpload);
    }
    
    @FXML
    private void handleCancel(MouseEvent event){
        System.exit(0);
    }
    
    @FXML
    private void handleFileUpload(MouseEvent event){
        Window win = details.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload GPX file");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("GPX file", "*.gpx"));
        File selectedFile = fileChooser.showOpenDialog(null);
        
        if(selectedFile != null){
            // Read
            SportActivityApp app = SportActivityApp.getInstance();
            Activity activity = app.importActivity(selectedFile);
            
            System.out.println(activity);
            
            mapViewer.setVisible(true);
            mapViewer.setImage(new Image("resources/calderona.jpg"));
            filenameLabel.setText(selectedFile.getName());
            toggleVisibility(true);
        }
        //System.out.println("HOLA");
    }

    @FXML
    private void about(ActionEvent event) {
    }
}
