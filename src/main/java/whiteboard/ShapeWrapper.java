package whiteboard;

import java.awt.*;
import java.io.Serializable;

public class ShapeWrapper implements Serializable {

    private Object shape;
    private String color;
    private boolean isText;
    private int posX;
    private int posY;


    // Constructors
    public ShapeWrapper(Shape shape, String color) {
        this.shape = shape;
        this.color = color;
        this.isText = false;
    }

    public ShapeWrapper(Object shape, String color, Boolean bool, int x, int y) {
        this.shape = shape;
        this.color = color;
        this.isText = bool;
        this.posX = x;
        this.posY = y;
    }

    public ShapeWrapper() {
        //dummy
    }

    public Object getShape() {
        return this.shape;
    }

    public String getColor() {
        return this.color;
    }

    public int getX() {
        return this.posX;
    }

    public int getY() {
        return this.posY;
    }

    public Boolean isText() {
        return this.isText;
    }
}
