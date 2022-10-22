package client;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import message.*;
import whiteboard.ClientState;
import whiteboard.ShapeWrapper;
import whiteboard.Whiteboard;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Locale;


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
    private JButton btnLine;
    private JButton btnCircle;
    private JButton btnRectangle;
    private JButton btnFreeHand;
    private JComboBox<String> barColor;
    private JButton btnTriangle;
    private JToolBar barShape;
    private JButton btnTextCanvas;
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
    private JPanel pnlTxtInput;
    private String canvasStr;
    private String colorHex = "#000000"; // default black
    private String brush = "Line"; // default line brush
    private Path2D triPath;
    private ShapeWrapper wrapper = new ShapeWrapper();
    private ArrayList<ShapeWrapper> graphicsFinal = new ArrayList<>();
    private ArrayList<ShapeWrapper> graphicsBuffer = new ArrayList<>();
    private Point2D.Float p1 = new Point2D.Float();
    private Point2D.Float p2 = new Point2D.Float();

    // ClientGUI is central class for all client/user-side logic and variables
    // Constructor builds initial state as "NONE" (ie. no whiteboard loaded) and sets associated GUI elements
    public ClientGUI(InetSocketAddress serverAddress, int clientPort, String APP_NAME) {
        super(APP_NAME);
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
        this.wbName = DEFAULT_WB_NAME;
        this.userName = DEFAULT_USER_NAME;
        setState(ClientState.NONE);
        setTitle(wbName);
        activeUsers = new ArrayList<>();
        guiConstructors();
        setMngButtonListeners();

    }

    // Constructor used to constructor JFrame and components
    private void guiConstructors() {

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(pnlMain);
        this.setPreferredSize(new Dimension(1500, 800));

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
        barColor.addItem("Brown"); // 12
        COLOR.put("Brown", "#A52A2A");
        barColor.addItem("Gray");
        COLOR.put("Gray", "#808080");
        barColor.addItem("Amber");
        COLOR.put("Amber", "#FFBF00");
        barColor.addItem("Amaranth");
        COLOR.put("Amaranth", "#9F2B68");
        barColor.addItem("Pear");
        COLOR.put("Pear", "#C9CC3F");

        // Color Bar listener
        barColor.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String color = (String) e.getItem();
                colorHex = COLOR.get(color);
            }
        });

        // MouseListener for switching brushes
        btnRectangle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = btnRectangle.getText();
            }
        });

        btnLine.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = btnLine.getText();
            }
        });

        btnCircle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = btnCircle.getText();
            }
        });

        btnTriangle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = btnTriangle.getText();
            }
        });

        btnFreeHand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = btnFreeHand.getText();
            }
        });

        btnTextCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getComponent().isEnabled()) {
                    JFrame canvasTextInput = new JFrame();
                    canvasStr = JOptionPane.showInputDialog(canvasTextInput, "Enter text for canvas, then click to place:");
                    brush = btnTextCanvas.getText();
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
                            triPath = new Path2D.Float();
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
                                wrapper = new ShapeWrapper(canvasStr, colorHex, true, (int) p2.x, (int) p2.y);
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

    }

    // Initialise mouse and button listeners for all non-canvas / draw gui components:
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
                    }
                    ;
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
                            leaveCurrentWhiteboard();
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
                    }
                    ;
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
                    }
                    ;
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
                    }
                    ;
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
                    }
                    ;
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
                    }
                    ;
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
                            serverAddress.getAddress().getCanonicalHostName() + ":" + serverAddress.getPort());
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
                    txtChat.append("\n" + userName + ": " + chatText);
                    sendChatUpdate(chatText);
                    txtChatIn.setText("");
                }
            }
        });
    }

    // Switch the 'state' of the GUI (NONE =  no whiteboard loaded; USER = Joined another whiteboard; MGR = Started own whiteboard as manager)
    // The method enables/disables GUI elements to match the appropriate state.
    public void setState(ClientState state) {
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
                txtChatIn.setEnabled(false);
                pnlCanvas.setEnabled(false);
                btnTextCanvas.setEnabled(false);
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
                txtUsers.setVisible(true);
                txtChatIn.setEnabled(true);
                pnlCanvas.setEnabled(true);
                btnTextCanvas.setEnabled(true);
                break;
            case MGR:
                btnJoin.setEnabled(false);
                btnLeave.setEnabled(false);
                btnOpen.setEnabled(false);
                btnNew.setEnabled(false);
                if (currentFileName != null) {
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
                txtChatIn.setEnabled(true);
                pnlCanvas.setEnabled(true);
                btnTextCanvas.setEnabled(true);
                break;
        }
    }

    // Build Whiteboard object and write to binary file
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

    // Open Whiteboard object from binary file, and load into GUI
    private void openFile(String fileName) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName));
            Whiteboard wb = (Whiteboard) inputStream.readObject();
            inputStream.close();
            graphicsFinal = wb.getGraphicsFinal();
            repaint();
            wbName = wb.getWbName();
            setTitle(wbName);
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

    // Update user name (helper method)
    private void updateUserName(String newUserName) {
        userName = newUserName;
        updateStatus(TAG + "Updated username to " + userName);
    }

    // Update server address (helper method)
    private void updateServerAddress(InetSocketAddress newAdd) {
        serverAddress = newAdd;
        updateStatus(TAG + "Updated server address to " + serverAddress.toString());
    }

    // Remove user from user list (helper method)
    private void removeUser(String otherUserName) {
        activeUsers.remove(otherUserName);
    }

    // Add user to user list (helper method)
    private void addUser(String otherUserName) {
        activeUsers.add(otherUserName);
    }

    public String getUserName() {
        return this.userName;
    }

    // Refresh user list (when new user joins / leaves whiteboard being managed in MGR state)
    private void refreshUserList() {
        txtUsers.setText("");
        txtUsers.append("ACTIVE USERS:\n");
        txtUsers.append(userName + " (mgr)\n");
        for (String u : activeUsers) {
            txtUsers.append(u + "\n");
        }
    }

    // Create new whiteboard and update relevant GUI elements
    private void buildNewWhiteboard(String newWBName) {
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

    // Close current whiteboard and change relevant GUI elements
    private void closeCurrentWhiteboard() {
        setState(ClientState.NONE);
        wbName = DEFAULT_WB_NAME;
        setTitle(wbName);
        txtChat.setText("CHAT:");
        graphicsFinal = new ArrayList<ShapeWrapper>();
        graphicsBuffer = new ArrayList<ShapeWrapper>();
        activeUsers.clear();
        refreshUserList();

    }

    // Leave current whiteboard and update GUI elements
    private void leaveCurrentWhiteboard() {
        setState(ClientState.NONE);
        wbName = DEFAULT_WB_NAME;
        setTitle(wbName);
        txtChat.setText("CHAT:");
        graphicsFinal = new ArrayList<ShapeWrapper>();
        graphicsBuffer = new ArrayList<ShapeWrapper>();
    }

    // Start new MsgSender thread, send NewWhiteboard message to server
    private void sendNewWhiteboard(String newWBName) {
        NewWhiteboard newwb = new NewWhiteboard(userName, newWBName, clientPort);
        ClientMsgSender sender = new ClientMsgSender(newwb, serverAddress, this);
        sender.start();
    }

    // Start new MsgSender thread, send Close message to server
    private void sendCloseWhiteboard() {
        Close close = new Close(wbName, userName);
        ClientMsgSender sender = new ClientMsgSender(close, serverAddress, this);
        sender.start();
    }

    // Start new MsgSender thread, send canvas update to server
    private void sendCanvasUpdate() {
        CanvasUpdate canup = new CanvasUpdate(wbName, userName);
        ArrayList<ShapeWrapper> graphicsToSend = makeCopy(graphicsBuffer);
        ClientMsgSender sender = new ClientMsgSender(canup, serverAddress, this, graphicsToSend);
        sender.start();
        graphicsBuffer.clear();
    }

    // Start new MsgSender thread, send chat update to server
    private void sendChatUpdate(String chat) {
        ChatUpdate chatup = new ChatUpdate(wbName, userName, chat);
        ClientMsgSender sender = new ClientMsgSender(chatup, serverAddress, this);
        sender.start();
    }

    // Start new MsgSender thread, send Join request to server
    private void sendJoinRequest() {
        JoinRequest joinreq = new JoinRequest(wbName, userName, clientPort);
        ClientMsgSender sender = new ClientMsgSender(joinreq, serverAddress, this);
        sender.start();
    }

    // Start new MsgSender thread, send Leave notice to server
    private void sendLeave() {
        Leave leave = new Leave(wbName, userName);
        ClientMsgSender sender = new ClientMsgSender(leave, serverAddress, this);
        sender.start();
    }

    // Start new MsgSender thread, send Join Decision to server (ie. the response from the Manager)
    private void sendJoinDecision(String otherUserName, boolean accepted) {
        JoinDecision joindec = new JoinDecision(wbName, otherUserName, accepted);
        ClientMsgSender sender = new ClientMsgSender(joindec, serverAddress, this, graphicsFinal);
        sender.start();
    }

    // Start new MsgSender thread, send Boot User to server (from manager)
    private void sendBootUser(String otherUserName) {
        BootUser btuser = new BootUser(wbName, userName, otherUserName);
        ClientMsgSender sender = new ClientMsgSender(btuser, serverAddress, this);
        sender.start();
    }

    // Update canvas based on ShapeWrapper array received from other users
    public void incomingCanvasUpdate(ArrayList<ShapeWrapper> graphicsNew, String otherUserName) {
        graphicsFinal.addAll(graphicsNew);
        repaint();
        updateStatus(TAG + "Canvas update received from " + otherUserName);
    }

    // Update chat received from other users
    public void incomingChatUpdate(String otherUserName, String chat) {
        txtChat.append(otherUserName + ": " + chat + "\n");
        updateStatus(TAG + "Chat update received from " + otherUserName);
    }

    // Notify manager and ask for approval when other user asks to join
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

    // When new user receives a join decision from remote manager, and, if approved, updates full canvas
    public void incomingJoinDecision(String newWbName, boolean approved, ArrayList<ShapeWrapper> graphics) {
        if (approved) {
            wbName = newWbName;
            setTitle(wbName);
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

    // When user has been booted by manager, clears canvas and switch to NONE state
    public void incomingBootUser(String oldWbName, String mgrName) {
        graphicsFinal.clear();
        graphicsBuffer.clear();
        wbName = DEFAULT_WB_NAME;
        setTitle(wbName);
        txtChat.setText("CHAT:");
        repaint();
        setState(ClientState.NONE);
        updateStatus(TAG + "You have been booted from this whiteboard by " + mgrName);
    }

    // Notification to manager that another user has left. Update userlist display:
    public void incomingLeave(String otherUserName) {
        removeUser(otherUserName);
        refreshUserList();
        updateStatus(TAG + otherUserName + " has left the whiteboard.");
    }

    // Notification to manager of whether the boot request was successful
    public void incomingBootUserReply(boolean success, String otherUserName) {
        if (success) {
            removeUser(otherUserName);
            refreshUserList();
            updateStatus(TAG + otherUserName + " has been removed from the whiteboard.");
        } else {
            updateStatus(TAG + "Could not remove user '" + otherUserName + "'. Check the user name.");
        }
    }

    // Incoming notice that manager has closed the whiteboard. Call method to clear current canvas and chat, and switch to NONE state
    public void incomingClose(Close close) {
        if (close.getWbName().equals(wbName)) {
            closeCurrentWhiteboard();
            updateStatus(TAG + "Whiteboard closed by manager " + close.getMgrName());
        }
    }

    // Makes deep copy of graphics array to send to new thread (for outgoing canvas update) prior to clearing graphicsBuffer
    private ArrayList<ShapeWrapper> makeCopy(ArrayList<ShapeWrapper> arrayIN) {
        ArrayList<ShapeWrapper> arrayOUT = new ArrayList<ShapeWrapper>();
        for (ShapeWrapper sw : arrayIN) {
            arrayOUT.add(sw);
        }
        return arrayOUT;
    }

    // Generic output method to Log screen of GUI
    public void updateStatus(String update) {
        txtLog.append("\n" + update);
        repaint();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        pnlMain = new JPanel();
        pnlMain.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        pnlMain.setBackground(new Color(-1));
        pnlCanvas = new JPanel();
        pnlCanvas.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pnlCanvas.setBackground(new Color(-1));
        pnlCanvas.setEnabled(true);
        pnlCanvas.setToolTipText("Canvas");
        pnlMain.add(pnlCanvas, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        pnlCanvas.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, 10, pnlCanvas.getFont()), null));
        pnlText = new JPanel();
        pnlText.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        pnlText.setBackground(new Color(-1));
        pnlMain.add(pnlText, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        pnlText.add(scrollPane1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtChat = new JTextArea();
        txtChat.setBackground(new Color(-328961));
        txtChat.setEnabled(false);
        Font txtChatFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, txtChat.getFont());
        if (txtChatFont != null) txtChat.setFont(txtChatFont);
        txtChat.setText("CHAT:");
        scrollPane1.setViewportView(txtChat);
        final JScrollPane scrollPane2 = new JScrollPane();
        pnlText.add(scrollPane2, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtLog = new JTextArea();
        txtLog.setBackground(new Color(-328961));
        txtLog.setEnabled(false);
        Font txtLogFont = this.$$$getFont$$$("JetBrains Mono", Font.PLAIN, 12, txtLog.getFont());
        if (txtLogFont != null) txtLog.setFont(txtLogFont);
        txtLog.setText("LOG:");
        scrollPane2.setViewportView(txtLog);
        pnlTxtInput = new JPanel();
        pnlTxtInput.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        pnlTxtInput.setBackground(new Color(-1));
        pnlText.add(pnlTxtInput, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        txtChatIn = new JTextField();
        Font txtChatInFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, txtChatIn.getFont());
        if (txtChatInFont != null) txtChatIn.setFont(txtChatInFont);
        txtChatIn.setHorizontalAlignment(10);
        txtChatIn.setText("");
        pnlTxtInput.add(txtChatIn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 21), null, 0, false));
        btnSend = new JButton();
        Font btnSendFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnSend.getFont());
        if (btnSendFont != null) btnSend.setFont(btnSendFont);
        btnSend.setText("Chat");
        pnlTxtInput.add(btnSend, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(98, 21), null, 1, false));
        pnlManage = new JPanel();
        pnlManage.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        pnlManage.setBackground(new Color(-1));
        pnlMain.add(pnlManage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        pnlManage.add(scrollPane3, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtUsers = new JTextArea();
        txtUsers.setBackground(new Color(-328961));
        txtUsers.setEnabled(false);
        Font txtUsersFont = this.$$$getFont$$$("JetBrains Mono", Font.PLAIN, 11, txtUsers.getFont());
        if (txtUsersFont != null) txtUsers.setFont(txtUsersFont);
        txtUsers.setText("");
        scrollPane3.setViewportView(txtUsers);
        barManage = new JToolBar();
        barManage.setFloatable(false);
        barManage.setOrientation(0);
        pnlManage.add(barManage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnJoin = new JButton();
        Font btnJoinFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnJoin.getFont());
        if (btnJoinFont != null) btnJoin.setFont(btnJoinFont);
        btnJoin.setText("Join");
        barManage.add(btnJoin);
        btnLeave = new JButton();
        Font btnLeaveFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnLeave.getFont());
        if (btnLeaveFont != null) btnLeave.setFont(btnLeaveFont);
        btnLeave.setText("Leave");
        barManage.add(btnLeave);
        btnOpen = new JButton();
        Font btnOpenFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnOpen.getFont());
        if (btnOpenFont != null) btnOpen.setFont(btnOpenFont);
        btnOpen.setText("Open");
        barManage.add(btnOpen);
        btnNew = new JButton();
        Font btnNewFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnNew.getFont());
        if (btnNewFont != null) btnNew.setFont(btnNewFont);
        btnNew.setText("New");
        barManage.add(btnNew);
        btnSave = new JButton();
        Font btnSaveFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnSave.getFont());
        if (btnSaveFont != null) btnSave.setFont(btnSaveFont);
        btnSave.setText("Save");
        barManage.add(btnSave);
        btnSaveAs = new JButton();
        Font btnSaveAsFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnSaveAs.getFont());
        if (btnSaveAsFont != null) btnSaveAs.setFont(btnSaveAsFont);
        btnSaveAs.setText("Save as");
        barManage.add(btnSaveAs);
        btnBoot = new JButton();
        Font btnBootFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnBoot.getFont());
        if (btnBootFont != null) btnBoot.setFont(btnBootFont);
        btnBoot.setText("Boot User");
        barManage.add(btnBoot);
        btnClose = new JButton();
        Font btnCloseFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnClose.getFont());
        if (btnCloseFont != null) btnClose.setFont(btnCloseFont);
        btnClose.setText("Close");
        barManage.add(btnClose);
        btnUserName = new JButton();
        Font btnUserNameFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnUserName.getFont());
        if (btnUserNameFont != null) btnUserName.setFont(btnUserNameFont);
        btnUserName.setText("Username");
        barManage.add(btnUserName);
        btnServer = new JButton();
        Font btnServerFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnServer.getFont());
        if (btnServerFont != null) btnServer.setFont(btnServerFont);
        btnServer.setText("Server");
        barManage.add(btnServer);
        barShape = new JToolBar();
        barShape.setFloatable(false);
        barShape.setRollover(false);
        barShape.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        pnlManage.add(barShape, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnTriangle = new JButton();
        Font btnTriangleFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnTriangle.getFont());
        if (btnTriangleFont != null) btnTriangle.setFont(btnTriangleFont);
        btnTriangle.setText("Triangle");
        barShape.add(btnTriangle);
        btnFreeHand = new JButton();
        Font btnFreeHandFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnFreeHand.getFont());
        if (btnFreeHandFont != null) btnFreeHand.setFont(btnFreeHandFont);
        btnFreeHand.setText("FreeH");
        barShape.add(btnFreeHand);
        btnRectangle = new JButton();
        Font btnRectangleFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnRectangle.getFont());
        if (btnRectangleFont != null) btnRectangle.setFont(btnRectangleFont);
        btnRectangle.setText("Rectangle");
        barShape.add(btnRectangle);
        btnCircle = new JButton();
        Font btnCircleFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnCircle.getFont());
        if (btnCircleFont != null) btnCircle.setFont(btnCircleFont);
        btnCircle.setText("Circle");
        barShape.add(btnCircle);
        btnLine = new JButton();
        Font btnLineFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnLine.getFont());
        if (btnLineFont != null) btnLine.setFont(btnLineFont);
        btnLine.setText("Line");
        barShape.add(btnLine);
        btnTextCanvas = new JButton();
        Font btnTextCanvasFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, btnTextCanvas.getFont());
        if (btnTextCanvasFont != null) btnTextCanvas.setFont(btnTextCanvasFont);
        btnTextCanvas.setText("Text");
        barShape.add(btnTextCanvas);
        barColor = new JComboBox();
        barColor.setEditable(false);
        Font barColorFont = this.$$$getFont$$$("JetBrains Mono", Font.BOLD, 12, barColor.getFont());
        if (barColorFont != null) barColor.setFont(barColorFont);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        barColor.setModel(defaultComboBoxModel1);
        barColor.setToolTipText("");
        barShape.add(barColor);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return pnlMain;
    }

}
