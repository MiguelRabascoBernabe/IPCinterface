/*
 * ============================================================
 *  PROYECTO EJEMPLO – IPC 2026
 *  Asignatura: Interfaces Persona-Computador
 *  Universitat Politècnica de València
 * ============================================================
 *
 *  DESCRIPCIÓN GENERAL
 *  -------------------
 *  Este controlador gestiona la vista principal de la aplicación
 *  de puntos de interés (POI) sobre un mapa.
 *
 *  Funcionalidades implementadas:
 *   1. Carga y visualización de una imagen de mapa.
 *   2. Zoom interactivo mediante un Slider.
 *   3. Añadir POIs (texto) y anotaciones (círculos) con clic derecho.
 *   4. Listado de POIs en un ListView con CellFactory personalizada.
 *   5. Centrado animado del mapa al seleccionar un POI de la lista.
 *   6. Modo inserción: activar con botón y colocar POI con siguiente clic.
 *
 *  PATRÓN UTILIZADO: MVC (Model-View-Controller)
 *   - Modelo : clase Poi  (datos del punto de interés)
 *   - Vista  : FXMLDocument.fxml  (layout declarativo)
 *   - Control: esta clase (lógica de interacción)
 *
 * ============================================================
 */
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Modality;
import javafx.stage.Stage;
import upv.ipc.sportlib.*;
import utils.AnnotationCreationState;

/**
 * Controlador principal de la aplicación de mapa con POIs.
 *
 * La anotación @FXML conecta automáticamente los campos de esta clase
 * con los elementos declarados en el fichero FXML mediante su atributo fx:id.
 *
 * Implementa {@link Initializable} para poder ejecutar código de
 * inicialización una vez que el FXML ha sido cargado completamente.
 */
public class MainSceneController implements Initializable {

    // =========================================================
    //  ESTRUCTURA DE NODOS PARA ZOOM
    // =========================================================
    //
    //  El zoom se consigue escalando un Group (zoomGroup).
    //  Escalar un Group NO desplaza los nodos que contiene,
    //  lo que evita el "salto" visual al hacer zoom.
    //
    //  Jerarquía de nodos:
    //
    //  ScrollPane (map_scrollpane)
    //   └─ contentGroup          ← Group raíz dentro del ScrollPane
    //       └─ zoomGroup         ← se escala para el zoom
    //           └─ mapPane       ← Pane con la imagen y los POIs
    //               ├─ ImageView ← imagen del mapa
    //               ├─ Text      ← etiquetas de POIs
    //               └─ Circle    ← anotaciones circulares
    //
    // =========================================================

    SportActivityApp app = SportActivityApp.getInstance();

    /** Group que se escala para aplicar el zoom. */
    @FXML
    private Group zoomGroup;

    /**
     * Pane que actúa como lienzo del mapa.
     * Contiene la imagen de fondo y todos los elementos superpuestos
     * (textos, círculos, etc.). Sus dimensiones coinciden con las de
     * la imagen cargada.
     */
    @FXML
    private Pane mapPane;


    /** Menú contextual reutilizable para el clic derecho sobre el mapa. */
    private ContextMenu mapContextMenu;


    /**
     * Indica si el controlador está en modo inserción de POI.
     * {@code true} → el próximo clic izquierdo sobre el mapa abre el diálogo.
     */
    private boolean insertionMode = false;

    // =========================================================
    //  ELEMENTOS FXML  (inyectados automáticamente por el cargador)
    // =========================================================

    /** Lista lateral que muestra todos los POIs añadidos al mapa. */
    @FXML
    private ListView<Poi> map_listview;

    /** ScrollPane que envuelve el mapa y permite desplazarlo. */
    @FXML
    private ScrollPane map_scrollpane;

    /**
     * Slider de zoom.
     * Rango: [0.5 – 1.5]. Valor inicial: 1.0 (sin zoom).
     * Cada cambio de valor llama al método zoom().
     */
    //@FXML
    //private Slider zoom_slider;
    private double zoomV;

    /**
     * Botón de pin visible sobre el mapa.
     * Se desplaza hasta la posición del POI seleccionado en la lista.
     */
    private MenuButton map_pin;

