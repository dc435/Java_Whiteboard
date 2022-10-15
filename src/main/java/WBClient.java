import client.ClientGUI;
import client.ClientListener;

import java.net.InetSocketAddress;


public class WBClient {

    private static final int DEFAULT_CLIENT_PORT = 3210;
    private static final InetSocketAddress DEFAULT_SERVER_ADDRESS = new InetSocketAddress("localhost", 3211);
    private static final String WELCOME_MSG = "Welcome to the COMP90015 Whiteboard Client, by D Curran & Y Peng.";
    private static int clientPort;
    private static InetSocketAddress serverAddress;

    public static void main (String[] args) {

        //process options (eg. -p port number)
        clientPort = DEFAULT_CLIENT_PORT;
        serverAddress = DEFAULT_SERVER_ADDRESS;

        System.out.println(WELCOME_MSG);
        ClientGUI gui = new ClientGUI(serverAddress);
        gui.setVisible(true);
        ClientListener listener = new ClientListener(gui);
        listener.start();

        //For testing:
        gui.guiTester();


        //Testing:
//        ConnectionRequest cr = new ConnectionRequest();
//        cr.setUsername("Anna");
//        ClientSendMsg sender = new ClientSendMsg(cr, serverAddress);
//        sender.start();

//        ArrayList<User> userList = new ArrayList<>();
//        User user = new User();
//        user.username = "braile";
//        user.manager = true;
//        userList.add(user);
//        String asString = userList.toString();
//        System.out.println("as string: " + asString);
//        System.out.println("as string: " + asString);
//        ArrayList<User> userList2 = (ArrayList<User>)asString;

    }

}
