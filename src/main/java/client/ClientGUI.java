package client;

import message.NewWBRequest;

import javax.swing.*;
import java.net.InetSocketAddress;


public class ClientGUI extends JFrame {

    InetSocketAddress serverAddress;

    public ClientGUI(InetSocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }



    //DC: For Testing:
    public void guiTester() {
        NewWBRequest wbr = new NewWBRequest("Dylan","My Whiteboard", "password1");
        ClientMsgSender sender = new ClientMsgSender(wbr, serverAddress, this);
        sender.start();

    }

}
