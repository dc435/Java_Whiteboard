package message;

import org.json.simple.JSONObject;

public class NewWBRequest extends Message {

    public NewWBRequest() {
        super();
    }

    public NewWBRequest(String mgrName, String wbName, int port) {
        super();
        json.put("_mgrName", mgrName);
        json.put("_wbName", wbName);
        json.put("_port", port);
    }

    public NewWBRequest(JSONObject json) {
        super(json);
    }

    public String getMgrName() {
        return (String)json.get("_manager");
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public int getPort() {
        Long port = (Long)json.get("_port");
        return port.intValue();
    }

}