package message;

import org.json.simple.JSONObject;

public class NewWBReply extends Message {

    public NewWBReply(boolean added) {
        super();
        json.put("_added", added);
    }

    public NewWBReply(JSONObject json) {
        super(json);
    }

    public boolean getAdded() {
        return (boolean) json.get("_added");
    }

}