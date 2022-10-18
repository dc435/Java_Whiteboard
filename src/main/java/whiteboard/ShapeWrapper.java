package whiteboard;

import java.awt.*;

public class ShapeWrapper {

    //TODO: YP
    private Shape shape;
    private String color;
    private String textPane; //FIXME: not implemented


    // Constructor
    public ShapeWrapper(Shape shape, String color) {
        this.shape = shape;
        this.color = color;
    }

    // Getters and setters
    //public String getBrush() {return this.brush;}
    //public void setBrush(String brush) {
        //this.brush = brush;
    //}
    public Shape getShape() {return this.shape;}
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public String getColor() {return this.color;}
    public void setColor(String color) {
        this.color = color;
    }

    // FIXME: I haven't figured out how to add textPane on canvas
    public String getTextPane() {return this.textPane;}
    public void setTextPane(String textPane) {
        this.textPane = textPane;
    }
}
