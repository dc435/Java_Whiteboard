package message;

import org.json.simple.JSONObject;

public class CanvasUpdateRequest extends Message {

    public CanvasUpdateRequest() {
        super();
    }

    public CanvasUpdateRequest(String wbName, String userName, float x, float y, String brushType, String color) {
        super();
        json.put("_wbName", wbName);
        json.put("_userName", userName);
        json.put("_x", x);
        json.put("_y", y);
        json.put("_brushType", brushType);
        json.put("_color", color);

    }

    public CanvasUpdateRequest(JSONObject json) {
        super(json);
    }

    public String getWbName() {
        return (String)json.get("_wbName");
    }

    public float getX() {
        Double x = (Double)json.get("_x");
        return x.floatValue();
    }

    public float getY() {
        Double y = (Double)json.get("_y");
        return y.floatValue();
    }

    public String getBrushType() {
        return (String)json.get("_brushType");
    }

    public String getColor() {
        return (String)json.get("_color");
    }

    public String getUserName() {
        return (String)json.get("_userName");
    }



}