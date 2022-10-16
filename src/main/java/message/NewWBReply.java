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
//        System.out.println(json.get("_added"));
//        String added = (String) json.get("_added");
//        return Boolean.parseBoolean(added);
    }

}