package server;

import message.BasicReply;
import message.CanvasUpdate;
import message.JoinDecision;
import message.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.ShapeWrapper;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMsgSender extends Thread {

    private final String TAG = "(SERVER MSGSEND): ";
    private Message message;
    private InetSocketAddress target;
    private DataOutputStream out;
    private DataInputStream in;
    private JSONParser parser;
    private ArrayList<ShapeWrapper> graphics;
    private ObjectOutputStream outObj;

    public ServerMsgSender(Message message, InetSocketAddress target) {
        this.message = message;
        this.target = target;
        this.parser = new JSONParser();
    }

    public ServerMsgSender(Message message, InetSocketAddress target, ArrayList<ShapeWrapper> graphics) {
        this.message = message;
        this.target = target;
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
            System.out.println(TAG + "Error establishing outbound connection with client.");
            return;
        }

        String type = message.getType();
        switch(type) {
            case "JoinRequest":
                ListenForBasicReply();
                break;
            case "JoinDecision":
                ProcessJoinDecision();
                break;
            case "CanvasUpdate":
                ProcessCanvasUpdate();
                break;
            case "BootUser": //Send notification to booted user that they have been booted.
                ListenForBasicReply();
                break;
            case "Leave":
                ListenForBasicReply();
                break;
            case "Close":
                ListenForBasicReply();
                break;
        }
    }

    private void ListenForBasicReply() {
        try {
            JSONObject js = (JSONObject) parser.parse(in.readUTF());
            BasicReply brep = new BasicReply(js);
            System.out.println(TAG + "(RECEIVED FROM CLIENT:)  " + brep.getMessage());
        } catch (IOException e) {
            System.out.println(TAG + "Did not receive confirmation from client (IOException).");
        } catch (ParseException e) {
            System.out.println(TAG + "Could not parse response from client (ParseException).");
        }
    }

    private void ProcessJoinDecision() {
        JoinDecision joindec = (JoinDecision) message;
        if (joindec.getApproved()) {
            try {
                outObj.writeObject(graphics);
                out.flush();
            } catch (IOException e) {
                System.out.println(TAG + "Error writing graphics during approved join decision.");
            }
        }
        ListenForBasicReply();
    }

    private void ProcessCanvasUpdate() {
        CanvasUpdate canup = (CanvasUpdate) message;
        try {
            outObj.writeObject(graphics);
            out.flush();
        } catch (IOException e) {
            System.out.println(TAG + "Error writing graphics during canvas update.");
        }
        ListenForBasicReply();
    }
}