    // FIX 5 — Eliminadas las variables sin uso:
    //   · 'mousePosistion' (errata + duplicado de mousePosition)
    //   · 'pin_info'       (inyectada pero nunca actualizada)

    /** Etiqueta en la barra de estado que muestra las coordenadas del ratón. */
    //private Label mousePosition;
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


    // =========================================================
    //  MANEJADORES DE ZOOM
    // =========================================================

    /**
     * Aumenta el zoom en 0.1 unidades al pulsar el botón "+".
     *
     * @param event evento de acción del botón
     */
    void zoomIn(ActionEvent event) {
        double sliderVal = zoomV; //zoom_slider.getValue();
        //zoom_slider.setValue(sliderVal + 0.1);
        zoomV = zoomV +0.1;
    }

    /**
     * Reduce el zoom en 0.1 unidades al pulsar el botón "–".
     *
     * @param event evento de acción del botón
     */
    void zoomOut(ActionEvent event) {
        double sliderVal = zoomV; //zoom_slider.getValue();
        //zoom_slider.setValue(sliderVal - 0.1);
        zoomV = zoomV -0.1;
        //System.out.println(app.registerUser("testing","test@tung.sahur", "Ul12345$", LocalDate.MIN, "/src/resources/logo.png"));
        System.out.println(app.login("testing", "Ul12345$"));
    }

    /**
     * Aplica el factor de escala al {@code zoomGroup}.
     *
     * Este método es invocado automáticamente cada vez que cambia el
     * valor del slider, gracias al listener registrado en {@link #initialize}.
     *
     * Truco: guardamos y restauramos los valores de scroll para que el
     * contenido visible no salte al cambiar la escala.
     *
     * @param scaleValue nuevo factor de escala (p. ej. 1.2 → 120 %)
     */
    private void zoom(double scaleValue) {
        // Guardamos la posición del scroll antes de escalar
        double scrollH = map_scrollpane.getHvalue();
        double scrollV = map_scrollpane.getVvalue();

        // Aplicamos el zoom escalando el Group en ambos ejes
        zoomGroup.setScaleX(scaleValue);
        zoomGroup.setScaleY(scaleValue);

        // Restauramos la posición del scroll para que el centro visual
        // permanezca estable durante el zoom
        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }

    // =========================================================
    //  SELECCIÓN EN EL LISTVIEW → CENTRADO EN EL MAPA
    // =========================================================

    /**
     * Se ejecuta cuando el usuario hace clic en un elemento del ListView.
     *
     * Objetivo: centrar el ScrollPane sobre la posición del POI seleccionado
     * con una animación suave de 500 ms, y mover el pin al punto.
     *
     * Cálculo del scroll
     * ------------------
     * El ScrollPane expresa su posición como valores normalizados [0, 1]:
     *   · hValue = 0 → extremo izquierdo
     *   · hValue = 1 → extremo derecho
     *
     * Para centrar el POI necesitamos:
     *
     *   scrollH = (poiX_escalado - viewportAncho / 2)
     *             ─────────────────────────────────────
     *             (mapaAncho_escalado - viewportAncho)
     *
     * Aplicamos clamp para no salir del rango [0, 1].
     *
     * @param event evento de ratón sobre el ListView
     */
    @FXML
    void listClicked(MouseEvent event) {
        // Obtenemos el POI seleccionado; si no hay ninguno, salimos
        Poi itemSelected = map_listview.getSelectionModel().getSelectedItem();
        if (itemSelected == null) return;

        // ── Dimensiones del mapa con el zoom actual aplicado ──────────
        double mapWidth  = mapPane.getWidth()  * zoomGroup.getScaleX();
        double mapHeight = mapPane.getHeight() * zoomGroup.getScaleY();

        // ── Posición del POI escalada ──────────────────────────────────
        // getPosition() devuelve las coordenadas en el sistema local del
        // mapPane (sin zoom). Las multiplicamos por el factor de escala
        // para obtener la posición real en pantalla.
        double poiX = itemSelected.getPosition().getX() * zoomGroup.getScaleX();
        double poiY = itemSelected.getPosition().getY() * zoomGroup.getScaleY();

        // ── Tamaño visible del ScrollPane (viewport) ───────────────────
        double viewW = map_scrollpane.getViewportBounds().getWidth();
        double viewH = map_scrollpane.getViewportBounds().getHeight();

        // ── Cálculo del scroll normalizado [0, 1] ─────────────────────
        // Restamos la mitad del viewport para que el POI quede centrado
        // y no en la esquina superior-izquierda del área visible.
        double scrollH = (poiX - viewW / 2) / (mapWidth  - viewW);
        double scrollV = (poiY - viewH / 2) / (mapHeight - viewH);

        // Garantizamos que el valor esté dentro del rango válido [0, 1]
        scrollH = Math.max(0, Math.min(1, scrollH));
        scrollV = Math.max(0, Math.min(1, scrollV));

        // ── Animación suave con Timeline ──────────────────────────────
        // Timeline interpola los valores de las propiedades a lo largo
        // del tiempo. KeyValue define qué propiedad animar y hasta qué
        // valor; KeyFrame define en qué instante se alcanza ese valor.
        final Timeline timeline = new Timeline();
        final KeyValue kv1 = new KeyValue(map_scrollpane.hvalueProperty(), scrollH);
        final KeyValue kv2 = new KeyValue(map_scrollpane.vvalueProperty(), scrollV);
        final KeyFrame kf  = new KeyFrame(Duration.millis(500), kv1, kv2);
        timeline.getKeyFrames().add(kf);
        timeline.play(); // Inicia la animación (no bloquea el hilo de la UI)

    }

