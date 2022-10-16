package client;

import message.Message;
import message.NewWBReply;
import message.NewWBRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientMsgSender extends Thread {

    Message message;
    InetSocketAddress target;
    ClientGUI gui;
    DataOutputStream out;
    DataInputStream in;
    JSONParser parser;

    public ClientMsgSender(Message message, InetSocketAddress target, ClientGUI gui) {
        this.message = message;
        this.target = target;
        this.gui = gui;
        parser = new JSONParser();

    }

    public void run() {

        try {
            Socket socket = new Socket(target.getAddress(), target.getPort());
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            out.writeUTF(message.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String type = message.getType();
        switch(type) {
            case "NewWBRequest":
                ListenForNewWBReply();
                break;
            case "ConnectionRequest":
                //NewConnectionRequest();
                break;
        }
    }

    private void ListenForNewWBReply() {
        try {
            JSONObject js = (JSONObject) parser.parse(in.readUTF());
            NewWBReply wbr = new NewWBReply(js);
            if (wbr.getAdded()) {
                System.out.println("New Whiteboard added by server.");
            } else {
                System.out.println("Server failed to add whiteboard.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}