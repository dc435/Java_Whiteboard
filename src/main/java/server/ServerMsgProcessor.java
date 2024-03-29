package server;

import message.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.ShapeWrapper;
import whiteboard.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates a new thread to process inbound connection requests to the server
 */
public class ServerMsgProcessor extends Thread {

    private final String TAG = "(SERVER MSGPROCESS):";
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private ObjectInputStream inObj;

    /**
     * Builds new thread for inbound connections to server
     * @param socket incoming socket
     * @param server Server object
     */
    public ServerMsgProcessor(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        // Process initial JSON message
        try {
            JSONParser parser = new JSONParser();
            in = new DataInputStream(socket.getInputStream());
            inObj = new ObjectInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            JSONObject js = (JSONObject) parser.parse(in.readUTF());
            processJSON(js);
        } catch (IOException | ParseException e) {
            System.out.println(TAG + "Error establishing inbound connection with client.");
            return;
        }
    }

    // Process next steps in protocol depending on message type
    private void processJSON(JSONObject js) {

        String jsType = (String) js.get("_type");
        switch(jsType) {
            case "NewWhiteboard":
                NewWhiteboard newwb = new NewWhiteboard(js);
                processNewWhiteboard(newwb);
                break;
            case "CanvasUpdate":
                CanvasUpdate canup = new CanvasUpdate(js);
                processCanvasUpdate(canup);
                break;
            case "ChatUpdate":
                ChatUpdate chatup = new ChatUpdate(js);
                processChatUpdate(chatup);
                break;
            case "JoinRequest":
                JoinRequest joinreq = new JoinRequest(js);
                processJoinRequest(joinreq);
                break;
            case "JoinDecision":
                JoinDecision joindec = new JoinDecision(js);
                processJoinDecision(joindec);
                break;
            case "Leave":
                Leave leave = new Leave(js);
                processLeave(leave);
                break;
            case "BootUser":
                BootUser btuser = new BootUser(js);
                processBootUser(btuser);
                break;
            case "Close":
                Close close = new Close(js);
                processClose(close);
                break;
        }
    }

