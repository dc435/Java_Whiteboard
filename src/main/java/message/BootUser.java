package message;

import org.json.simple.JSONObject;

public class BootUser extends Message {

    public BootUser() {
        super();
    }

    public BootUser(String wbName, String mgrName, String userName) {
        super();
        json.put("_wbName", wbName);
        json.put("_mgrName", mgrName);
        json.put("_userName", userName);
    }

    public BootUser(JSONObject json) {
        super(json);
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public String getMgrName() {
        return (String)json.get("_mgrName");
    }
    public String getUserName() {
        return (String)json.get("_userName");
    }
}