package message;

import org.json.simple.JSONObject;

public class JoinDecision extends Message {

    public JoinDecision() {
        super();
    }

    public JoinDecision(String wbName, String userName, boolean approved) {
        super();
        json.put("_wbName", wbName);
        json.put("_userName", userName);
        json.put("_approved", approved);
    }

    public JoinDecision(JSONObject json) {
        super(json);
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public String getUserName() {
        return (String)json.get("_userName");
    }

    public boolean getApproved() {
        return (boolean) json.get("_approved");
    }
}