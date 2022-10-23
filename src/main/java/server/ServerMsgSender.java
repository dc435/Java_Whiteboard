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

/**
 * Creates a new thread to process outbound connections from the server
 */
public class ServerMsgSender extends Thread {

    private final String TAG = "(SERVER MSGSEND): ";
    private Message message;
    private InetSocketAddress target;
    private DataOutputStream out;
    private DataInputStream in;
    private JSONParser parser;
    private ArrayList<ShapeWrapper> graphics;
    private ObjectOutputStream outObj;

    /**
     * Constructor where no graphics are being sent (JSON messages only)
     * @param message outgoing message
     * @param target inet address of recipient
     */
    public ServerMsgSender(Message message, InetSocketAddress target) {
        this.message = message;
        this.target = target;
        this.parser = new JSONParser();
    }

    // Constructor where graphics are being sent
    public ServerMsgSender(Message message, InetSocketAddress target, ArrayList<ShapeWrapper> graphics) {
        this.message = message;
        this.target = target;
        this.parser = new JSONParser();
        this.graphics = graphics;
    }

    public void run() {

        // Process original JSON message
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

        // Process next step in protocol depending on message type
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
            case "BootUser":
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

    // Process basic reply protocol
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

    // Process sending join decision (and graphics object) from manager to new user
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

    // Process sending canvas update received from one user to new user, and sending graphics object containing update
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
