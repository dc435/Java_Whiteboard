package whiteboard;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Wrapper class for saving whiteboards to file
 */
public class Whiteboard implements Serializable {

    private String wbName;
    private ArrayList<ShapeWrapper> graphicsFinal;

    public Whiteboard(String wbName, ArrayList<ShapeWrapper> graphicsFinal) {
        this.wbName = wbName;
        this.graphicsFinal = graphicsFinal;
    }

    public String getWbName() {
        return wbName;
    }

    public ArrayList<ShapeWrapper> getGraphicsFinal() {
        return graphicsFinal;
    }

}
