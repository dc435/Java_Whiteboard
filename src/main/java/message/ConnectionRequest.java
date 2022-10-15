package message;

import org.json.simple.JSONObject;

public class ConnectionRequest extends Message {

    public ConnectionRequest() {
        super();
    }

    public ConnectionRequest(JSONObject json) {
        super(json);
    }

    public void setUsername(String username) {
        json.put("_username", username);
    }

    public String getUsername() {
        return (String)json.get("_username");
    }

}
