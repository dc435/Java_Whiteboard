package client;

import javax.swing.*;
import java.awt.*;

public class PopUpGUI extends JFrame{
    private JButton btnOK;
    private JButton btnCancel;
    private JTextField txtUserEntry;
    private JLabel lblText;
    private JPanel wholePane;

    public PopUpGUI(String appName, String label) {

        super(appName);
        lblText.setText(label);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(wholePane);
        this.setBackground(Color.WHITE);
        this.pack();


    }
}
