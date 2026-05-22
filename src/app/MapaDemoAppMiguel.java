package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MapaDemoAppMiguel extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        int window = 0; // 0 for New Activity, 1 for new annotation and 2 for cumulative statistics
        Parent root;
        
        // Cases to "dynamically" add the required FXML for testing purposes
        if(window == 0) root = FXMLLoader.load(getClass().getResource("/fxml/NewActivity.fxml"));
        else if(window == 1) root = FXMLLoader.load(getClass().getResource("/fxml/NewAnnotation.fxml"));
        else root = FXMLLoader.load(getClass().getResource("/fxml/CumulativeStatistics.fxml"));
        
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        Scene scene = new Scene(root);
        
        // Cases to "dynamically" add the required CSS for testing purposes
        if(window == 0) scene.getStylesheets().add(this.getClass().getResource("/css/newActivityStyles.css").toExternalForm());
        else if (window == 1) scene.getStylesheets().add(this.getClass().getResource("/css/newAnnotationStyles.css").toExternalForm());
        else if (window == 2) scene.getStylesheets().add(this.getClass().getResource("/css/statisticsStyles.css").toExternalForm());
        
        stage.setTitle("Demo mapas - IPC");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
