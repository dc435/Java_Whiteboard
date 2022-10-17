package server;

import message.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMsgProcessor extends Thread {

    Socket socket;
    Server server;
    DataInputStream in;
    DataOutputStream out;

    public ServerMsgProcessor(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            JSONParser parser = new JSONParser();
            in = new DataInputStream(socket.getInputStream());
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
        echoMessage(canup, canup.getWbName());
    }

    private void processChatUpdate(ChatUpdateRequest chatup) {
        echoMessage(chatup, chatup.getWbName());
    }

    private void echoMessage(Message msg, String wbName) {
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

}
