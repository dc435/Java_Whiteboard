import client.ClientGUI;
import client.ClientMsgProcessor;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class WBClient {

    private static final int DEFAULT_CLIENT_PORT = 3210;
    private static final InetSocketAddress DEFAULT_SERVER_ADDRESS = new InetSocketAddress("localhost", 3200);
    private static final String WELCOME_MSG = "Welcome to the COMP90015 Whiteboard Client, by D Curran & Y Peng (Group 29).";
    private static final String APP_NAME = "WHITEBOARD";
    private static final int TIMEOUT = 1000;
    private static int clientPort;
    private static InetSocketAddress serverAddress;

    // WBClient class is entry point into application for all client-side users
    public static void main (String[] args) {

        serverAddress = DEFAULT_SERVER_ADDRESS;
        parseCmdOption(args);

        System.out.println(WELCOME_MSG);
        ClientGUI gui = new ClientGUI(serverAddress, clientPort, APP_NAME);
        gui.setVisible(true);

        Thread t = new Thread(() -> startListening(gui));
        t.start();

    }

    // Parse command line option of client port (optional)
    private static void parseCmdOption(String[] args) {

        clientPort = DEFAULT_CLIENT_PORT;

        // Check if args[0] is valid client port number
        if (args.length > 0) {
            int portNo = 0;
            try {
                portNo = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port number not valid. Using default client port " + DEFAULT_CLIENT_PORT);
            }
            if (portNo > 100 && portNo < 65535) {
                clientPort = portNo;
            }
        }
    }

    // Start listener on new thread
    private static void startListening(ClientGUI gui) {

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(
            ServerSocket server = factory.createServerSocket(clientPort)){
            System.out.println("Listening for messages on port " + clientPort);
            while(true){
                Socket socket = server.accept();
                socket.setSoTimeout(TIMEOUT);
                ClientMsgProcessor processor =  new ClientMsgProcessor(socket, gui);
                processor.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
