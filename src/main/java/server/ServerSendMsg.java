package server;

import message.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerSendMsg extends Thread {

    Message message;
    InetSocketAddress target;

    public ServerSendMsg(Message message, InetSocketAddress target) {
        this.message = message;
        this.target = target;

    }

    public void run() {
        try {
            Socket socket = new Socket(target.getAddress(), target.getPort());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(message.toString());
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
