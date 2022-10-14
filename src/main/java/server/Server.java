package server;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {


    private int serverPort;
    private Whiteboard whiteboard;

    public Server(int serverPort) {

        this.serverPort = serverPort;

    }

    public void run() {

        this.whiteboard = new Whiteboard();
        System.out.println("Created new whiteboard.");

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(ServerSocket server = factory.createServerSocket(serverPort)){
            System.out.println("Listening for users on port " + serverPort);
            while(!isInterrupted()){
                Socket socket = server.accept();
                Session session =  new Session(socket, whiteboard);
                session.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
