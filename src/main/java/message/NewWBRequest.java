package message;

import org.json.simple.JSONObject;

public class NewWBRequest extends Message {

    public NewWBRequest() {
        super();
    }

    public NewWBRequest(String mgrName, String wbName, String wbPassword) {
        super();
        json.put("_mgrName", mgrName);
        json.put("_wbName", wbName);
        json.put("_wbPassword", wbPassword);
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
    public String getWbPassword() {
        return (String)json.get("_wbPassword");
    }

}