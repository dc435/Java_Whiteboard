import server.Server;
import server.ServerMsgProcessor;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WBServer {

    private static final int DEFAULT_SERVER_PORT = 3200;
    private static final String WELCOME_MSG = "Welcome to the COMP90015 Whiteboard Server, by D Curran & Y Peng (Group 29).";
    private static int serverPort;

    // WBServer is entry point into application for all server-side components
    public static void main (String[] args) {

        parseCmdOption(args);

        System.out.println(WELCOME_MSG);

        Server server = new Server();
        server.start();
        Thread t = new Thread(() -> startListening(server));
        t.start();

    }

    // Parse optional command line of server port number
    private static void parseCmdOption(String[] args) {

        serverPort = DEFAULT_SERVER_PORT;

        // Check if args[0] is valid client port number
        if (args.length > 0) {
            int portNo = 0;
            try {
                portNo = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port number not valid. Using default client port " + DEFAULT_SERVER_PORT);
            }
            if (portNo > 100 && portNo < 65535) {
                serverPort = portNo;
            }
        }
    }

    // Start listener on new thread
    private static void startListening(Server server) {

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(ServerSocket serverSocket = factory.createServerSocket(serverPort)){
            System.out.println("Listening for messages on port " + serverPort + "...");
            while(true){
                Socket socket = serverSocket.accept();
                ServerMsgProcessor processor =  new ServerMsgProcessor(socket, server);
                processor.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
