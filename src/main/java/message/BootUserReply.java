package message;

import org.json.simple.JSONObject;

public class BootUserReply extends Message {

    public BootUserReply() {
        super();
    }

    public BootUserReply(boolean success, String userName) {
        super();
        json.put("_success", success);
        json.put("_userName", userName);
    }

    public BootUserReply(JSONObject json) {
        super(json);
    }

    public boolean getSuccess() {
        return (boolean) json.get("_success");
    }

    public String getUserName() {
        return (String) json.get("_userName");
    }


}