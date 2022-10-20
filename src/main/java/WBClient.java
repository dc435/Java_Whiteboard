import client.ClientGUI;
import client.ClientMsgProcessor;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class WBClient {

    private static final int DEFAULT_CLIENT_PORT = 3211;
    private static final InetSocketAddress DEFAULT_SERVER_ADDRESS = new InetSocketAddress("localhost", 4210);
    private static final String WELCOME_MSG = "Welcome to the COMP90015 Whiteboard Client, by D Curran & Y Peng.";
    private static final String APP_NAME = "WHITEBOARD";
    private static int clientPort;
    private static InetSocketAddress serverAddress;

    public static void main (String[] args) {

        //process options (eg. -p port number)
        clientPort = DEFAULT_CLIENT_PORT;
        serverAddress = DEFAULT_SERVER_ADDRESS;

        System.out.println(WELCOME_MSG);
        ClientGUI gui = new ClientGUI(serverAddress, clientPort, APP_NAME);
        gui.setVisible(true);

        Thread t = new Thread(() -> startListening(gui));
        t.start();

    }

    private static void startListening(ClientGUI gui) {

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(
            ServerSocket server = factory.createServerSocket(clientPort)){
            System.out.println("Listening for messages on port " + clientPort);
            while(true){
                Socket socket = server.accept();
                ClientMsgProcessor processor =  new ClientMsgProcessor(socket, gui);
                processor.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
