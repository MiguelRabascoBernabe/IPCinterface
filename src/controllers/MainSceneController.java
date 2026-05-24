package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import app.Poi;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Modality;
import javafx.stage.Stage;
import upv.ipc.sportlib.*;
import utils.AnnotationCreationState;


public class MainSceneController implements Initializable {

    SportActivityApp app = SportActivityApp.getInstance();

    @FXML
    private Group zoomGroup;

    @FXML
    private Pane mapPane;

    private ContextMenu mapContextMenu;

    private boolean insertionMode = false;

    @FXML
    private ListView<Poi> map_listview;

    @FXML
    private ScrollPane map_scrollpane;

    private double zoomV;

    private MenuButton map_pin;

    @FXML
    private SplitPane splitPane;
    @FXML
    private ImageView imageViewer;
    @FXML
    private Group contentGroup;
    @FXML
    private MenuItem viewEditBtn;
    @FXML
    private Button activitiesBtn;
    @FXML
    private LineChart<Number, Number> graficaAlturas;
    private boolean speedMode = false;

    private AnnotationCreationState annotationState;
    private boolean waitingForSecondPoint = false;
    private Activity currentActivity;

    private double mapWidth;
    private double mapHeight;
    @FXML
    private TextField distanceF;
    @FXML
    private TextField durationF;
    @FXML
    private TextField avgSpeedF;
    @FXML
    private TextField avgPaceF;
    @FXML
    private TextField elevGainF;
    @FXML
    private TextField elevLossF;
    @FXML
    private TextField minAltitudeF;
    @FXML
    private TextField maxAltitudeF;
    @FXML
    private Label activityName;
    
    void zoomIn(ActionEvent event) {
        double sliderVal = zoomV; //zoom_slider.getValue();
        //zoom_slider.setValue(sliderVal + 0.1);
        zoomV = zoomV +0.1;
    }

    void zoomOut(ActionEvent event) {
        double sliderVal = zoomV; //zoom_slider.getValue();
        //zoom_slider.setValue(sliderVal - 0.1);
        zoomV = zoomV -0.1;
        //System.out.println(app.registerUser("testing","test@tung.sahur", "Ul12345$", LocalDate.MIN, "/src/resources/logo.png"));
        System.out.println(app.login("testing", "Ul12345$"));
    }

    private void zoom(double scaleValue) {
        double scrollH = map_scrollpane.getHvalue();
        double scrollV = map_scrollpane.getVvalue();

        zoomGroup.setScaleX(scaleValue);
        zoomGroup.setScaleY(scaleValue);

        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }

    @FXML
    void listClicked(MouseEvent event) {
        Poi itemSelected = map_listview.getSelectionModel().getSelectedItem();
        if (itemSelected == null) return;

        double mapWidth  = mapPane.getWidth()  * zoomGroup.getScaleX();
        double mapHeight = mapPane.getHeight() * zoomGroup.getScaleY();

        double poiX = itemSelected.getPosition().getX() * zoomGroup.getScaleX();
        double poiY = itemSelected.getPosition().getY() * zoomGroup.getScaleY();

        double viewW = map_scrollpane.getViewportBounds().getWidth();
        double viewH = map_scrollpane.getViewportBounds().getHeight();

        double scrollH = (poiX - viewW / 2) / (mapWidth  - viewW);
        double scrollV = (poiY - viewH / 2) / (mapHeight - viewH);

        scrollH = Math.max(0, Math.min(1, scrollH));
        scrollV = Math.max(0, Math.min(1, scrollV));

        final Timeline timeline = new Timeline();
        final KeyValue kv1 = new KeyValue(map_scrollpane.hvalueProperty(), scrollH);
        final KeyValue kv2 = new KeyValue(map_scrollpane.vvalueProperty(), scrollV);
        final KeyFrame kf  = new KeyFrame(Duration.millis(500), kv1, kv2);
        timeline.getKeyFrames().add(kf);
        timeline.play();

    }

