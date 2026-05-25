package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import upv.ipc.sportlib.AnnotationType;

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
    @FXML
    private Text errorText;
    @FXML
    private HBox errorBox;

    private class MenuItemSelection {
        private MenuItem reference;
        private String name;

        public MenuItemSelection(MenuItem ref, String n){
            this.reference = ref;
            this.name = n;
        }
    }
    
    private boolean accepted = false;
    
    public boolean isAccepted(){ return accepted;}
    public AnnotationType getAnnotationType() {
        String lol = visibleSelectedOption.getText();
        if(lol.equals("POINT")) return AnnotationType.POINT;
        else if(lol.equals("TEXT")) return AnnotationType.TEXT;
        else if(lol.equals("LINE")) return AnnotationType.LINE;
        else return AnnotationType.CIRCLE;
    }
    public String getAnnotationText() {return annotationText.getText();}
    public Color getSelectedColor() {return colorSelection.getValue();}
    
    private BooleanProperty validAnnotation;
    private ChangeListener<String> textListener;
    
    private void checkValidation(){
        boolean valid = (!annotationText.getText().equals("") && visibleSelectedOption.getText().equals("TEXT")) || !visibleSelectedOption.getText().equals("TEXT");
        validAnnotation.set(valid);
        
        showError(valid, errorBox, errorText);
    }
    
    private void showError(boolean isValid, Node field, Text errorMessage){
        field.setVisible(!isValid);
        errorMessage.setVisible(!isValid);
        errorMessage.setText("Text annotations must have a piece of text associated with it");
        field.setStyle((isValid ? "" : "-fx-background-color: #fce5e0"));
        acceptButton.setDisable(!isValid);
    }
    
    private void buttonAction(boolean value, ActionEvent e){
        accepted = value;
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ///////////// MISC INITIALIZATIONS /////////////
        cancelButton.setOnAction(e -> buttonAction(false, e));
        acceptButton.setOnAction(e -> buttonAction(true, e));
        errorBox.setVisible(false);
        
        ///////////// MENU SELECTION LOGIC ///////////// 
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
        
        visibleSelectedOption.setText("CIRCLE");
        
        ///////////// VALIDATION FOR TEXT IN SOME CASES ///////////// 
        
        validAnnotation = new SimpleBooleanProperty();
        validAnnotation.setValue(Boolean.FALSE);
        
        annotationText.focusedProperty().addListener((obs, old, n) -> {
            if(!n && visibleSelectedOption.getText().equals("TEXT")){
                checkValidation();
            
                if(!validAnnotation.get()){
                    if(textListener == null){
                        textListener = (a, b, c) -> checkValidation();
                        annotationText.textProperty().addListener(textListener);
                        visibleSelectedOption.textProperty().addListener(textListener);
                    }
                }
            }
        });
        
        ///////////// ///////////// ///////////// 
    }
}
