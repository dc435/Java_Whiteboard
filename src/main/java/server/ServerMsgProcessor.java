package server;

import message.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.User;

import java.awt.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMsgProcessor extends Thread {

    Socket socket;
    Server server;
    DataInputStream in;
    DataOutputStream out;
    ObjectInputStream inObj;

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
            e.printStackTrace();
        }
    }

    private void processJSON(JSONObject js) {

        String jsType = (String) js.get("_type");
        switch(jsType) {
            case "NewWBRequest":
                NewWBRequest wbr = new NewWBRequest(js);
                processNewWBRequest(wbr);
                break;
            case "CanvasUpdateRequest":
                CanvasUpdateRequest cup = new CanvasUpdateRequest(js);
                processCanvasUpdate(cup);
                break;
            case "ChatUpdate":
                ChatUpdateRequest chatup = new ChatUpdateRequest(js);
                processChatUpdate(chatup);
                break;
//            case "FullCanvas":
//                FullCanvas fullcan = new FullCanvas(js);
//                processFullCanvas(fullcan);
//                break;
            case "JoinRequest":
                JoinRequest joinreq = new JoinRequest(js);
                processJoinRequest(joinreq);
                break;
            case "JoinDecision":
                JoinDecision joindec = new JoinDecision(js);
                processJoinDecision(joindec);
                break;

        }

    }

    private void processNewWBRequest(NewWBRequest wbr) {
        InetSocketAddress mgrAddress = new InetSocketAddress(socket.getInetAddress(), wbr.getPort());
        boolean added = server.addWhiteboardMgr(wbr, mgrAddress);
        NewWBReply newWBReply = new NewWBReply(added);
        try {
            out.writeUTF(newWBReply.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCanvasUpdate(CanvasUpdateRequest canup) {
        echoMessageToAll(canup, canup.getWbName());
    }

    private void processChatUpdate(ChatUpdateRequest chatup) {
        echoMessageToAll(chatup, chatup.getWbName());
    }

    private void echoMessageToAll(Message msg, String wbName) {
        // Check if server is managing a whiteboard with that name:
        if (!server.isManagingWhiteboard(wbName)){
            BasicReply brep = new BasicReply(false, "No whiteboard named " + wbName);
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // Get User list:
        ArrayList<User> userList = server.getUserList(wbName);

        // Forward the cup to each user in the userList (except for the original sender):
        for (User u : userList) {
            if (u.address.getAddress() != socket.getInetAddress()) {
                ServerMsgSender sender = new ServerMsgSender(msg, u.address);
                sender.start();
            }
        }

        // Send reply to original sender:
        BasicReply brep = new BasicReply(true, "Sent update to " + userList.size() + " users.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
                e.printStackTrace();
            }
            return;
        }

        //Check if username is unique to that whiteboard:
        if (!server.isUserNameUnique(joinreq.getWbName(), joinreq.getUserName())){
            BasicReply brep = new BasicReply(false, "Username (" + joinreq.getUserName() + ") is not unique to this whiteboard.");
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            e.printStackTrace();
        }
    }

    private void processJoinDecision(JoinDecision joindec) {

        //Get otherUser details from wb manager:
        User otherUser = server.getUser(joindec.getWbName(), joindec.getUserName());

        if (joindec.getApproved()) {

            otherUser.approved = true;
            ArrayList<Shape> graphicsArrayList = null;
            try {
                graphicsArrayList = (ArrayList<Shape>) inObj.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            //Forward JoinDec and GraphicArrayList to otherUser:
            ServerMsgSender sender = new ServerMsgSender(joindec, otherUser.address, graphicsArrayList);
            sender.start();
            // Send reply to original sender:
            BasicReply brep = new BasicReply(true, "Sent join approval and full canvas to user " + joindec.getUserName());
            try {
                out.writeUTF(brep.toString());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }
            //Delete user from whiteboard manager:
            server.deleteUser(joindec.getWbName(), joindec.getUserName());
        }

    }
}
