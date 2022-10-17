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

public class ClientMsgProcessor extends Thread {

    private Socket socket;
    private ClientGUI gui;
    private DataInputStream in;
    private DataOutputStream out;
    ObjectInputStream inObj;

    public ClientMsgProcessor(Socket socket, ClientGUI gui) {
        this.socket = socket;
        this.gui = gui;
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
            case "CanvasUpdateRequest":
                CanvasUpdate canup = new CanvasUpdate(js);
                processCanvasUpdate(canup);
                break;
            case "ChatUpdateRequest":
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
            case "BootUSer":
                BootUser btuser = new BootUser(js);
                processBootUser(btuser);
                break;
        }

    }

    private void processCanvasUpdate(CanvasUpdate canup) {
        ArrayList<ShapeWrapper> graphicsNew = null;
        // If approved, listen for wb array from server:
        try {
            graphicsNew = (ArrayList<ShapeWrapper>) inObj.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        //send basic approval reply to server:
        BasicReply brep = new BasicReply(true, "Updated canvas graphics received.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //update gui:
        gui.incomingCanvasUpdate(graphicsNew, canup.getUserName());
        gui.updateStatus("Updated canvas received from: " + canup.getUserName());
    }

    private void processChatUpdate(ChatUpdate chatup) {
        gui.incomingChatUpdate(chatup.getUserName(), chatup.getChat());
        gui.updateStatus("Chat update from " + chatup.getUserName());
    }

    private void processJoinRequest(JoinRequest joinreq) {
        gui.incomingJoinRequest(joinreq.getWbName(), joinreq.getUserName());
        gui.updateStatus("Join request from " + joinreq.getUserName());
    }

    private void processJoinDecision(JoinDecision joindec) {
        ArrayList<ShapeWrapper> graphics = null;
        // If approved, listen for wb array from server:
        if (joindec.getApproved()) {
            try {
                graphics = (ArrayList<ShapeWrapper>) inObj.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        //send basic approval reply to server:
        BasicReply brep = new BasicReply(true, "Join decision and update whiteboard received.");
        try {
            out.writeUTF(brep.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //update gui:
        gui.incomingJoinDecision(joindec.getWbName(), joindec.getApproved(), graphics);
        gui.updateStatus("Join decision received regarding whiteboard: " + joindec.getWbName() + " : " + joindec.getApproved());

    }

    private void processBootUser(BootUser btuser) {
        gui.incomingBootUser(btuser.getWbName(), btuser.getMgrName());
        gui.updateStatus("User booted from whiteboard by " + btuser.getMgrName());
    }

}
