package whiteboard;

import java.util.ArrayList;

public class WhiteboardMgr {

    private ArrayList<User> userList;
    private String wbName;

    public WhiteboardMgr(ArrayList<User> userList, String wbName) {
        this.userList = userList;
        this.wbName = wbName;
    }

    public String getWbName() {
        return wbName;
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

}