    private void buildMap(File imgFile) throws Exception {

        if (!imgFile.exists()) {
            map_scrollpane.setContent(
                new Label("Imagen no encontrada: " + imgFile.getPath())
            );
            return;
        }

        Image img = new Image(imgFile.toURI().toString());

        mapWidth = img.getWidth();
        mapHeight = img.getHeight();

        mapPane.setPrefSize(mapWidth, mapHeight);
        mapPane.setMinSize(mapWidth, mapHeight);
        mapPane.setMaxSize(mapWidth, mapHeight);

        // Remove previous map image only
        mapPane.getChildren().removeIf(node ->
            node instanceof ImageView
        );

        ImageView iv = new ImageView(img);

        iv.setFitWidth(mapWidth);
        iv.setFitHeight(mapHeight);

        // background image ALWAYS at bottom layer
        mapPane.getChildren().add(0, iv);
    }

    private void saveAnnotation(boolean useTwoPoints){
        System.out.println(
            "Saving annotation type = " +
            annotationState.getType()
        );
        MapProjection proj = new MapProjection(
            currentRegion,
            mapWidth,
            mapHeight
        );

        Annotation ann;
        GeoPoint firstGeo = proj.unproject(annotationState.getFirstX(), annotationState.getFirstY());

        if(useTwoPoints){
            GeoPoint secondGeo = proj.unproject(annotationState.getSecondX(), annotationState.getSecondY());

            ann = new Annotation(annotationState.getType(),
                annotationState.getText(),
                annotationState.getColor(),
                2.0, List.of(firstGeo, secondGeo));
        } else {
            ann = new Annotation(
                annotationState.getType(),
                annotationState.getText(),
                annotationState.getColor(),
                2.0, List.of(firstGeo)
            );
        }

        System.out.println("ann:" + ann.getType());
        Annotation saved = app.addAnnotation(currentActivity, ann);

        if(saved != null) {
            System.out.println("saved: " + saved.getType());
            drawAnnotations(currentActivity);
        }
        System.out.println("Annotation saved correctly");
    }

     private void addPoi(double x, double y) {

        Dialog<Poi> poiDialog = new Dialog<>();
        poiDialog.setTitle("Nuevo POI");
        poiDialog.setHeaderText("Introduce un nuevo POI");

        Stage dialogStage = (Stage) poiDialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
            new Image(getClass().getResourceAsStream("/resources/logo.png"))
        );

        ButtonType okButton = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del POI");

        VBox vbox = new VBox(10, new Label("Nombre:"), nameField);
        poiDialog.getDialogPane().setContent(vbox);

        poiDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Poi(nameField.getText().trim(), x, y);
            }
            return null;
        });

        Optional<Poi> result = poiDialog.showAndWait();

        if (result.isPresent()) {
            Poi poi = result.get();

            poi.setPosition(new Point2D(x, y));

            map_listview.getItems().add(poi);

            Text text = new Text(poi.getCode());
            text.setX(x);
            text.setY(y);
            mapPane.getChildren().add(text);
        }
    }


    private void onMapRightClick(double x, double y) throws Exception{

        annotationState = new AnnotationCreationState(x, y);

        ////////////// RENDERING THE NEW ANNOTATION WINDOW //////////////
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewAnnotation.fxml"));
        Parent root = loader.load();
        NewAnnotation controller = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("/css/newAnnotationStyles.css").toExternalForm());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Create new annotation");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        //////////////////////////////////////////

        if(!controller.isAccepted()) return;

        annotationState.setType(controller.getAnnotationType());
        System.out.println("Selected type = " + controller.getAnnotationType());
        annotationState.setText(controller.getAnnotationText());
        annotationState.setColor(controller.getSelectedColor().toString());

        AnnotationType type = controller.getAnnotationType();
        if(type == AnnotationType.LINE || type == AnnotationType.CIRCLE){
            this.annotationState = annotationState;
            waitingForSecondPoint = true;

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Select the second point on the map");
            alert.show();

            return;
        } else {
            // CREATE THE ANNOTATION FOR TEXT OR POINT
//            System.out.println(annotationState.getType());
            saveAnnotation(false);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Annotation saved successfully");
            alert.show();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        MenuItem miText   = new MenuItem("📝 Añadir texto");
        MenuItem miCircle = new MenuItem("⭕ Añadir círculo");
        mapContextMenu = new ContextMenu(miText, miCircle);

               //  setCellFactory() define cómo se renderiza cada celda
        //  de forma independiente al modelo Poi.
        //  Aquí mostramos "CÓDIGO – Nombre" en cada fila.
        map_listview.setCellFactory(listView -> new ListCell<Poi>() {
            @Override
            protected void updateItem(Poi poi, boolean empty) {
                // Siempre llamar a super primero (requerido por JavaFX)
                super.updateItem(poi, empty);

                if (empty || poi == null) {
                    // Celda vacía: limpiamos texto y gráfico
                    setText(null);
                    setGraphic(null);
                } else {
                    // Mostramos código y nombre separados por un guión largo
                    setText(poi.getCode() + " – " + poi.getPosition());
                }
            }
        });

        mapPane = new Pane();

    zoomGroup = new Group();
    zoomGroup.getChildren().add(mapPane);

    contentGroup = new Group();
    contentGroup.getChildren().add(zoomGroup);

    map_scrollpane.setContent(contentGroup);

    try {

        List<Activity> activities = app.getAllActivities();

        if (!activities.isEmpty()) {

            currentActivity = activities.get(0);

            currentRegion = currentActivity.getSuggestedMap();

            buildMap(new File(currentRegion.getImagePath()));

            drawRoute(currentActivity);
            drawAnnotations(currentActivity);

            cargarDatosGrafico(currentActivity);
        }
        
        if(currentActivity == null) activityName.textProperty().set("Activity: None");
        else activityName.textProperty().set(currentActivity.getName());

    } catch (Exception ex) {
        ex.printStackTrace();
    }
    }

    public void cargarDatosGrafico(Activity actividad) {
        graficaAlturas.getData().clear();
        graficaAlturas.setLegendVisible(false);
        graficaAlturas.setCreateSymbols(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        List<TrackPoint> puntos = actividad.getTrackPoints();
        double distanciaAcumulada = 0.0;

        for (int i = 0; i < puntos.size(); i++) {
            TrackPoint puntoActual = puntos.get(i);

            if (i > 0) {
                distanciaAcumulada += puntoActual.distanceTo(puntos.get(i - 1));
            }

            series.getData().add(new XYChart.Data<>(distanciaAcumulada / 1000.0, puntoActual.getElevation()));
        }

        graficaAlturas.getData().add(series);
    }

    private void about(ActionEvent event) {
        Alert mensaje = new Alert(Alert.AlertType.INFORMATION);

        // Personalizamos el icono de la ventana del diálogo
        Stage dialogStage = (Stage) mensaje.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
            new Image(getClass().getResourceAsStream("/resources/logo.png"))
        );

        mensaje.setTitle("Acerca de");
        mensaje.setHeaderText("IPC - 2026");
        mensaje.showAndWait(); // Bloquea hasta que el usuario cierra el diálogo
    }

    private void cambiarMapa(ActionEvent event) throws IOException, Exception {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(".")); // Empezamos en el directorio del proyecto

        File imgFile = fc.showOpenDialog(imageViewer.getScene().getWindow());

        // FIX 3: showOpenDialog() devuelve null si el usuario cancela la selección
        if (imgFile != null) {
            System.out.println("Mapa seleccionado: " + imgFile.getCanonicalPath());
            buildMap(imgFile); // Reconstruimos la vista con la nueva imagen
            map_listview.getItems().clear(); // Borramos los datos del mapa anterior
        }
    }


    @FXML
    private void zoomInBtnFunction(ActionEvent event){
        if(zoomV <= 1.5){
            zoomIn(event);
            zoom(zoomV);
        }
    }

    @FXML
    private void zoomOutBtnFunction(ActionEvent event){
        if(zoomV >= 0.5){
            zoomOut(event);
            zoom(zoomV);
        }
    }

    @FXML
    private void openViewEdit(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("View-edit");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/css/userViewStyles.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.show();
    }

    private MapRegion currentRegion;

    @FXML
    private void openActivities(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListActivities.fxml"));
        Parent root = loader.load();

        ListActivitiesController controller = loader.getController();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("/css/listActivitiesStyles.css").toExternalForm());

        Stage stage = new Stage();
        stage.setTitle("Activities");
        stage.setScene(scene);
        stage.showAndWait();

        Activity selected = controller.getSelectedActivity();

        if (selected != null) {
            currentActivity = selected;
            try {
                currentRegion = currentActivity.getSuggestedMap();
                buildMap(new File(currentRegion.getImagePath()));
                drawRoute(currentActivity);
                drawAnnotations(currentActivity);
//                centerMapOnActivity(currentActivity);
                cargarDatosGrafico(currentActivity);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void openAddMap(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addMap.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Add map");
        stage.setScene(new Scene(root));

        stage.showAndWait();
    }

    @FXML

    private void speedBtnAction(ActionEvent event) {
        speedMode = !speedMode;
        if(speedMode){
            dibujarHeatmapVelocidad(app.getAllActivities().get(0));
        }else{
            //Aqui va el codigo de volver a mostrar la trace normal
            //Completar
            drawRoute(currentActivity);
        }
    }

    @FXML
    private void sessionHistory(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Session.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Session history");
        stage.setScene(new Scene(root));
        Scene scene = stage.getScene();
        scene.getStylesheets().add(getClass().getResource("/css/sessionStyles.css").toExternalForm());


        stage.showAndWait();
    }

    private Color getColorSpeed(double velocidadKmh) {
        if (velocidadKmh < 5) return Color.BLUE;
        if (velocidadKmh < 15) return Color.GREEN;
        if (velocidadKmh < 25) return Color.YELLOW;
        if (velocidadKmh < 40) return Color.ORANGE;
        return Color.RED;
    }

    public void dibujarHeatmapVelocidad(Activity actividad) {
        mapPane.getChildren().removeIf(node -> node instanceof Line);

//        MapProjection proj = new MapProjection(app.findMapForActivity(actividad), mapPane.getWidth(), mapPane.getHeight());
        MapProjection proj = new MapProjection(
            app.findMapForActivity(actividad),
            mapWidth,
            mapHeight
        );
        List<TrackPoint> puntos = actividad.getTrackPoints();

        for (int i = 1; i < puntos.size(); i++) {
            TrackPoint p1 = puntos.get(i - 1);
            TrackPoint p2 = puntos.get(i);

            double velocidad = p1.speedTo(p2);

            Point2D pix1 = proj.project(p1);
            Point2D pix2 = proj.project(p2);

            Line segmento = new Line(pix1.getX(), pix1.getY(), pix2.getX(), pix2.getY());
            System.out.println(pix1.getX()+", "+ pix1.getY());
            segmento.setStroke(getColorSpeed(velocidad));
            segmento.setStrokeWidth(3.5);
            segmento.setStrokeLineCap(StrokeLineCap.ROUND);

            mapPane.getChildren().add(segmento);
        }
    }

    @FXML
    private void signOut(ActionEvent event) throws IOException{
        SportActivityApp.getInstance().logout();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InitialScene.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Welcome");
        stage.setScene(new Scene(root));
        stage.show();
        Stage stag = (Stage) zoomGroup.getScene().getWindow();
        stag.close();
    }

    private void drawAnnotations(Activity activity) {

        mapPane.getChildren().removeIf(node ->
            node.getUserData() != null &&
            node.getUserData().equals("annotation")
        );
        
        map_listview.getItems().clear();

        MapProjection proj = new MapProjection(
            currentRegion,
            mapWidth,
            mapHeight
        );

//        List<Annotation> anns = activity.getAnnotations();


        for (Annotation ann : activity.getAnnotations()) {
            
            Poi poiFromAnn = new Poi(ann.getText() != null && !ann.getText().isEmpty() ? ann.getText() : ann.getType().toString(), ann.getGeoPoints().get(0).getLatitude(), ann.getGeoPoints().get(0).getLongitude());
            map_listview.getItems().add(poiFromAnn);
            
            System.out.println("tipo anotacion: " + ann.getType());
            switch (ann.getType()) {
                case POINT -> {

                    GeoPoint gp = ann.getGeoPoints().get(0);
                    Point2D p = proj.project(gp);

                    Circle c = new Circle(p.getX(), p.getY(), 6);
                    c.setFill(Color.web(ann.getColor()));

                    c.setUserData("annotation");

                    mapPane.getChildren().add(c);
                }
                case TEXT -> {

                    GeoPoint gp = ann.getGeoPoints().get(0);
                    Point2D p = proj.project(gp);

                    Text text = new Text(ann.getText());

                    text.setX(p.getX());
                    text.setY(p.getY());

                    text.setFill(Color.web(ann.getColor()));

                    text.setUserData("annotation");

                    mapPane.getChildren().add(text);
                }

                case LINE -> {

                    GeoPoint gp1 = ann.getGeoPoints().get(0);
                    GeoPoint gp2 = ann.getGeoPoints().get(1);

                    Point2D p1 = proj.project(gp1);
                    Point2D p2 = proj.project(gp2);

                    Line line = new Line(
                        p1.getX(), p1.getY(),
                        p2.getX(), p2.getY()
                    );

                    line.setStroke(Color.web(ann.getColor()));
                    line.setStrokeWidth(ann.getStrokeWidth());

                    line.setUserData("annotation");

                    mapPane.getChildren().add(line);
                }

                case CIRCLE -> {

                    GeoPoint center = ann.getGeoPoints().get(0);
                    GeoPoint edge = ann.getGeoPoints().get(1);

                    Point2D c = proj.project(center);
                    Point2D e = proj.project(edge);

                    double radius = c.distance(e);

                    Circle circle = new Circle(
                        c.getX(),
                        c.getY(),
                        radius
                    );

                    circle.setFill(Color.TRANSPARENT);

                    circle.setStroke(Color.web(ann.getColor()));
                    circle.setStrokeWidth(ann.getStrokeWidth());

                    circle.setUserData("annotation");

                    mapPane.getChildren().add(circle);
                }
            }
        }
    }

    private void drawRoute(Activity activity) {

        // Remove previous route drawings
        mapPane.getChildren().removeIf(node ->
            node.getUserData() != null &&
            node.getUserData().equals("route")
        );

        MapProjection proj = new MapProjection(
            currentRegion,
            mapWidth,
            mapHeight
        );

        // Route line
        Polyline route = new Polyline();

        for (TrackPoint tp : activity.getTrackPoints()) {
            Point2D p = proj.project(tp);

            route.getPoints().addAll(
                p.getX(),
                p.getY()
            );
        }

        route.setStroke(Color.DODGERBLUE);
        route.setStrokeWidth(3);

        route.setUserData("route");

        mapPane.getChildren().add(route);

        // START POINT (green)
        TrackPoint start = activity.getStartPoint();
        Point2D startP = proj.project(start);

        Circle startCircle = new Circle(
            startP.getX(),
            startP.getY(),
            8
        );

        startCircle.setFill(Color.LIMEGREEN);
        startCircle.setStroke(Color.BLACK);
        startCircle.setUserData("route");

        mapPane.getChildren().add(startCircle);

        // END POINT (red)
        TrackPoint end = activity.getEndPoint();
        Point2D endP = proj.project(end);

        Circle endCircle = new Circle(
            endP.getX(),
            endP.getY(),
            8
        );

        endCircle.setFill(Color.RED);
        endCircle.setStroke(Color.BLACK);
        endCircle.setUserData("route");

        mapPane.getChildren().add(endCircle);
        activityName.textProperty().set(currentActivity.getName());
        setStatistics();
    }
    
    private void setStatistics(){
        if (currentActivity == null) {
            distanceF.setText("none");
            durationF.setText("none");
            avgSpeedF.setText("none");
            avgPaceF.setText("none");
            elevGainF.setText("none");
            elevLossF.setText("none");
            minAltitudeF.setText("none");
            maxAltitudeF.setText("none");
        }else{
            double distance = 0;
            double maxAl = Double.MIN_VALUE;
            double minAl = Double.MAX_VALUE;

            List<TrackPoint> puntos = currentActivity.getTrackPoints();

            for (int i = 1; i < puntos.size(); i++) {
                TrackPoint p1 = puntos.get(i - 1);
                TrackPoint p2 = puntos.get(i);

                distance = p1.distanceTo(p2);
                maxAl = Double.max(maxAl,p1.getElevation());
                minAl = Double.min(minAl,p1.getElevation());
            }
            distanceF.setText(String.format("%.2f", distance).replace(',', '.'));
            durationF.setText(formatearDuration());
            avgSpeedF.setText(String.format("%.2f", currentActivity.getAverageSpeed()).replace(',', '.'));
            avgPaceF.setText(String.format("%.2f", currentActivity.getAveragePace()).replace(',', '.'));
            elevGainF.setText(String.format("%.2f", currentActivity.getElevationGain()).replace(',', '.'));
            elevLossF.setText(String.format("%.2f", currentActivity.getElevationLoss()).replace(',', '.'));
            minAltitudeF.setText(String.format("%.2f", minAl).replace(',', '.'));
            maxAltitudeF.setText(String.format("%.2f", maxAl).replace(',', '.'));
        }
        
        
    }
    
    private String formatearDuration(){
        long seconds = currentActivity.getDuration().getSeconds();
        long hours = seconds/3600;
        long minutes = (seconds%3600)/60;

        return String.format("%dh %02dmin", hours, minutes);
    }
}
