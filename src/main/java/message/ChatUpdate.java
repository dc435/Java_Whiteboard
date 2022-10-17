package message;

import org.json.simple.JSONObject;

public class ChatUpdate extends Message {

    public ChatUpdate() {
        super();
    }

    public ChatUpdate(String wbName, String userName, String chat) {
        super();
        json.put("_wbName", wbName);
        json.put("_userName", userName);
        json.put("_chat", chat);
    }

    public ChatUpdate(JSONObject json) {
        super(json);
    }

    public String getWbName() {
        return (String)json.get("_wbName");
    }

    public String getUserName() {
        return (String)json.get("_userName");
    }
    public String getChat() {
        return (String)json.get("_chat");
    }

}