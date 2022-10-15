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

    public String getAdded() {
        return (String)json.get("_added");
    }

}