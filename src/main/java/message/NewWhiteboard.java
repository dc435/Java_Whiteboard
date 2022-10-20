package message;

import org.json.simple.JSONObject;

public class NewWhiteboard extends Message {

    public NewWhiteboard() {
        super();
    }

    public NewWhiteboard(String mgrName, String wbName, int port) {
        super();
        json.put("_mgrName", mgrName);
        json.put("_wbName", wbName);
        json.put("_port", port);
    }

    public NewWhiteboard(JSONObject json) {
        super(json);
    }

    public String getMgrName() {
        return (String)json.get("_mgrName");
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public int getPort() {
        Long port = (Long)json.get("_port");
        return port.intValue();
    }

}