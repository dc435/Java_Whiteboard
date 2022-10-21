package client;

import message.*;
import whiteboard.ClientState;
import whiteboard.ShapeWrapper;
import whiteboard.Whiteboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.net.InetSocketAddress;
import java.util.HashMap;


public class ClientGUI extends JFrame {

    private final String DEFAULT_WB_NAME = "My New Whiteboard";
    private final String DEFAULT_USER_NAME = "New User";
    private final String TAG = "(CLIENT GUI): ";
    private InetSocketAddress serverAddress;
    private int clientPort;
    private String wbName;
    private String userName;
    private String currentFileName;
    private ArrayList<String> activeUsers;
    public final static HashMap<String, String> COLOR = new HashMap<>();
    private JPanel pnlMain;
    private JPanel pnlCanvas;
    private JButton bntLine;
    private JButton bntCircle;
    private JButton bntRectangle;
    private JButton bntFreeHand;
    private JComboBox<String> barColor;
    private JButton bntTriangle;
    private JToolBar barShape;
    private JButton bntTextCanvas;
    private JTextArea txtChat;
    private JTextField txtChatIn;
    private JButton btnSend;
    private JTextArea txtUsers;
    private JTextArea txtLog;
    private JPanel pnlText;
    private JButton btnJoin;
    private JButton btnLeave;
    private JButton btnSave;
    private JButton btnNew;
    private JButton btnOpen;
    private JButton btnBoot;
    private JButton btnSaveAs;
    private JButton btnClose;
    private JButton btnUserName;
    private JButton btnServer;
    private JPanel pnlManage;
    private JToolBar barManage;
    private String canvasStr;
    private String colorHex = "#000000"; // default black
    private String brush = "Line"; // default line brush
    private Path2D triPath = new Path2D.Float();
    ShapeWrapper wrapper = new ShapeWrapper();
    private ArrayList<ShapeWrapper> graphicsFinal = new ArrayList<>();
    private ArrayList<ShapeWrapper> graphicsBuffer = new ArrayList<>();
    private Point2D.Float p1 = new Point2D.Float();
    private Point2D.Float p2 = new Point2D.Float();

    public ClientGUI(InetSocketAddress serverAddress, int clientPort, String APP_NAME) {
        super(APP_NAME);
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
        this.wbName = DEFAULT_WB_NAME;
        this.userName = DEFAULT_USER_NAME;
        setState(ClientState.NONE);
        setTitle(wbName);
        activeUsers = new ArrayList<String>();
        guiConstructors();
        setMngButtonListeners();

    }

//    public ClientGUI(String appName) {
//        super(appName);
//        callYPConstructors();//TODO: NOTE to YP: I have moved your constructors to separate method (below), so I can call them also.
//    }

