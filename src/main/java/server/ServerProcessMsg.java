package server;

import message.ConnectionRequest;
import message.NewWBReply;
import message.NewWBRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerProcessMsg extends Thread {

    Socket socket;
    Server server;
    DataInputStream in;
    DataOutputStream out;

    public ServerProcessMsg(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            JSONParser parser = new JSONParser();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            JSONObject js = (JSONObject) parser.parse(in.readUTF());
            process(js);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void process(JSONObject js) {

        String jsType = (String) js.get("_type");
        switch(jsType) {
            case "NewWBRequest":
                NewWBRequest wbr = new NewWBRequest(js);
                processNewWBRequest(wbr);
                break;
            case "ConnectionRequest":
                ConnectionRequest cr = new ConnectionRequest(js);
                processConnectionRequest(cr);
                break;

        }

    }

    private void processNewWBRequest(NewWBRequest wbr) {
        boolean added = server.addWhiteboardMgr(wbr);
        NewWBReply newWBReply = new NewWBReply(added);
        try {
            out.writeUTF(newWBReply.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processConnectionRequest(ConnectionRequest cr) {
        System.out.println("Username: " + cr.getUsername());
        System.out.println(cr);
    }

}
