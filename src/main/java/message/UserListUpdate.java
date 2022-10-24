package message;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class UserListUpdate extends Message {

    public UserListUpdate() {
        super();
    }

    public UserListUpdate(String wbName, ArrayList<String> list) {
        super();
        json.put("_wbName", wbName);
        json.put("_list", list);
    }

    public UserListUpdate(JSONObject json) {
        super(json);
    }
    public String getWbName() {
        return (String)json.get("_wbName");
    }
    public ArrayList<String> getList() {
        return (ArrayList<String>) json.get("_list");
    }
}