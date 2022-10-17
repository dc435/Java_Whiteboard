package server;

import message.NewWBRequest;
import whiteboard.User;
import whiteboard.WhiteboardMgr;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class Server extends Thread {

    private HashMap<String,WhiteboardMgr> whiteboards;

    public Server() {
        whiteboards = new HashMap<String,WhiteboardMgr>();
    }

    public boolean addWhiteboardMgr(NewWBRequest wbr, InetSocketAddress address){

        String mgrName = wbr.getMgrName();
        String wbName = wbr.getWbName();
        User manager = new User(mgrName, true, address, true);
        ArrayList<User> userList = new ArrayList<User>();
        userList.add(manager);
        WhiteboardMgr whiteboardMgr = new WhiteboardMgr(userList, wbName);

        if (whiteboards.containsKey(wbName)) {
            System.out.println("Could not add whiteboard. WB Name already in use.");
            return false;
        } else {
            whiteboards.put(wbName,whiteboardMgr);
            System.out.println("Added new whiteboard: " + wbName + " at " + manager.address);
            return true;
        }
    }

    public boolean isManagingWhiteboard(String wbName) {
        return whiteboards.containsKey(wbName);
    }

    public ArrayList<User> getUserList(String wbName) {
        return whiteboards.get(wbName).getUserList();
    }

    public boolean isUserNameUnique(String wbName, String userName) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        ArrayList<User> userList = whiteboardMgr.getUserList();
        boolean clash = false;
        for (User u: userList) {
            if (u.username == userName) {
                clash = true;
            }
        }
        return clash;
    }

    public User getManager(String wbName) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        User manager = whiteboardMgr.getManager();
        return manager;
    }

    public void addUser(String wbName, User u) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        whiteboardMgr.addUser(u);
    }

    public User getUser(String wbName, String userName) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        User user = whiteboardMgr.getUser(userName);
        return user;
    }

    public void deleteUser(String wbName, String userName) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        whiteboardMgr.deleteUser(userName);
    }

}
