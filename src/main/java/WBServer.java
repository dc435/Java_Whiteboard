import server.Server;
import server.ServerMsgProcessor;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WBServer {

    private static final int DEFAULT_SERVER_PORT = 4210;
    private static final String WELCOME_MSG = "Welcome to the COMP90015 Whiteboard Server, by D Curran & Y Peng.";
    private static int serverPort;

    public static void main (String[] args) {

        serverPort = DEFAULT_SERVER_PORT;

        System.out.println(WELCOME_MSG);

        Server server = new Server();
        server.start();
        Thread t = new Thread(() -> startListening(server));
        t.start();

    }

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
