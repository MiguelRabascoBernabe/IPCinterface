    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
     */
    package controllers;

    import java.net.URL;
    import java.util.ResourceBundle;
    import javafx.fxml.FXML;
    import javafx.fxml.Initializable;
    import javafx.scene.Node;
    import javafx.scene.control.Button;
    import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuButton;
    import javafx.scene.control.MenuItem;
    import javafx.scene.control.TextField;

    /**
     * FXML Controller class
     *
     * @author Miguel
     */
    public class NewAnnotation implements Initializable {
        @FXML
        private MenuItem pointSelection;
        @FXML
        private MenuItem textSelection;
        @FXML
        private MenuItem lineSelection;
        @FXML
        private MenuItem circleSelection;
        @FXML
        private TextField annotationText;
        @FXML
        private ColorPicker colorSelection;
        @FXML
        private Button cancelButton;
        @FXML
        private Button acceptButton;
        @FXML
        private MenuButton visibleSelectedOption;
        
        private class MenuItemSelection {
            private MenuItem reference;
            private String name;

            public MenuItemSelection(MenuItem ref, String n){
                this.reference = ref;
                this.name = n;
            }
        }
        
        @Override
        public void initialize(URL url, ResourceBundle rb) {
            MenuItemSelection point = new MenuItemSelection(pointSelection, "POINT");    
            MenuItemSelection text = new MenuItemSelection(textSelection, "TEXT");
            MenuItemSelection line = new MenuItemSelection(lineSelection, "LINE");
            MenuItemSelection circle = new MenuItemSelection(circleSelection, "CIRCLE");
            MenuItemSelection[] selectionOptions = {point, text, line, circle};
            
            for(int i = 0; i < selectionOptions.length; i++){
                final int lol = i;
                MenuItemSelection lol2 = selectionOptions[lol];
                lol2.reference.setOnAction(event -> {
                    visibleSelectedOption.setText(lol2.name);
                });
            }
        }
    }
