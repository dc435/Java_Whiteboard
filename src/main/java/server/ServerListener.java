package server;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener extends Thread {

    private int serverPort;
    private Server server;

    public ServerListener(int serverPort, Server server) {

        this.serverPort = serverPort;
        this.server = server;

    }

    public void run() {

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(ServerSocket serverSocket = factory.createServerSocket(serverPort)){
            System.out.println("Listening for messages on port " + serverPort);
            while(!isInterrupted()){

                Socket socket = serverSocket.accept();
                ServerProcessMsg processor =  new ServerProcessMsg(socket, server);
                processor.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
