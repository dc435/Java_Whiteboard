package server;

import message.NewWBRequest;
import whiteboard.User;
import whiteboard.WhiteboardMgr;

import java.util.ArrayList;
import java.util.HashMap;

public class Server extends Thread {

    private HashMap<String,WhiteboardMgr> whiteboards;

    public Server() {
        whiteboards = new HashMap<String,WhiteboardMgr>();
    }

    public boolean addWhiteboard(NewWBRequest wbr){

        String mgrName = wbr.getMgrName();
        String wbName = wbr.getWbName();
        String wbPassword = wbr.getWbPassword();
        User manager = new User(mgrName, true);
        ArrayList<User> userList = new ArrayList<User>();
        userList.add(manager);
        WhiteboardMgr whiteboardMgr = new WhiteboardMgr(userList, wbName, wbPassword);

        if (whiteboards.containsKey(wbName)) {
            return false;
        } else {
            whiteboards.put(wbName,whiteboardMgr);
            return true;
        }

    }

}
