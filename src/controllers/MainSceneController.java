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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
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
    private BooleanProperty speedMode = new SimpleBooleanProperty(false);

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
    @FXML
    private ToggleButton speedBtn;
    @FXML
    private VBox speedLeyenda;
    
    private Circle highlightMarker;
    
    void zoomIn(ActionEvent event) {
        double sliderVal = zoomV; //zoom_slider.getValue();
        //zoom_slider.setValue(sliderVal + 0.1);
        zoomV = zoomV +0.1;
    }

    void zoomOut(ActionEvent event) {
        double sliderVal = zoomV; //zoom_slider.getValue();
        //zoom_slider.setValue(sliderVal - 0.1);
        zoomV = zoomV -0.1;
//        System.out.println(app.login("testing", "Ul12345$"));
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
        
        mapPane.setOnMouseClicked(e -> {
            if(waitingForSecondPoint && e.getButton() == MouseButton.PRIMARY){
                annotationState.setSecondX(e.getX());
                annotationState.setSecondY(e.getY());

                // CREATE ANNOTATION
                saveAnnotation(true);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(null);
                alert.setContentText("Annotation saved correctly");
                alert.show();

                waitingForSecondPoint = false;
                annotationState = null;

                return;
            }

            if (e.getButton() == MouseButton.SECONDARY) {
                try {
                    // Clic derecho → mostrar menú contextual
                    onMapRightClick(e.getX(), e.getY());
                } catch (Exception ex) {
                    System.getLogger(MainSceneController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }

            } else if (e.getButton() == MouseButton.PRIMARY && insertionMode) {
                // FIX 2: clic izquierdo en modo inserción → añadir POI y desactivar modo
                insertionMode = false;
                mapPane.setStyle(""); // Restauramos el cursor normal
                addPoi(e.getX(), e.getY());
            }
        });

        // background image ALWAYS at bottom layer
        mapPane.getChildren().add(0, iv);
    }

    private void saveAnnotation(boolean useTwoPoints){
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

        Annotation saved = app.addAnnotation(currentActivity, ann);

        if(saved != null) drawAnnotations(currentActivity);
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
            saveAnnotation(false);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Annotation saved successfully");
            alert.show();
        }
    }
    
    private Label emptyMapLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
               
        zoomV = 1.0;
        speedBtn.selectedProperty().bindBidirectional(speedMode);
        speedLeyenda.visibleProperty().bind(speedMode);
        speedLeyenda.managedProperty().bind(speedLeyenda.visibleProperty());
        
        map_listview.setCellFactory(listView -> new ListCell<Poi>() {
            @Override
            protected void updateItem(Poi poi, boolean empty) {
                super.updateItem(poi, empty);

                if (empty || poi == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    String coords = String.format(
                        "(X, Y) = (%.0f, %.0f)",
                        poi.getPosition().getX(),
                        poi.getPosition().getY()
                    );

                    setText(poi.getCode() + " - " + coords);
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
            activityName.setText("Activity: None");
            mapPane.setPrefSize(650, 300);
            showNoActivityMessage();
        } catch(Exception e){
            e.printStackTrace();
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
        javafx.application.Platform.runLater(() -> setupChartHighlight());
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
//            System.out.println("Mapa seleccionado: " + imgFile.getCanonicalPath());
            buildMap(imgFile); // Reconstruimos la vista con la nueva imagen
            map_listview.getItems().clear(); // Borramos los datos del mapa anterior
        }
    }


    @FXML
    private void zoomInBtnFunction(ActionEvent event){
        if(zoomV <= 2){
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
            mapPane.getChildren().clear();
            currentActivity = selected;
            try {
                currentRegion = currentActivity.getSuggestedMap();
                buildMap(new File(currentRegion.getImagePath()));
                drawRoute(currentActivity);
                drawAnnotations(currentActivity);
                centerMapOnActivity(currentActivity);
                cargarDatosGrafico(currentActivity);
                speedMode.setValue(false);
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
        if(speedMode.get()){
            if(currentActivity != null){
                dibujarHeatmapVelocidad();
                drawAnnotations(currentActivity);
            }
        }else{
            if(currentActivity != null){
                drawRoute(currentActivity);
                drawAnnotations(currentActivity);
            }
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

    public void dibujarHeatmapVelocidad() {
        mapPane.getChildren().removeIf(node -> 
            node.getUserData() != null && node.getUserData().equals("speed_segment")
        );

        MapProjection proj = new MapProjection(
            currentRegion,
            mapWidth,
            mapHeight
        );

        List<TrackPoint> puntos = currentActivity.getTrackPoints();

        for (int i = 1; i < puntos.size(); i++) {
            TrackPoint p1 = puntos.get(i - 1);
            TrackPoint p2 = puntos.get(i);

            Point2D pix1 = proj.project(p1);
            Point2D pix2 = proj.project(p2);

            double velocidad = p1.speedTo(p2);

            Line segmento = new Line(pix1.getX(), pix1.getY(), pix2.getX(), pix2.getY());
            //System.out.println("Heat: "+pix1.getX()+", " +pix1.getY()+", " +pix2.getX()+", "+ pix2.getY());
            segmento.setStroke(getColorSpeed(velocidad));
            segmento.setStrokeWidth(3); 
            segmento.setStrokeLineCap(StrokeLineCap.ROUND);
            segmento.setUserData("speed_segment");

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

        for (Annotation ann : activity.getAnnotations()) {
            GeoPoint gp = ann.getGeoPoints().get(0);
            Point2D projectedPoint = proj.project(gp);
            
            Poi poiFromAnn = new Poi(ann.getText() != null && !ann.getText().isEmpty() ? ann.getText() : ann.getType().toString(), projectedPoint.getX(), projectedPoint.getY());
            
            poiFromAnn.setPosition(projectedPoint);
            map_listview.getItems().add(poiFromAnn);
            
            switch (ann.getType()) {
                case POINT -> {

                    Point2D p = proj.project(gp);

                    Circle c = new Circle(p.getX(), p.getY(), 6);
                    c.setFill(Color.web(ann.getColor()));

                    c.setUserData("annotation");

                    mapPane.getChildren().add(c);
                }
                case TEXT -> {

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
            //System.out.println(p.getX()+", " +p.getY());
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
            distanceF.setText(String.format("%.2f km", currentActivity.getTotalDistance() / 1000.0).replace(',', '.'));
            durationF.setText(formatearDuration());
            avgSpeedF.setText(String.format("%.2f km/h", currentActivity.getAverageSpeed()).replace(',', '.'));
            avgPaceF.setText(String.format("%.2f min/km", currentActivity.getAveragePace()).replace(',', '.'));
            elevGainF.setText(String.format("%.2f m", currentActivity.getElevationGain()).replace(',', '.'));
            elevLossF.setText(String.format("%.2f m", currentActivity.getElevationLoss()).replace(',', '.'));
            minAltitudeF.setText(String.format("%.2f m", currentActivity.getMinElevation()).replace(',', '.'));
            maxAltitudeF.setText(String.format("%.2f m", currentActivity.getMaxElevation()).replace(',', '.'));
        }
        
        
    }
    
    private String formatearDuration(){
        long seconds = currentActivity.getDuration().getSeconds();
        long hours = seconds/3600;
        long minutes = (seconds%3600)/60;

        return String.format("%dh %02dmin", hours, minutes);
    }

    @FXML
    private void statistics(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CumulativeStatistics.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Cumulative statistics");
        stage.setScene(new Scene(root));
        Scene scene = stage.getScene();
        scene.getStylesheets().add(getClass().getResource("/css/statisticsStyles.css").toExternalForm());


        stage.showAndWait();
    }
    
    private void showNoActivityMessage() {
        mapPane.getChildren().clear();
        emptyMapLabel = new Label(
            "Welcome!\n\nLoad an activity from the \"Activities\" menu."
        );
        emptyMapLabel.getStyleClass().add("no-map-label");

        emptyMapLabel.layoutXProperty().bind(
            mapPane.widthProperty()
                .subtract(emptyMapLabel.widthProperty())
                .divide(2)
        );

        emptyMapLabel.layoutYProperty().bind(
            mapPane.heightProperty()
                .subtract(emptyMapLabel.heightProperty())
                .divide(2)
        );

        mapPane.getChildren().add(emptyMapLabel);
    }
    
    private void centerMapOnActivity(Activity activity) {
        if (activity == null || activity.getTrackPoints().isEmpty()) {
            return;
        }

        MapProjection proj = new MapProjection(
            currentRegion,
            mapWidth,
            mapHeight
        );

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (TrackPoint tp : activity.getTrackPoints()) {

            Point2D p = proj.project(tp);

            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());

            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;

        double scaledMapWidth = mapPane.getWidth() * zoomGroup.getScaleX();
        double scaledMapHeight = mapPane.getHeight() * zoomGroup.getScaleY();

        double viewW = map_scrollpane.getViewportBounds().getWidth();
        double viewH = map_scrollpane.getViewportBounds().getHeight();

        double scrollH = (centerX * zoomGroup.getScaleX() - viewW / 2)
                / (scaledMapWidth - viewW);

        double scrollV = (centerY * zoomGroup.getScaleY() - viewH / 2)
                / (scaledMapHeight - viewH);

        scrollH = Math.max(0, Math.min(1, scrollH));
        scrollV = Math.max(0, Math.min(1, scrollV));

        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }
    
    private void setupChartHighlight() {
        if (highlightMarker == null) {
            highlightMarker = new Circle(8);
            highlightMarker.setFill(Color.ORANGE);
            highlightMarker.setStroke(Color.WHITE);
            highlightMarker.setStrokeWidth(2);
            highlightMarker.setVisible(false);
        }

        javafx.scene.Node plotBackground = graficaAlturas.lookup(".chart-plot-background");
        if (plotBackground == null) return;

        plotBackground.setOnMouseMoved(e -> {
            if (currentActivity == null || currentActivity.getTrackPoints().isEmpty()) return;

            if (!mapPane.getChildren().contains(highlightMarker)) {
                mapPane.getChildren().add(highlightMarker);
            }
            highlightMarker.setVisible(true);

            double mouseX = e.getX();
            double distanciaKmTarget = graficaAlturas.getXAxis().getValueForDisplay(mouseX).doubleValue();

            List<TrackPoint> puntos = currentActivity.getTrackPoints();
            TrackPoint puntoMasCercano = puntos.get(0);
            double minimaDiferencia = Double.MAX_VALUE;
            double distanciaAcumuladaMetros = 0.0;

            for (int i = 0; i < puntos.size(); i++) {
                TrackPoint tp = puntos.get(i);
                if (i > 0) {
                    distanciaAcumuladaMetros += tp.distanceTo(puntos.get(i - 1));
                }

                double distanciaKmActual = distanciaAcumuladaMetros / 1000.0;
                double diferencia = Math.abs(distanciaKmActual - distanciaKmTarget);

                if (diferencia < minimaDiferencia) {
                    minimaDiferencia = diferencia;
                    puntoMasCercano = tp;
                }
            }

            MapProjection proj = new MapProjection(currentRegion, mapWidth, mapHeight);
            Point2D mapaPixels = proj.project(puntoMasCercano);

            highlightMarker.setCenterX(mapValue(mapaPixels.getX()));
            highlightMarker.setCenterY(mapValue(mapaPixels.getY()));
        });

        plotBackground.setOnMouseExited(e -> {
            if (highlightMarker != null) {
                highlightMarker.setVisible(false);
            }
        });
    }

    private double mapValue(double val) {
        return Double.isNaN(val) || Double.isInfinite(val) ? 0.0 : val;
    }
}