    private void guiConstructors() {

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(pnlMain);
        this.setPreferredSize(new Dimension(1500,800));

        // Color Bar
        barColor.addItem("Black");
        COLOR.put("Black", "#000000");
        barColor.addItem("Red");
        COLOR.put("Red", "#FF0000");
        barColor.addItem("Maroon");
        COLOR.put("Maroon", "#800000");
        barColor.addItem("Yellow");
        COLOR.put("Yellow", "#FFFF00");
        barColor.addItem("Olive");
        COLOR.put("Olive", "#808000");
        barColor.addItem("Green");
        COLOR.put("Green", "#008000");
        barColor.addItem("Blue");
        COLOR.put("Blue", "#0000FF");
        barColor.addItem("Purple");
        COLOR.put("Purple", "#800080");
        barColor.addItem("Navy");
        COLOR.put("Navy", "#000080");
        barColor.addItem("Aqua");
        COLOR.put("Aqua", "#00FFFF");
        barColor.addItem("Fuchsia");
        COLOR.put("Fuchsia", "#FF00FF");

        // Color Bar listener
        barColor.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String color = (String) e.getItem();
                colorHex = COLOR.get(color);
                System.out.println(colorHex);
            }
        });

        // MouseListener for switching brushes
        bntRectangle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = bntRectangle.getText();
                System.out.println(brush); //debug
            }
        });

        bntLine.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = bntLine.getText();
                System.out.println(brush); //debug
            }
        });

        bntCircle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = bntCircle.getText();
                System.out.println(brush); //debug
            }
        });

        bntTriangle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = bntTriangle.getText();
                System.out.println(brush); //debug
            }
        });

        bntFreeHand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = bntFreeHand.getText();
                System.out.println(brush); //debug
            }
        });

        bntTextCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame canvasTextInput = new JFrame();
                    canvasStr = JOptionPane.showInputDialog(canvasTextInput, "Enter text for canvas:");
                    brush = bntTextCanvas.getText();
                    System.out.println(brush);
                }
            }
        });

        // MouseListener for drawing on canvas
        pnlCanvas.addMouseListener(new MouseAdapter() {

            // Starting point of the shape
            @Override
            public void mousePressed(MouseEvent e) {
                if (pnlCanvas.isEnabled()) {
                    p1.setLocation(0, 0);
                    p2.setLocation(0, 0);
                    p1.setLocation(e.getX(), e.getY());
                }
            }

            // Ending point of the shape
            @Override
            public void mouseReleased(MouseEvent e) {
                if (pnlCanvas.isEnabled()) {
                    p2.setLocation(e.getX(), e.getY());

                    switch (brush) {

                        case "Line":
                            Line2D.Float line2D = new Line2D.Float(p1, p2);
                            wrapper = new ShapeWrapper(line2D, colorHex);

                            graphicsFinal.add(wrapper);
                            graphicsBuffer.add(wrapper);
                            sendCanvasUpdate();

                            repaint();
                            break;

                        case "Circle":
                            float x = p1.x;
                            float y = p1.y;
                            float w = Math.abs(p2.x - p1.x);
                            float h = Math.abs(p2.y - p1.y);
                            Ellipse2D.Float circle2D = new Ellipse2D.Float(x, y, w, h);
                            wrapper = new ShapeWrapper(circle2D, colorHex);

                            graphicsFinal.add(wrapper);
                            graphicsBuffer.add(wrapper);
                            sendCanvasUpdate();

                            repaint();
                            break;

                        case "Rectangle":
                            float x1 = p1.x;
                            float y1 = p1.y;
                            float w1 = Math.abs(p2.x - p1.x);
                            float h1 = Math.abs(p2.y - p1.y);
                            Rectangle2D.Float rectangle2D = new Rectangle2D.Float(x1, y1, w1, h1);
                            wrapper = new ShapeWrapper(rectangle2D, colorHex);

                            graphicsFinal.add(wrapper);
                            graphicsBuffer.add(wrapper);
                            sendCanvasUpdate();

                            repaint();
                            break;

                        case "Triangle":
                            triPath.moveTo(p1.x, p1.y); // Starting point as mid-point
                            float pref_w = p2.x - p1.x;
                            triPath.lineTo(p2.x - (2 * pref_w), p2.y);
                            triPath.lineTo(p2.x, p2.y); // Ending point finish point
                            triPath.closePath();
                            wrapper = new ShapeWrapper(triPath, colorHex);

                            graphicsFinal.add(wrapper);
                            graphicsBuffer.add(wrapper);
                            sendCanvasUpdate();

                            repaint();
                            break;

                        case "FreeH":
                            // Send out update only when user release mouse
                            sendCanvasUpdate();
                            break;

                        case "Text":

                            if (canvasStr != null) {
                                wrapper = new ShapeWrapper(canvasStr, colorHex, true, (int) p2.x,(int) p2.y);
                                canvasStr = null;

                                graphicsFinal.add(wrapper);
                                graphicsBuffer.add(wrapper);
                                sendCanvasUpdate();

                                repaint();

                            }
                            break;
                    }
                }
            }

        });

        pnlCanvas.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (e.getComponent().isEnabled()) {
                    if (brush.equals("FreeH")) {
                        if (p2.x != 0 && p2.y != 0) {
                            p1.x = p2.x;
                            p1.y = p2.y;
                        }
                        p2.setLocation(e.getX(), e.getY());
                        Line2D.Float line2D = new Line2D.Float(p1, p2);
                        ShapeWrapper wrapper = new ShapeWrapper(line2D, colorHex);
                        graphicsBuffer.add(wrapper);
                        graphicsFinal.add(wrapper);
                        repaint(); // Call paint(g)

                    }
                }
            }
        });

        this.pack();
    }

//    public static void main(String[] args) {
//        ClientGUI frame = new ClientGUI("Tester");
//        frame.setVisible(true);
//
//    }

    @Override
    public void paint(Graphics g) {
        // Convert graphics objects to graphics2D objects
        super.paint(g);
        Graphics2D g2 = (Graphics2D) pnlCanvas.getGraphics();

        for (ShapeWrapper wrapper : graphicsFinal) {

            // Normal Shapes
            if (!wrapper.isText()) {
                g2.setColor(Color.decode(wrapper.getColor()));
                g2.setStroke(new BasicStroke(5));
                g2.draw((Shape) wrapper.getShape());

            // Draw text
            } else {
                if (canvasStr == null) {
                    String text = (String) wrapper.getShape();
                    g2.setColor(Color.decode(wrapper.getColor()));
                    g2.drawString(text, wrapper.getX(), wrapper.getY());
                }

            }


        }
        //FIXME: for debug
        System.out.println("Buffer size: " + graphicsBuffer.size());
        System.out.println("Final size: " + graphicsFinal.size());

    }

    //DC: For Testing:
