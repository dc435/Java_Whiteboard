package whiteboard;

import java.util.ArrayList;

public class WhiteboardMgr {

    private ArrayList<User> userList;
    private String name;
    private String password;

    public WhiteboardMgr(ArrayList<User> userList, String name, String password) {
        this.userList = userList;
        this.name = name;
        this.password = password;
    }

}

