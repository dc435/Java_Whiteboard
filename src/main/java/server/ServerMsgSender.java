package server;

import message.BasicReply;
import message.JoinDecision;
import message.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMsgSender extends Thread {

    Message message;
    InetSocketAddress target;
    DataOutputStream out;
    DataInputStream in;
    JSONParser parser;
    ArrayList<Shape> graphicsArrayList;
    ObjectOutputStream outObj;

    public ServerMsgSender(Message message, InetSocketAddress target) {
        this.message = message;
        this.target = target;
        this.parser = new JSONParser();
    }

    public ServerMsgSender(Message message, InetSocketAddress target, ArrayList<Shape> graphicsArrayList) {
        this.message = message;
        this.target = target;
        this.parser = new JSONParser();
        this.graphicsArrayList = graphicsArrayList;
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
            case "JoinDecision":
                ProcessJoinDecision();
                break;
        }
    }

    private void ListenForBasicReply() {
        try {
            JSONObject js = (JSONObject) parser.parse(in.readUTF());
            BasicReply brep = new BasicReply(js);
            System.out.println(brep.getMessage());
        } catch (IOException e) {
            System.out.println("Did not receive confirmation from client (IOException).");
        } catch (ParseException e) {
            System.out.println("Could not parse response from client (ParseException).");
        }
    }

    private void ProcessJoinDecision() {
        JoinDecision joindec = (JoinDecision) message;
        if (joindec.getApproved()) {
            try {
                outObj.writeObject(graphicsArrayList);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ListenForBasicReply();
    }
}