//    public void guiTester() {
//        setMngButtonListeners();
//    }

    private void setMngButtonListeners() {

        btnJoin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame userInput = new JFrame();
                    Object result = JOptionPane.showInputDialog(userInput, "Enter name of the remote whiteboard to join:");
                    if (result != null) {
                        wbName = result.toString();
                        sendJoinRequest();
                    };
                }
            }
        });
        btnLeave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to leave");
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            sendLeave();
                            setState(ClientState.NONE);
                            break;
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CANCEL_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            break;
                    }
                }
            }
        });
        btnOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame userInput = new JFrame();
                    Object result = JOptionPane.showInputDialog(userInput, "Enter the name of the file to open:");
                    if (result != null) {
                        String fileName = result.toString();
                        openFile(fileName);
                    };
                }
            }
        });
        btnNew.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame userInput = new JFrame();
                    Object result = JOptionPane.showInputDialog(userInput, "Enter name of new whiteboard:");
                    if (result != null) {
                        buildNewWhiteboard(result.toString());
                    };
                }
            }
        });
        btnSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    writeToFile(currentFileName);
                }
            }
        });
        btnSaveAs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame userInput = new JFrame();
                    Object result = JOptionPane.showInputDialog(userInput, "Enter new file name:");
                    if (result != null) {
                        currentFileName = result.toString();
                        writeToFile(currentFileName);
                    };
                }
            }
        });
        btnBoot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame userInput = new JFrame();
                    Object result = JOptionPane.showInputDialog(userInput, "Enter name of user to boot from this whiteboard:");
                    if (result != null) {
                        String otherUserName = result.toString();
                        sendBootUser(otherUserName);
                    };
                }
            }
        });
        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    int result = JOptionPane.showConfirmDialog(null, "Are sure you want to close this whiteboard?");
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            sendCloseWhiteboard();
                            closeCurrentWhiteboard();
                            updateStatus(TAG + "Whiteboard closed.");
                            break;
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CANCEL_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            System.out.println(TAG + "Continue.");
                            break;
                    }
                }
            }
        });
        btnUserName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame userInput = new JFrame();
                    Object result = JOptionPane.showInputDialog(userInput, "Edit your username:", userName);
                    if (result != null) {
                        updateUserName(result.toString());
                    };
                }
            }
        });
        btnServer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame userInput = new JFrame();
                    Object result = JOptionPane.showInputDialog(userInput, "Edit server address:",
                            serverAddress.getAddress() + ":" + serverAddress.getPort());
                    if (result != null) {
                        String newServerAddress = result.toString();
                        try {
                            URI uri = new URI("my://" + newServerAddress);
                            String host = uri.getHost();
                            int port = uri.getPort();
                            InetSocketAddress newAdd = new InetSocketAddress(host, port);
                            updateServerAddress(newAdd);
                        } catch (Exception ex) {
                            updateStatus(TAG + "Failure to process new server address. Please check the address.");
                        }
                    }
                }
            }
        });
        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    String chatText = txtChatIn.getText();
                    txtChat.append(userName + ": " + chatText + "\n");
                    sendChatUpdate(chatText);
                    txtChatIn.setText("");
                }
            }
        });
        //TODO: Add listener for when press ENTER whilst in Chat text box.
    }

    public void setState(ClientState state){
        switch (state) {
            case NONE:
                btnJoin.setEnabled(true);
                btnLeave.setEnabled(false);
                btnOpen.setEnabled(true);
                btnNew.setEnabled(true);
                btnSave.setEnabled(false);
                btnSaveAs.setEnabled(false);
                btnBoot.setEnabled(false);
                btnClose.setEnabled(false);
                btnUserName.setEnabled(true);
                btnServer.setEnabled(true);
                btnSend.setEnabled(false);
                txtUsers.setVisible(false);
                pnlCanvas.setEnabled(false);
                bntTextCanvas.setEnabled(false);
                break;
            case USER:
                btnJoin.setEnabled(false);
                btnLeave.setEnabled(true);
                btnOpen.setEnabled(false);
                btnNew.setEnabled(false);
                btnSave.setEnabled(false);
                btnSaveAs.setEnabled(false);
                btnBoot.setEnabled(false);
                btnClose.setEnabled(false);
                btnUserName.setEnabled(false);
                btnServer.setEnabled(false);
                btnSend.setEnabled(true);
                txtUsers.setVisible(false);
                pnlCanvas.setEnabled(true);
                bntTextCanvas.setEnabled(true);
                break;
            case MGR:
                btnJoin.setEnabled(false);
                btnLeave.setEnabled(false);
                btnOpen.setEnabled(false);
                btnNew.setEnabled(false);
                if (currentFileName!=null) {
                    btnSave.setEnabled(true);
                } else {
                    btnSave.setEnabled(false);
                }
                btnSaveAs.setEnabled(true);
                btnBoot.setEnabled(true);
                btnClose.setEnabled(true);
                btnUserName.setEnabled(false);
                btnServer.setEnabled(false);
                btnSend.setEnabled(true);
                txtUsers.setVisible(true);
                pnlCanvas.setEnabled(true);
                bntTextCanvas.setEnabled(true);
                break;
        }
    }

    private void writeToFile(String fileName) {
        Whiteboard wb = new Whiteboard(wbName, graphicsFinal);
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
            outputStream.writeObject(wb);
            outputStream.close();
            updateStatus(TAG + "Whiteboard saved to file " + fileName);
            currentFileName = fileName;
            btnSave.setEnabled(true);
        } catch (IOException e) {
            updateStatus(TAG + "Error saving whiteboard.");
        }
    }

    private void openFile(String fileName) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName));
            Whiteboard wb = (Whiteboard) inputStream.readObject();
            inputStream.close();
            graphicsFinal = wb.getGraphicsFinal();
            repaint();
            wbName = wb.getWbName();
            currentFileName = fileName;
            setState(ClientState.MGR);
            sendNewWhiteboard(wbName);
            repaint();
        } catch (FileNotFoundException e) {
            updateStatus(TAG + "Could not open whiteboard. File not found.");
        } catch (IOException e) {
            updateStatus(TAG + "Could not open whiteboard.");
        } catch (ClassNotFoundException e) {
            updateStatus(TAG + "Could not open whiteboard. WB class not found.");
        }
    }

    private void updateUserName(String newUserName){
        userName = newUserName;
        updateStatus(TAG + "Updated username to " + userName);
    }

    private void updateServerAddress(InetSocketAddress newAdd) {
        serverAddress = newAdd;
        updateStatus(TAG + "Updated server address to " + serverAddress.toString());
    }

    private void removeUser(String otherUserName) {
        activeUsers.remove(otherUserName);
    }

    private void addUser(String otherUserName) {
        activeUsers.add(otherUserName);
    }

    private void refreshUserList(){
        txtUsers.setText("");
        txtUsers.append("ACTIVE USERS:\n");
        txtUsers.append(userName + " (mgr)\n");
        for (String u : activeUsers) {
            txtUsers.append(u + "\n");
        }
    }

    private void buildNewWhiteboard(String newWBName){
        setState(ClientState.MGR);
        wbName = newWBName;
        setTitle(newWBName);
        graphicsFinal = new ArrayList<ShapeWrapper>();
        graphicsBuffer = new ArrayList<ShapeWrapper>();
        repaint();
        activeUsers.clear();
        refreshUserList();
        sendNewWhiteboard(newWBName);
        updateStatus(TAG + "New whiteboard " + newWBName + " created. Switch to manager state.");
    }

    private void closeCurrentWhiteboard() {
        setState(ClientState.NONE);
        wbName = DEFAULT_WB_NAME;
        setTitle(wbName);
        graphicsFinal = new ArrayList<ShapeWrapper>();
        graphicsBuffer = new ArrayList<ShapeWrapper>();
        activeUsers.clear();
        refreshUserList();
        txtChat.setText("");
    }

    private void sendNewWhiteboard(String newWBName) {
        NewWhiteboard newwb = new NewWhiteboard(userName, newWBName, clientPort);
        ClientMsgSender sender = new ClientMsgSender(newwb, serverAddress, this);
        sender.start();
    }

    private void sendCloseWhiteboard() {
        Close close = new Close(wbName, userName);
        ClientMsgSender sender = new ClientMsgSender(close, serverAddress, this);
        sender.start();
    }

    private void sendCanvasUpdate() {
        CanvasUpdate canup = new CanvasUpdate(wbName, userName);
        ArrayList<ShapeWrapper> graphicsToSend = makeCopy(graphicsBuffer);
        ClientMsgSender sender = new ClientMsgSender(canup, serverAddress, this, graphicsToSend);
        sender.start();
        graphicsBuffer.clear();
    }

    private void sendChatUpdate(String chat) {
        ChatUpdate chatup = new ChatUpdate(wbName, userName, chat);
        ClientMsgSender sender = new ClientMsgSender(chatup, serverAddress, this);
        sender.start();
    }

    private void sendJoinRequest() {
        JoinRequest joinreq = new JoinRequest(wbName, userName, clientPort);
        ClientMsgSender sender = new ClientMsgSender(joinreq, serverAddress, this);
        sender.start();
    }

    private void sendLeave() {
        Leave leave = new Leave(wbName, userName);
        ClientMsgSender sender = new ClientMsgSender(leave, serverAddress, this);
        sender.start();
    }

    private void sendJoinDecision(String otherUserName, boolean accepted) {
        JoinDecision joindec = new JoinDecision(wbName, otherUserName, accepted);
        ClientMsgSender sender = new ClientMsgSender(joindec, serverAddress, this, graphicsFinal);
        sender.start();
    }

    private void sendBootUser(String otherUserName) {
        BootUser btuser = new BootUser(wbName, userName, otherUserName);
        ClientMsgSender sender = new ClientMsgSender(btuser, serverAddress, this);
        sender.start();
    }

    public void incomingCanvasUpdate(ArrayList<ShapeWrapper> graphicsNew, String otherUserName) {
        graphicsFinal.addAll(graphicsNew);
        repaint();
        updateStatus(TAG + "Canvas update received from " + otherUserName);
    }

    public void incomingChatUpdate(String otherUserName, String chat) {
        txtChat.append(otherUserName + ": " + chat + "\n");
        updateStatus(TAG + "Chat update received from " + otherUserName);
    }

    public void incomingJoinRequest(String wbName, String newUserName) {
        JFrame userInput = new JFrame();
        int result = JOptionPane.showConfirmDialog(null, "The user "
                + newUserName + " has requested to join " + wbName + ". Approve join?");
        switch (result) {
            case JOptionPane.YES_OPTION:
                sendJoinDecision(newUserName, true);
                addUser(newUserName);
                refreshUserList();
                break;
            case JOptionPane.NO_OPTION:
            case JOptionPane.CANCEL_OPTION:
            case JOptionPane.CLOSED_OPTION:
                sendJoinDecision(newUserName, false);
                break;
        }
        updateStatus(TAG + "Join request received from " + newUserName);
    }

    public void incomingJoinDecision(String wbName, boolean approved, ArrayList<ShapeWrapper> graphics) {
        if (approved) {
            updateStatus(TAG + "Your request to join " + wbName + " has been approved.");
            graphicsFinal.clear();
            graphicsBuffer.clear();
            graphicsFinal = graphics;
            repaint();
            setState(ClientState.USER);
            updateStatus(TAG + "The whiteboard has been update with current status of " + wbName);
        } else {
            updateStatus(TAG + "The manager of " + wbName + " did not approve your request to join.");
        }
    }

    public void incomingBootUser(String wbName, String mgrName) {
        graphicsFinal.clear();
        graphicsBuffer.clear();
        repaint();
        setState(ClientState.NONE);
        updateStatus(TAG + "You have been booted from this whiteboard by " + mgrName);
    }

    public void incomingLeave(String otherUserName) {
        //Manager receives confirmation that another user has left:
        removeUser(otherUserName);
        refreshUserList();
        updateStatus(TAG + otherUserName + " has left the whiteboard.");
    }

    public void incomingBootUserReply(boolean success, String otherUserName) {
        if (success) {
            removeUser(otherUserName);
            refreshUserList();
            updateStatus(TAG + otherUserName + " has been removed from the whiteboard.");
        } else {
            updateStatus(TAG + "Could not remove user '" + otherUserName + "'. Check the user name.");
        }
    }

    public void incomingClose(Close close) {
        if (close.getWbName().equals(wbName)) {
            closeCurrentWhiteboard();
            updateStatus(TAG + "Whiteboard closed by manager " + close.getMgrName());
        }
    }

    private ArrayList<ShapeWrapper> makeCopy(ArrayList<ShapeWrapper> arrayIN) {
        ArrayList<ShapeWrapper>arrayOUT = new ArrayList<ShapeWrapper>();
        for (ShapeWrapper sw : arrayIN) {
            arrayOUT.add(sw);
        }
        return arrayOUT;
    }

    public void updateStatus(String update) {
        txtLog.append("\n" + update);
        repaint();
    }

}
