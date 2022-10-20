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

public class ServerMsgProcessor extends Thread {

    private final String TAG = "(SERVER MSGPROCESS):";
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private ObjectInputStream inObj;

    public ServerMsgProcessor(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
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

    private void processNewWhiteboard(NewWhiteboard newwb) {
        InetSocketAddress mgrAddress = new InetSocketAddress(socket.getInetAddress(), newwb.getPort());
        boolean success = server.addWhiteboardMgr(newwb, mgrAddress);
        BasicReply brep;
        if (success) {
            brep = new BasicReply(success, "Successfully added whiteboard named " + newwb.getWbName());
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

        // Forward the cup to each user in the userList (except for the original sender):
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

    private void processJoinRequest(JoinRequest joinreq) {
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
        if (!server.isUserNameUnique(joinreq.getWbName(), joinreq.getUserName())){
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

        //Send reply to original sender:
        BasicReply brep = new BasicReply(true, "Join Request sent to " + joinreq.getWbName() + " manager. Request pending.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error send confirmation reply to user following process join request.");
        }
    }

    private void processJoinDecision(JoinDecision joindec) {

        //Get otherUser details from wb manager:
        User otherUser = server.getUser(joindec.getWbName(), joindec.getUserName());

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

    private void processCanvasUpdate(CanvasUpdate canup) {

        //Get graphics object from stream:
        ArrayList<ShapeWrapper> graphics = null;
        try {
            graphics = (ArrayList<ShapeWrapper>) inObj.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(TAG + "Error reading in graphics during canvas update.");
        }

        // Get User list:
        ArrayList<User> userList = server.getUserList(canup.getWbName());

        // Forward the canup and graphics to each user in the userList (except for the original sender):
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

    }

    private void processLeave(Leave leave) {
        BasicReply brep;
        if (server.checkUser(leave.getWbName(), leave.getUserName())) {
            server.deleteUser(leave.getWbName(), leave.getUserName());
            brep = new BasicReply(true, "You have been removed from " + leave.getWbName() + ".");
            User manager = server.getManager(leave.getWbName());
            ServerMsgSender sender = new ServerMsgSender(leave, manager.address);
            sender.start();
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

    private void processBootUser(BootUser btuser) {

        BootUserReply btuserrep;
        User manager = server.getManager(btuser.getWbName());
        //Check if mgr has authority, then action boot:
        if (manager.address.getAddress() != socket.getInetAddress() || manager.username != btuser.getMgrName()) {
            btuserrep = new BootUserReply(false, btuser.getUserName());
            System.out.println(TAG + "Could not boot user " + btuser.getUserName() + ". No authority.");
        }
        // Check if user exists on this wb:
        else if (!server.checkUser(btuser.getWbName(), btuser.getUserName())) {
            btuserrep = new BootUserReply(false, btuser.getUserName());
            System.out.println(TAG + "Could not boot user " + btuser.getUserName() + ". User not on list.");
        }
        // Otherwise delete user:
        else {
            server.deleteUser(btuser.getWbName(), btuser.getUserName());
            btuserrep = new BootUserReply(true, btuser.getUserName());
            User bootedUser = server.getUser(btuser.getWbName(), btuser.getUserName());
            ServerMsgSender sender = new ServerMsgSender(btuser, bootedUser.address);
            sender.start();
            System.out.println(TAG + "Booted user " + btuser.getUserName() + " from " + btuser.getWbName());
        }

        //Send boot user reply to manager:
        try {
            out.writeUTF(btuserrep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error sending boot user reply to manager.");
        }
    }

    private void processClose(Close close) {

        // Get User list:
        ArrayList<User> userList = server.getUserList(close.getWbName());

        // Forward the close to each user in the userList (except for the manager):
        int count = 0;
        for (User u : userList) {
            if (!u.username.equals(close.getMgrName())) {
                ServerMsgSender sender = new ServerMsgSender(close, u.address);
                sender.start();
                count++;
            }
        }
        System.out.println(TAG + "Sent close notification to " + count + " other users.");

        //Delete whiteboard from whiteboards:
        server.deleteWhiteboard(close.getWbName());
        System.out.println(TAG + "Whiteboard " + close.getWbName() + " closed.");

        //Send reply to original sender:
        BasicReply brep = new BasicReply(true, "Close notification sent to " + count + " other users.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error send confirmation of close.");
        }
    }
}
