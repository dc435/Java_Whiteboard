package message;

import org.json.simple.JSONObject;

public class JoinRequest extends Message {

    public JoinRequest() {
        super();
    }

    public JoinRequest(String wbName, String userName, int port) {
        super();
        json.put("_wbName", wbName);
        json.put("_userName", userName);
        json.put("_port", port);
    }

    public JoinRequest(JSONObject json) {
        super(json);
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public String getUserName() {
        return (String)json.get("_userName");
    }

    public int getPort() {
        Long port = (Long)json.get("_port");
        return port.intValue();
    }

}