    // =========================================================
    //  CONSTRUCCIÓN DEL MAPA
    // =========================================================

    /**
     * Carga una imagen y construye la jerarquía de nodos del mapa.
     *
     * Este método puede llamarse varias veces (p. ej. al cambiar el mapa),
     * ya que sustituye completamente el contenido del ScrollPane.
     *
     * @param imgFile fichero de imagen a cargar como fondo del mapa
     */
    private void buildMap(File imgFile) throws Exception{
        // Comprobación defensiva: si el fichero no existe mostramos un aviso
        if (!imgFile.exists()) {
            map_scrollpane.setContent(
                new Label("Imagen no encontrada: " + imgFile.getPath()));
            return;
        }

        // Cargamos la imagen y obtenemos sus dimensiones reales en píxeles
        Image img = new Image(imgFile.toURI().toString());
        double W = img.getWidth();
        double H = img.getHeight();

        // ── mapPane: lienzo del mapa ───────────────────────────────────
        // Usamos un Pane (y no un Group) para poder posicionar los nodos
        // hijos con coordenadas absolutas (setLayoutX / setLayoutY).
        mapPane = new Pane();
        mapPane.setPrefSize(W, H); // tamaño preferido = tamaño de la imagen
        mapPane.setMinSize(W, H);  // impedimos que el layout lo encoja
        mapPane.setMaxSize(W, H);  // impedimos que el layout lo agrande

        // Añadimos la imagen como fondo del Pane
        ImageView iv = new ImageView(img);
        iv.setFitWidth(W);
        iv.setFitHeight(H);
        mapPane.getChildren().add(iv);

        // ── Manejador de clics sobre el mapa ──────────────────────────
        // Gestionamos el clic derecho (menú contextual) y el clic izquierdo
        // en modo inserción (FIX 2).
        mapPane.setOnMouseClicked(e -> {
            if(waitingForSecondPoint && e.getButton() == MouseButton.PRIMARY){
                annotationState.setSecondX(e.getX());
                annotationState.setSecondY(e.getY());

                // CREATE ANNOTATION
                saveAnnotation(true);
//                System.out.println("creating annotation for line or circle");
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

        // ── Jerarquía de Groups para el zoom ──────────────────────────
        // contentGroup es el nodo raíz que recibe el ScrollPane.
        // zoomGroup es el que se escala; anidar un Group dentro de otro
        // evita que el ScrollPane reajuste su contenido durante el escalado.
        zoomGroup = new Group();
        Group contentGroup = new Group();
        zoomGroup.getChildren().add(mapPane);
        contentGroup.getChildren().add(zoomGroup);

        // Aplicamos el zoom actual (valor actual del slider)
        double zoom = zoomV;
        zoomGroup.setScaleX(zoom);
        zoomGroup.setScaleY(zoom);

        // Asignamos el contentGroup como contenido del ScrollPane
        map_scrollpane.setContent(contentGroup);

    }
    
    private void saveAnnotation(boolean useTwoPoints){
        System.out.println(
            "Saving annotation type = " +
            annotationState.getType()
        );
        MapProjection proj = new MapProjection(
            app.findMapForActivity(currentActivity),
            mapPane.getWidth(),
            mapPane.getHeight()
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

        // ── Construcción del diálogo personalizado ────────────────────
        Dialog<Poi> poiDialog = new Dialog<>();
        poiDialog.setTitle("Nuevo POI");
        poiDialog.setHeaderText("Introduce un nuevo POI");

        // Personalizamos el icono de la ventana del diálogo
        Stage dialogStage = (Stage) poiDialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
            new Image(getClass().getResourceAsStream("/resources/logo.png"))
        );

        // Botones del diálogo: Aceptar y Cancelar
        ButtonType okButton = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        // Campo de texto para el nombre del POI
        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del POI");

        // Layout del contenido del diálogo (VBox con espaciado de 10 px)
        VBox vbox = new VBox(10, new Label("Nombre:"), nameField);
        poiDialog.getDialogPane().setContent(vbox);

        // ResultConverter: transforma la selección del botón en un objeto Poi.
        // FIX 1: ya no usamos coordenadas provisionales (0,0); pasamos (x,y)
        // directamente al constructor para que el modelo sea coherente desde el inicio.
        poiDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Poi(nameField.getText().trim(), x, y);
            }
            return null;
        });

        // Mostramos el diálogo y esperamos la respuesta del usuario
        Optional<Poi> result = poiDialog.showAndWait();

        if (result.isPresent()) {
            Poi poi = result.get();

            // FIX 1: confirmamos la posición como Point2D para compatibilidad
            // con getPosition(), usando las mismas coordenadas (x, y).
            poi.setPosition(new Point2D(x, y));

            // Añadimos el POI al ListView (la CellFactory mostrará nombre y código)
            map_listview.getItems().add(poi);

            // FIX 1: usamos (x, y) tanto para el modelo como para el Text,
            // garantizando que la etiqueta aparezca exactamente donde se hizo clic.
            Text text = new Text(poi.getCode());
            text.setX(x);
            text.setY(y);
            mapPane.getChildren().add(text);
        }
    }

