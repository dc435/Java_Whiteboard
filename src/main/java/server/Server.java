package server;

import message.NewWhiteboard;
import whiteboard.User;
import whiteboard.WhiteboardMgr;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class Server extends Thread {

    private final String TAG = "(SERVER):";
    private HashMap<String,WhiteboardMgr> whiteboards;

    // Server class is central class for all server-side logic and variables
    // It maintains a list of all whiteboards that it manages, each of which contain their own user list
    public Server() {
        whiteboards = new HashMap<String,WhiteboardMgr>();
    }

    // Add a new whiteboard
    public boolean addWhiteboardMgr(NewWhiteboard newwb, InetSocketAddress address){

        String mgrName = newwb.getMgrName();
        String wbName = newwb.getWbName();
        User manager = new User(mgrName, true, address, true);
        ArrayList<User> userList = new ArrayList<User>();
        userList.add(manager);
        WhiteboardMgr whiteboardMgr = new WhiteboardMgr(userList, wbName);

        if (whiteboards.containsKey(wbName)) {
            System.out.println(TAG + "Could not add whiteboard. WB Name already in use.");
            return false;
        } else {
            whiteboards.put(wbName,whiteboardMgr);
            System.out.println(TAG + "Added new whiteboard: " + wbName + " at " + manager.address);
            return true;
        }
    }

    // Check if the server is managing a whiteboard of this name
    public boolean isManagingWhiteboard(String wbName) {
        return whiteboards.containsKey(wbName);
    }

    // Get the userlist for a particular whiteboard
    public ArrayList<User> getUserList(String wbName) {
        return whiteboards.get(wbName).getUserList();
    }

    // Check if there already exists a user with that name on the whiteboard
    public boolean isUserNameClash(String wbName, String userName) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        ArrayList<User> userList = whiteboardMgr.getUserList();
        boolean clash = false;
        for (User u: userList) {
            if (u.username.equals(userName)) {
                clash = true;
            }
        }
        return clash;
    }

    // Get the manager of a particular whiteboard
    public User getManager(String wbName) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        User manager = whiteboardMgr.getManager();
        return manager;
    }

    // Add a new user to a particular whiteboard
    public void addUser(String wbName, User u) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        whiteboardMgr.addUser(u);
    }

    // Get the user of a given name and whiteboard
    public User getUser(String wbName, String userName) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        User user = whiteboardMgr.getUser(userName);
        return user;
    }

    // Delete user from a whiteboard
    public void deleteUser(String wbName, String userName) {
        WhiteboardMgr whiteboardMgr = whiteboards.get(wbName);
        whiteboardMgr.deleteUser(userName);
    }

    // Check if a user is on a particular whiteboard
    public boolean checkUser(String wbName, String userName) {
        if (whiteboards.containsKey(wbName)) {
            WhiteboardMgr wbMgr = whiteboards.get(wbName);
            return wbMgr.checkUser(userName);
        } else {
            return false;
        }
    }

    // Delete an entire whiteboard
    public void deleteWhiteboard(String wbName) {
        whiteboards.remove(wbName);
    }

}
