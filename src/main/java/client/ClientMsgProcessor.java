package client;

import message.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.ShapeWrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates new thread to process inbound connection requests to the Client
 */
public class ClientMsgProcessor extends Thread {

    private final String TAG = "(CLIENT MSGPROCESS): ";
    private Socket socket;
    private ClientGUI gui;
    private DataInputStream in;
    private DataOutputStream out;
    private ObjectInputStream inObj;

    /**
     * Constructor
     * @param socket Socket
     * @param gui ClientGUI
     */
    public ClientMsgProcessor(Socket socket, ClientGUI gui) {
        this.socket = socket;
        this.gui = gui;
    }

    public void run() {
        // Send out initial JSON message type
        try {
            JSONParser parser = new JSONParser();
            in = new DataInputStream(socket.getInputStream());
            inObj = new ObjectInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            JSONObject js = (JSONObject) parser.parse(in.readUTF());
            processJSON(js);
        } catch (IOException | ParseException e) {
            gui.updateStatus(TAG + "Error establishing inbound connection with server.");
            return;
        }
    }

    // Process incoming JSON message depending on message type
    private void processJSON(JSONObject js) {

        String jsType = (String) js.get("_type");
        switch(jsType) {
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

    // Logic for inbound canvas update from other user
    private void processCanvasUpdate(CanvasUpdate canup) {
        ArrayList<ShapeWrapper> graphicsNew = null;
        // If approved, listen for wb array from server:
        try {
            graphicsNew = (ArrayList<ShapeWrapper>) inObj.readObject();
        } catch (IOException | ClassNotFoundException e) {
            gui.updateStatus(TAG + "Error reading inbound canvas update graphics.");
        }

        //send basic approval reply to server:
        BasicReply brep = new BasicReply(true, "Updated canvas graphics received by " + gui.getUserName());
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            gui.updateStatus(TAG + "Error sending confirmation of canvas update to server.");
        }
        //update gui:
        gui.incomingCanvasUpdate(graphicsNew, canup.getUserName());
    }

    // Incoming chat message from other user
    private void processChatUpdate(ChatUpdate chatup) {
        gui.incomingChatUpdate(chatup.getUserName(), chatup.getChat());

        //send basic acknowledgment reply to server:
        BasicReply brep = new BasicReply(true, "Chat update received by " + gui.getUserName());
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            gui.updateStatus(TAG + "Error sending confirmation of canvas update to server.");
        }
    }

    // Incoming join request to wb manager for approval
    private void processJoinRequest(JoinRequest joinreq) {
        gui.incomingJoinRequest(joinreq.getWbName(), joinreq.getUserName());

        //send basic acknowledgment reply to server:
        BasicReply brep = new BasicReply(true, "Join request received by "
                + gui.getUserName() + ". Approval pending");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            gui.updateStatus(TAG + "Error sending confirmation of canvas update to server.");
        }
    }

    // Incoming join decision from manager, to user who previously submitted a join request
    private void processJoinDecision(JoinDecision joindec) {
        ArrayList<ShapeWrapper> graphics = null;
        // If approved, listen for wb graphics array which will be following the initial JSON message from server:
        if (joindec.getApproved()) {
            try {
                graphics = (ArrayList<ShapeWrapper>) inObj.readObject();
            } catch (IOException | ClassNotFoundException e) {
                gui.updateStatus(TAG + "Error reading graphics after approved join decision.");
            }
        }
        // Send basic approval reply to server:
        BasicReply brep = new BasicReply(true, "Join decision and update whiteboard received.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            gui.updateStatus(TAG + "Error sending confirmation reply to server after join decision received.");
        }
        // Update gui:
        gui.incomingJoinDecision(joindec.getWbName(), joindec.getApproved(), graphics);
    }

    // Incoming boot user notice from manager, booting this user from whiteboard
    private void processBootUser(BootUser btuser) {
        gui.incomingBootUser(btuser.getWbName(), btuser.getMgrName());
    }

    // Incoming leave notification from other user to manager
    private void processLeave(Leave leave) {
        gui.incomingLeave(leave.getUserName());
        // Send basic approval reply to server:
        BasicReply brep = new BasicReply(true, "Leave message received.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            gui.updateStatus(TAG + "Error sending confirmation reply for leave message.");
        }
    }

    // Incoming close notification from manager to other user
    private void processClose(Close close) {
        gui.incomingClose(close);
        BasicReply brep = new BasicReply(true, "Close message received.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            gui.updateStatus(TAG + "Error sending confirmation reply for close message.");
        }
    }

}
