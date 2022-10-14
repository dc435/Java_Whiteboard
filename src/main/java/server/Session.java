package server;

import java.io.*;
import java.net.Socket;

public class Session extends Thread {

    private Socket socket;
    private int timeout;
    private Whiteboard whiteboard;

    public Session(Socket socket, Whiteboard whiteboard)  {
        this.socket = socket;
        this.whiteboard = whiteboard;

    }

    public void run() {
        runSession();

        try {
            socket.close();
            System.out.println("Disconnected from client.");
        } catch (IOException e) {
            System.out.println("Error disconnecting from client.");
        }

    }

    private void runSession() {

        // 1. Establish connection and variables:

        String ip = socket.getInetAddress().getHostAddress();
        int port = socket.getPort();
        DataInputStream in;
        DataOutputStream out;

        try {

            System.out.println("Processing request from client on " + ip + ":" + port);
            socket.setSoTimeout(timeout);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {

            System.out.println("Error connecting to client.");
            return;

        }

        // 2. Process request:

        //TODO
        // read JSON in
        // case . switch, depending on type of message
        // if connect request, add to userlist (subject to approval)

    }



}
