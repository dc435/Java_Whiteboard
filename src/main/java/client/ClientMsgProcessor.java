package client;

import message.CanvasUpdateReply;
import message.CanvasUpdateRequest;
import message.Message;
import message.NewWBRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientMsgProcessor extends Thread {

    private Socket socket;
    private ClientGUI gui;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientMsgProcessor(Socket socket, ClientGUI gui) {
        this.socket = socket;
        this.gui = gui;
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
            case "CanvasUpdateRequest":
                CanvasUpdateRequest cup = new CanvasUpdateRequest(js);
                processCanvasUpdate(cup);
                break;

        }

    }

    private void processCanvasUpdate(CanvasUpdateRequest cup) {
        gui.updateCanvas(cup.getX(), cup.getY(), cup.getBrushType(), cup.getColor());
        gui.updateStatus("Canvas update from " + cup.getUserName());
    }

}
