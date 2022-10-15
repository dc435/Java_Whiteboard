package client;

import message.Message;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientProcessMsg extends Thread {

    Socket socket;
    ClientGUI gui;

    public ClientProcessMsg(Socket socket, ClientGUI gui) {
        this.socket = socket;
        this.gui = gui;

    }

    public void run() {
        try {
            JSONParser parser = new JSONParser();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Message message = (Message) parser.parse(in.readUTF());
            process(message);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void process(Message message) {
        System.out.println("Received: " + message.toString());
    }

}