    // =========================================================
    //  MENÚ CONTEXTUAL (clic derecho sobre el mapa)
    // =========================================================

    /**
     * Muestra el menú contextual reutilizable en la posición del clic.
     *
     * Las acciones de los MenuItem se actualizan con las coordenadas
     * del clic actual antes de mostrar el menú.
     *
     * @param x coordenada X del clic en el sistema local del mapPane
     * @param y coordenada Y del clic en el sistema local del mapPane
     */
    private void onMapRightClick(double x, double y) throws Exception{
        // FIX 6: cerramos el menú si ya estaba visible (evita instancias flotantes)
//        mapContextMenu.hide();
//
//        // Actualizamos las acciones de los items con las coordenadas actuales.
//        // Usamos variables final para que el lambda pueda capturarlas.
//        final double clickX = x;
//        final double clickY = y;
//        mapContextMenu.getItems().get(0).setOnAction(e -> addPoi(clickX, clickY));
//        mapContextMenu.getItems().get(1).setOnAction(e -> addCircle(clickX, clickY));
//
//        // Mostramos el menú en coordenadas de pantalla
//        mapContextMenu.show(
//            mapPane.getScene().getWindow(),
//            mapPane.localToScreen(x, y).getX(),
//            mapPane.localToScreen(x, y).getY()
//        );

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

    // =========================================================
    //  INICIALIZACIÓN DEL CONTROLADOR
    // =========================================================

    /**
     * Método llamado automáticamente por el FXMLLoader tras inyectar
     * todos los elementos {@code @FXML}.
     *
     * Aquí configuramos:
     *  - El slider de zoom y su listener.
     *  - El ContextMenu reutilizable (FIX 6).
     *  - La CellFactory del ListView (FIX 4).
     *  - La carga del mapa inicial.
     *
     * @param url  URL del documento FXML (no usado aquí)
     * @param rb   paquete de recursos de internacionalización (no usado aquí)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // ── Configuración del slider de zoom ──────────────────────────
        //zoom_slider.setMin(0.5);   // zoom mínimo: 50 %
        //zoom_slider.setMax(1.5);   // zoom máximo: 150 %
        //zoom_slider.setValue(1.0); // valor inicial: 100 %
        zoomV = 1.0;

        // Listener que invoca zoom() cada vez que el slider cambia de valor.
        // Usamos una expresión lambda en lugar de una clase anónima por brevedad.
        //zoom_slider.valueProperty().addListener(
        //    (observable, oldVal, newVal) -> zoom((Double) newVal)
        //);

        // Los items se crean aquí sin acción; las acciones se asignan
        // en onMapRightClick() con las coordenadas correctas de cada clic.
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

        // ── Carga del mapa inicial ─────────────────────────────────────
        // El fichero se busca relativo al directorio de trabajo del proyecto.
        //Se ha comentado la linea de abajo porque hay que buildear el mapa de la actividad cargada
        //borrar o ver que hacer
        //buildMap(new File("src/resources/upv.jpg"));
        try {
            // ── Carga del mapa inicial ─────────────────────────────────────
            // El fichero se busca relativo al directorio de trabajo del proyecto.
            buildMap(new File("src/resources/upv.jpg"));
        } catch (Exception ex) {
            System.getLogger(MainSceneController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        //Parte provisional para empezar lo de la grafica de altura
        //Necesita linkearse con la funcionalidad de seleccionar actividad, de momento cogemos la primera actividad
        List<Activity> activities = app.getAllActivities();

        if (!activities.isEmpty()) {
            currentActivity = activities.get(0);
            cargarDatosGrafico(currentActivity);
            drawAnnotations(currentActivity);
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

    // =========================================================
    //  DIÁLOGO "ACERCA DE"
    // =========================================================

    /**
     * Muestra un diálogo informativo con datos de la asignatura.
     *
     * Nota: accedemos al Stage del diálogo para poder personalizar
     * su icono, ya que Alert no expone directamente esa propiedad.
     *
     * @param event evento de acción del menú
     */
    @FXML
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

    // =========================================================
    //  CAMBIAR EL MAPA (selector de fichero)
    // =========================================================

    /**
     * Abre un selector de fichero para que el usuario elija una imagen
     * diferente como mapa y reconstruye toda la vista.
     *
     * FIX 3: se comprueba que imgFile no sea null antes de usarlo,
     * evitando NullPointerException cuando el usuario cierra el FileChooser
     * sin seleccionar ningún fichero.
     *
     * @param event evento de acción del menú
     * @throws IOException si hay un problema al obtener la ruta canónica
     */
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
            getClass().getResource("/css/login.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void openActivities(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewActivity.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("/css/newActivityStyles.css").toExternalForm());

        Stage stage = new Stage();
        stage.setTitle("Activities");
        stage.setScene(scene);
        stage.showAndWait();
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
    private void speedBtnAction(ActionEvent event)throws IOException{
        
    }

    @FXML
    private void sessionHistory(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Session.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Add map");
        stage.setScene(new Scene(root));
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

        MapProjection proj = new MapProjection(app.findMapForActivity(actividad), mapPane.getWidth(), mapPane.getHeight());
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
    
    // Method created with the help of AI
    private void drawAnnotations(Activity activity) {

        // remove old annotation drawings
        mapPane.getChildren().removeIf(node ->
            node.getUserData() != null &&
            node.getUserData().equals("annotation")
        );

        MapProjection proj = new MapProjection(
            app.findMapForActivity(activity),
            mapPane.getWidth(),
            mapPane.getHeight()
        );
        
//        List<Annotation> anns = activity.getAnnotations();

        for (Annotation ann : activity.getAnnotations()) {
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
//
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
}
