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
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/newAnnotation.fxml"));
//        Parent root = FXMLLoader.load(getClass().getResource("/fxml/NewActivity.fxml"));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        Scene scene = new Scene(root);
//        String css = this.getClass().getResource("/css/newActivityStyles.css").toExternalForm();
//        scene.getStylesheets().add(this.getClass().getResource("/css/statisticsStyles.css").toExternalForm());
        scene.getStylesheets().add(
            getClass().getResource("/css/newAnnotationStyles.css").toExternalForm()
        );
//        System.out.println(css);
//        scene.getStylesheets().add(css);
        stage.setTitle("Demo mapas - IPC");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