    // Process new whiteboard request
    private void processNewWhiteboard(NewWhiteboard newwb) {
        InetSocketAddress mgrAddress = new InetSocketAddress(socket.getInetAddress(), newwb.getPort());
        boolean success = server.addWhiteboardMgr(newwb, mgrAddress);
        BasicReply brep;
        if (success) {
            brep = new BasicReply(success, "Successfully added whiteboard named " + newwb.getWbName());
            // Update user list:
            sendUserListUpdate(newwb.getWbName());
        } else {
            brep = new BasicReply(success, "Could not add whiteboard named " + newwb.getWbName() + ". Try a different name.");
        }
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error processing new whiteboard.");
        }
    }

    // Process chat update request
    private void processChatUpdate(ChatUpdate chatup) {

        // Check if server is managing a whiteboard with that name:
        if (!server.isManagingWhiteboard(chatup.getWbName())){
            BasicReply brep = new BasicReply(false, "No whiteboard named " + chatup.getWbName());
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error sending chat update. No whiteboard name "  + chatup.getWbName());
            }
            return;
        }

        // Get User list:
        ArrayList<User> userList = server.getUserList(chatup.getWbName());

        // Forward the chatup to each user in the userList (except for the original sender):
        int count = 0;
        for (User u : userList) {
            if (!u.username.equals(chatup.getUserName())) {
                ServerMsgSender sender = new ServerMsgSender(chatup, u.address);
                sender.start();
                count++;
            }
        }
        System.out.println(TAG + "Sent chat update to " + count + " other users.");

        // Send reply to original sender:
        BasicReply brep = new BasicReply(true, "Sent chat message to " + count + " other users.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error sending confirmation reply to original sender during echo.");
        }
    }

    // Process request from new user to join a whiteboard
    private void processJoinRequest(JoinRequest joinreq) {

        System.out.println(TAG + "Join Request from " + joinreq.getUserName() + " for " + joinreq.getWbName() + " received.");
        // Check if server is managing a whiteboard with that name:
        if (!server.isManagingWhiteboard(joinreq.getWbName())){
            BasicReply brep = new BasicReply(false, "No whiteboard named " + joinreq.getWbName());
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error sending reply to user during process join request.");
            }
            System.out.println(TAG + "Join request from " + joinreq.getUserName() + " for "
                    + joinreq.getWbName() + " denied. Not managing whiteboard with that name.");
            return;
        }

        //Check if username is unique to that whiteboard:
        if (server.isUserNameClash(joinreq.getWbName(), joinreq.getUserName())){
            BasicReply brep = new BasicReply(false, "Username (" + joinreq.getUserName() + ") is not unique to this whiteboard.");
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error sending name-not-unique reply to user during process join request.");
            }
            System.out.println(TAG + "Join request from " + joinreq.getUserName() + " for "
                    + joinreq.getWbName() + " denied. Username is not unique.");
            return;
        }

        //Add prospective user to user list (as not-approved):
        InetSocketAddress userAddress = new InetSocketAddress(socket.getInetAddress(), joinreq.getPort());
        User pendingUser = new User(joinreq.getUserName(), false, userAddress, false);
        server.addUser(joinreq.getWbName(), pendingUser);

        //Forward JoinReq to manager:
        User manager = server.getManager(joinreq.getWbName());
        ServerMsgSender sender = new ServerMsgSender(joinreq, manager.address);
        sender.start();
        System.out.println(TAG + "Join Request for " + joinreq.getWbName() + " sent to manager " + manager.username);

        //Send reply to original sender:
        BasicReply brep = new BasicReply(true, "Join Request sent to " + joinreq.getWbName() + " manager. Request pending.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error send confirmation reply to user following process join request.");
        }

    }

    // Process a manager decision in reply to a new user join request
    private void processJoinDecision(JoinDecision joindec) {

        User otherUser;

        //Check if otherUser is still on wb manager list:
        if (server.checkUser(joindec.getWbName(), joindec.getUserName())) {
            otherUser = server.getUser(joindec.getWbName(), joindec.getUserName());
        } else {
            BasicReply brep = new BasicReply(false, "Cannot process join decision. User or whiteboard no longer available.");
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error sending reply process join decision.");
            }
            return;
        }

        // Check if the manager's decision was approved / not, and treat accordingly
        if (joindec.getApproved()) {

            otherUser.approved = true;
            ArrayList<ShapeWrapper> graphics = null;
            try {
                graphics = (ArrayList<ShapeWrapper>) inObj.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(TAG + "Error reading in graphics during approved join decision.");
            }
            //Forward JoinDec and GraphicArrayList to otherUser:
            ServerMsgSender sender = new ServerMsgSender(joindec, otherUser.address, graphics);
            sender.start();
            // Send reply to original sender:
            BasicReply brep = new BasicReply(true, "Sent join approval and full canvas to user " + joindec.getUserName());
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error writing confirmation reply during approved join decision.");
            }
            // Update user list for all users:
            sendUserListUpdate(joindec.getWbName());

        // If manager did not approve join request:
        } else {

            otherUser.approved = false;
            //Forward JoinDec (only) to otherUser:
            ServerMsgSender sender = new ServerMsgSender(joindec, otherUser.address);
            sender.start();
            // Send reply to original sender:
            BasicReply brep = new BasicReply(true, "Join denial ackowledged and sent to user " + joindec.getUserName());
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error sending join denial acknowledgement during denied join decision.");
            }
            //Delete user from whiteboard manager:
            server.deleteUser(joindec.getWbName(), joindec.getUserName());
        }

    }

    // Process a canvas update from any user
    private void processCanvasUpdate(CanvasUpdate canup) {

        //Get graphics object containing full canvas from input stream
        ArrayList<ShapeWrapper> graphics = null;
        try {
            graphics = (ArrayList<ShapeWrapper>) inObj.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(TAG + "Error reading in graphics during canvas update.");
        }

        // Check if server has record of wb, if so, process:
        if (server.isManagingWhiteboard(canup.getWbName())) {

            // Get User list for this whiteboard
            ArrayList<User> userList = server.getUserList(canup.getWbName());

            // Forward the canvas update and graphics to each user in the userList (except for the original sender):
            int count = 0;
            for (User u : userList) {
                if (!u.username.equals(canup.getUserName())) {
                    ServerMsgSender sender = new ServerMsgSender(canup, u.address, graphics);
                    sender.start();
                    count++;
                }
            }
            System.out.println(TAG + "Sent canvas update to " + count + " other users.");

            // Send reply to original sender:
            BasicReply brep = new BasicReply(true, "Sent canvas update to " + count + " other users.");
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error sending confirmation reply during canvas update.");
            }
        } else {
            // Send reply to original sender:
            BasicReply brep = new BasicReply(false, "Could not process canvas update for " + canup.getWbName() + ". Not managing wb.");
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error sending negative reply during canvas update.");
            }

        }

    }

    // Process leave notification from a user
    private void processLeave(Leave leave) {
        BasicReply brep;
        if (server.checkUser(leave.getWbName(), leave.getUserName())) {
            server.deleteUser(leave.getWbName(), leave.getUserName());
            brep = new BasicReply(true, "You have been removed from " + leave.getWbName() + ".");
            User manager = server.getManager(leave.getWbName());
            ServerMsgSender sender = new ServerMsgSender(leave, manager.address);
            sender.start();
            // Update user list for all users:
            sendUserListUpdate(leave.getWbName());
        } else {
            brep = new BasicReply(false, "Error removing user from " + leave.getWbName() + ".");
        }
        //Send reply to original user:
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error sending error message to user during leave processing.");
        }

    }

    // Process a boot user message from a manager
    private void processBootUser(BootUser btuser) {

        BootUserReply btuserrep;

        // Check if manager still managing wb, then action:
        if (server.checkUser(btuser.getWbName(), btuser.getMgrName())) {
            User manager = server.getManager(btuser.getWbName());
            // Check if mgr has authority, then action boot
            if (!manager.username.equals(btuser.getMgrName())) {
                btuserrep = new BootUserReply(false, btuser.getUserName());
                System.out.println(TAG + "Could not boot user " + btuser.getUserName() + ". No authority.");
            }
            // Check if user exists on this wb
            else if (!server.checkUser(btuser.getWbName(), btuser.getUserName())) {
                btuserrep = new BootUserReply(false, btuser.getUserName());
                System.out.println(TAG + "Could not boot user " + btuser.getUserName() + ". User not on list.");
            // Check if manager booting themselves
            } else if (server.getManager(btuser.getWbName()).username.equals(btuser.getUserName())) {
                btuserrep = new BootUserReply(false, btuser.getUserName());
                System.out.println(TAG + "Could not boot user " + btuser.getUserName() + ". Manager cannot boot themself.");
            }
            // If the do exist, delete user
            else {
                User bootedUser = server.getUser(btuser.getWbName(), btuser.getUserName());
                ServerMsgSender sender = new ServerMsgSender(btuser, bootedUser.address);
                sender.start();
                server.deleteUser(btuser.getWbName(), btuser.getUserName());
                btuserrep = new BootUserReply(true, btuser.getUserName());
                System.out.println(TAG + "Booted user " + btuser.getUserName() + " from " + btuser.getWbName());
            }
            // Update user list for all users:
            sendUserListUpdate(btuser.getWbName());
        } else {
            btuserrep = new BootUserReply(false, btuser.getUserName());
        }

        //Send boot user reply to manager:
        try {
            out.writeUTF(btuserrep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error sending boot user reply to manager.");
        }
    }

    // Process a close notification from manager
    private void processClose(Close close) {

        BasicReply brep;

        if (server.isManagingWhiteboard(close.getWbName())) {
            // Get User list:
            ArrayList<User> userList = server.getUserList(close.getWbName());

            // Forward the close to each user in the userList (except for the manager)
            int count = 0;
            for (User u : userList) {
                if (!u.username.equals(close.getMgrName())) {
                    ServerMsgSender sender = new ServerMsgSender(close, u.address);
                    sender.start();
                    count++;
                }
            }
            System.out.println(TAG + "Sent close notification to " + count + " other users.");

            //Delete whiteboard from whiteboards
            server.deleteWhiteboard(close.getWbName());
            System.out.println(TAG + "Whiteboard " + close.getWbName() + " closed.");

            //Set reply for original sender
            brep = new BasicReply(true, "Close notification sent to " + count + " other users.");

        } else {
            brep = new BasicReply(false, "Server not managing whiteboard name " + close.getWbName());
            System.out.println(TAG + "Received close request for " + close.getWbName() + ". Could not close. Server not managing that whiteboard.");

        }

        // Send reply to original sender
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error send confirmation of close.");
        }
    }

    private void sendUserListUpdate(String wbName) {
        //Build user list update:
        ArrayList<String> simpleUserList = server.getSimpleUserList(wbName);
        UserListUpdate ulup = new UserListUpdate(wbName, simpleUserList);

        // Forward UserListUpdate to each user on the wb:
        ArrayList<User> userList = server.getUserList(wbName);
        int count = 0;
        for (User u : userList) {
            ServerMsgSender sender = new ServerMsgSender(ulup, u.address);
            sender.start();
            count++;
        }
        System.out.println(TAG + "Sent user list update to " + count + " users.");

    }
}
