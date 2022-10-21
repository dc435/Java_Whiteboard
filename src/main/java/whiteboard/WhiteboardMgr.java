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

    public User getManager() {
        User manager = null;
        for (User u: userList) {
            if (u.manager) {
                manager = u;
            }
        }
        return manager;
    }

    public void addUser(User u) {
        userList.add(u);
    }

    public User getUser(String userName) {
        User user = null;
        for (User u: userList) {
            if (u.username.equals(userName)) {
                user = u;
            }
        }
        return user;
    }

    public void deleteUser(String userName) {
        User toDelete = null;
        for (User u: userList) {
            if (u.username.equals(userName)) {
                toDelete = u;
            }
        }
        userList.remove(toDelete);
    }

    public boolean checkUser(String userName) {
        for (User u: userList) {
            if (u.username.equals(userName)) {
                return true;
            }
        }
        return false;
    }

}

