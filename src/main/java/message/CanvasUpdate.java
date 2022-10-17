package message;

import org.json.simple.JSONObject;

public class CanvasUpdate extends Message {

    public CanvasUpdate() {
        super();
    }

    public CanvasUpdate(String wbName, String userName) {
        super();
        json.put("_wbName", wbName);
        json.put("_userName", userName);
    }

    public CanvasUpdate(JSONObject json) {
        super(json);
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public String getUserName() {
        return (String)json.get("_userName");
    }
}