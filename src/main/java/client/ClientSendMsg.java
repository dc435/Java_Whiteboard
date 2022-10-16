package client;

import message.ConnectionRequest;
import message.Message;
import message.NewWBRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSendMsg extends Thread {

    Message message;
    InetSocketAddress target;
    ClientGUI gui;
    DataOutputStream out;
    DataInputStream in;

    public ClientSendMsg(Message message, InetSocketAddress target, ClientGUI gui) {
        this.message = message;
        this.target = target;
        this.gui = gui;

    }

    public void run() {
        try {
            Socket socket = new Socket(target.getAddress(), target.getPort());
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            String type = message.getType();
            switch(type) {
                case "NewWBRequest":
                    NewWBRequest();
                    break;
                case "ConnectionRequest":
                    //NewConnectionRequest();
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void NewWBRequest() throws IOException {
        out.writeUTF(message.toString());
        out.flush();

    }

}