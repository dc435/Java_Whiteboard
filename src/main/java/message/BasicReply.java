package message;

import org.json.simple.JSONObject;

public class BasicReply extends Message {

    public BasicReply() {
        super();
    }

    public BasicReply(boolean success, String message) {
        super();
        json.put("_success", success);
        json.put("_message", message);
    }

    public BasicReply(JSONObject json) {
        super(json);
    }

    public boolean getSuccess() {
        return (boolean) json.get("_success");
    }

    public String getMessage() {
        return (String) json.get("_message");
    }


}