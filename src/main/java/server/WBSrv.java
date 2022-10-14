package server;

public class WBSrv {

    private static final int DEFAULT_SERVER_PORT = 3500;
    private static final String WELCOME_MSG = "Welcome to the COMP90015 Whiteboard Server, by D Curran & Y Peng.";
    private static int serverPort;


    public static void main (String[] args) {

        //process options (eg. -p port number)
        serverPort = DEFAULT_SERVER_PORT;

        System.out.println(WELCOME_MSG);

        Server server = new Server(serverPort);
        server.start();

    }

}
