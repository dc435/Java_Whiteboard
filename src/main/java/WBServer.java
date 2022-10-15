import server.Server;
import server.ServerListener;

public class WBServer {

    private static final int DEFAULT_SERVER_PORT = 3211;
    private static final String WELCOME_MSG = "Welcome to the COMP90015 Whiteboard Server, by D Curran & Y Peng.";
    private static int serverPort;


    public static void main (String[] args) {

        //process options (eg. -p port number)
        serverPort = DEFAULT_SERVER_PORT;

        System.out.println(WELCOME_MSG);

        Server server = new Server();
        server.start();
        ServerListener listener = new ServerListener(serverPort, server);
        listener.start();

    }

}
