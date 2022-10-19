package client;

import message.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.ShapeWrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientMsgSender extends Thread {

    Message message;
    InetSocketAddress target;
    ClientGUI gui;
    DataOutputStream out;
    DataInputStream in;
    JSONParser parser;
    ArrayList<ShapeWrapper> graphics;
    ObjectOutputStream outObj;

    public ClientMsgSender(Message message, InetSocketAddress target, ClientGUI gui) {
        this.message = message;
        this.target = target;
        this.gui = gui;
        this.parser = new JSONParser();
    }

    public ClientMsgSender(Message message, InetSocketAddress target, ClientGUI gui, ArrayList<ShapeWrapper> graphics) {
        this.message = message;
        this.target = target;
        this.gui = gui;
        this.parser = new JSONParser();
        this.graphics = graphics;
    }

    public void run() {

        try {
            Socket socket = new Socket(target.getAddress(), target.getPort());
            out = new DataOutputStream(socket.getOutputStream());
            outObj = new ObjectOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            out.writeUTF(message.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String type = message.getType();
        switch(type) {
            case "JoinRequest":
                ListenForBasicReply();
                break;
            case "NewWhiteboard":
                ListenForBasicReply();
                break;
            case "CanvasUpdate":
                CompleteCanvasUpdate();
                break;
            case "ChatUpdate":
                ListenForBasicReply();
                break;
            case "JoinDecision":
                CompleteJoinDecision();
                break;
            case "BootUser":
                ListenForBasicReply();
        }
    }

    private void ListenForBasicReply() {
        try {
            JSONObject js = (JSONObject) parser.parse(in.readUTF());
            BasicReply brep = new BasicReply(js);
            gui.updateStatus(brep.getMessage());
        } catch (IOException e) {
            gui.updateStatus("Did not receive confirmation from server (IOException).");
        } catch (ParseException e) {
            gui.updateStatus("Could not parse response from server (ParseException).");
        }
    }

    private void CompleteJoinDecision() {
        JoinDecision joindec = (JoinDecision) message;
        if (joindec.getApproved()) {
            try {
                outObj.writeObject(graphics);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ListenForBasicReply();
    }

    private void CompleteCanvasUpdate() {
        try {
            outObj.writeObject(graphics);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ListenForBasicReply();
    }

}