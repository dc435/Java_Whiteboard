package client;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener extends Thread {

    private ClientGUI gui;
    private int clientPort;


    public ClientListener(ClientGUI gui) {

        this.clientPort = 3210;//TODO: get info from GUI:
        this.gui = gui;

    }

    public void run() {

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(
            ServerSocket server = factory.createServerSocket(clientPort)){
            System.out.println("Listening for messages on port " + clientPort);
            while(!isInterrupted()){
                Socket socket = server.accept();
                ClientProcessMsg processor =  new ClientProcessMsg(socket, gui);
                processor.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
