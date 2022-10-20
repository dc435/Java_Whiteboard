package message;

import org.json.simple.JSONObject;

public class Close extends Message {

    public Close() {
        super();
    }

    public Close(String wbName, String mgrName) {
        super();
        json.put("_wbName", wbName);
        json.put("_mgrName", mgrName);
    }

    public Close(JSONObject json) {
        super(json);
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public String getMgrName() {
        return (String)json.get("_mgrName");
    }
}