package message;

import org.json.simple.JSONObject;

public class Leave extends Message {

    public Leave() {
        super();
    }

    public Leave(String wbName, String userName) {
        super();
        json.put("_wbName", wbName);
        json.put("_userName", userName);
    }

    public Leave(JSONObject json) {
        super(json);
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public String getUserName() {
        return (String)json.get("_userName");
    }
}