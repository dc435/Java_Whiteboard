package whiteboard;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class ShapeWrapper implements Serializable {

    //TODO: YP
    private Object shape;
    private String color;
    private boolean isText;
    private Point2D.Float pos;


    // Constructor
    public ShapeWrapper(Shape shape, String color) {
        this.shape = shape;
        this.color = color;
        this.isText = false;
    }

    public ShapeWrapper(Object shape, String color, Boolean bool, Point2D.Float pos) {
        this.shape = shape;
        this.color = color;
        this.isText = bool;
        this.pos = pos;
    }

    // Getters and setters
    //public String getBrush() {return this.brush;}
    //public void setBrush(String brush) {
    //this.brush = brush;
    //}
    public Object getShape() {
        return this.shape;
    }

    public String getColor() {
        return this.color;
    }

    public Point2D getPoint() {
        return this.pos;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean isText() {
        return this.isText;
    }
}
