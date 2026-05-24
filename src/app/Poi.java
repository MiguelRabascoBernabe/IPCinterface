 package app;
import javafx.geometry.Point2D;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jose
 */
public class Poi {
    private String code;
    private Point2D position;
    
    public Point2D getPosition() {
        return position;
    }
    
    public void setPosition(Point2D position) {
        this.position = position;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public Poi(String cod, double x, double y){
        code=cod;
        position= new Point2D(x,y);
    }

    @Override
    public String toString() {
        return  code + " [x:" +position.getX()+" y: "+position.getY()+"]" ;
    }
    
}

    

