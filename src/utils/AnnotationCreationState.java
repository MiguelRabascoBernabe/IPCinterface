package utils;

/**
 *
 * @author Miguel
 */
public class AnnotationCreationState {
    private final double firstX;
    private final double firstY;
    
    private double secondX;
    private double secondY;
    
    private String type;
    private String text;
    private String color;
    
    public AnnotationCreationState(double firstX, double firstY){
        this.firstX = firstX;
        this.firstY = firstY;
    }
    
    public double getFirstX() {return firstX;}
    public double getFirstY() {return firstY;}
    
    public double getSecondX() {return secondX;}
    public double getSecondY() {return secondY;}
    public void setSecondX(double n) {this.secondX = n;}
    public void setSecondY(double n) {this.secondY = n;}
    
    public String getText(){return text;}
    public String getColor(){return color;}
    public void setText(String n){this.text = n;}
    public void setColor(String n){this.color = n;}
    
    public String getType(){return type;}
    public void setType(String n){this.type = n;}
}
