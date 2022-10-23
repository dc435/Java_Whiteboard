package message;

import org.json.simple.JSONObject;

/**
 * Parent class for all inherited message types transmitted between clients and server
 */
public class Message {

    JSONObject json;

    public Message() {
        this.json = new JSONObject();
        json.put("_type", this.getClass().getSimpleName());
    }

    public Message(JSONObject json) {
        this.json = json;
    }

    @Override
    public String toString(){
        return json.toJSONString();
    }

    public String getType() {
        return (String) json.get("_type");
    }

}
