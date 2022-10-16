package message;

import org.json.simple.JSONObject;

public class CanvasUpdateReply extends Message {

    public CanvasUpdateReply() {
        super();
    }

    public CanvasUpdateReply(boolean success, String message) {
        super();
        json.put("_success", success);
        json.put("_message", message);
    }

    public CanvasUpdateReply(JSONObject json) {
        super(json);
    }

    public boolean getSuccess() {
        return (boolean) json.get("_success");
    }

    public String getMessage() {
        return (String) json.get("_message");
    }


}