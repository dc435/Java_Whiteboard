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
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.net.InetSocketAddress;
import java.util.HashMap;


public class ClientGUI extends JFrame {

    private final String DEFAULT_WB_NAME = "My New Whiteboard";
    private final String DEFAULT_USER_NAME = "New User";
    private InetSocketAddress serverAddress;
    private int clientPort;
    private String wbName;
    private String userName;

    public ClientGUI(InetSocketAddress serverAddress, int clientPort) {
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
        this.wbName = DEFAULT_WB_NAME;
        this.userName = DEFAULT_USER_NAME;
    }

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
    private JTextField txtType;
    private JButton btnSend;
    private JTextArea txtMember;
    private JTextArea txtLog;
    private JPanel pnlText;
    private JPanel pnlManage;
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
    private String colorHex = "#000000"; // default black
    private String brush = "Line"; // default line brush
    private Path2D triPath = new Path2D.Float();
    private ArrayList<ShapeWrapper> graphicsFinal = new ArrayList<>();
    private ArrayList<ShapeWrapper> graphicsBuffer = new ArrayList<>();
    private Point2D.Float p1 = new Point2D.Float();
    private Point2D.Float p2 = new Point2D.Float();


    public ClientGUI(String appName) {
        // From tutorial
        super(appName);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(pnlMain);
        this.setPreferredSize(new Dimension(1000,750));


        // Set size
        this.setBounds(30,30,900,650);


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
                brush = bntTextCanvas.getText();
                System.out.println(brush); //debug
            }
        });





        // MouseListener for drawing on canvas
        pnlCanvas.addMouseListener(new MouseAdapter() {

            // Starting point of the shape
            @Override
            public void mousePressed(MouseEvent e) {
                p1.setLocation(0,0);
                p2.setLocation(0,0);
                p1.setLocation(e.getX(), e.getY());
            }

            // Ending point of the shape
            @Override
            public void mouseReleased(MouseEvent e) {
                p2.setLocation(e.getX(), e.getY());

                switch (brush) {

                    case "Line":
                        Line2D.Float line2D = new Line2D.Float(p1, p2);
                        ShapeWrapper wrapper = new ShapeWrapper(line2D, colorHex);

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

                    case "Text":
                        // TODO:
                        break;

                    case "FreeH":
                        // Send out update only when user release mouse
                        sendCanvasUpdate();
                        break;

                }
            }
        });

        pnlCanvas.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

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
        });

        this.pack();
    }

    public static void main(String[] args) {
        ClientGUI frame = new ClientGUI("Tester");
        frame.setVisible(true);

    }

    @Override
    public void paint(Graphics g) {
        // Convert graphics objects to graphics2D objects
        super.paint(g);
        Graphics2D g2 = (Graphics2D) pnlCanvas.getGraphics();

        for (ShapeWrapper wrapper : graphicsFinal) {
            g2.setColor(Color.decode(wrapper.getColor()));
            g2.draw(wrapper.getShape());

            //FIXME: for debug
            System.out.println("Buffer size: " + graphicsBuffer.size());
            System.out.println("Final size: " + graphicsFinal.size());
        }
    }


    //DC: For Testing:
    public void guiTester() {
        //
    }

    private void setMngButtonListeners() {

        btnUserName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFrame userInput = new JFrame();
                Object result = JOptionPane.showInputDialog(userInput, "Enter new username:");
                updateUserName(result.toString());
            }
        });
        btnServer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFrame userInput = new JFrame();
                Object result = JOptionPane.showInputDialog(userInput, "Enter server address in form '127.0.0.1:999':");
                String newServerAddress = result.toString();
                try {
                    URI uri = new URI("my://" + newServerAddress);
                    String host = uri.getHost();
                    int port = uri.getPort();
                    InetSocketAddress newAdd = new InetSocketAddress(host,port);
                    updateServerAddress(newAdd);
                } catch (URISyntaxException urx) {
                    updateStatus("Failure to process new server address.");
                }
            }
        });
    }

    private void switchState(ClientState state){
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
                break;
            case MGR:
                btnJoin.setEnabled(false);
                btnLeave.setEnabled(false);
                btnOpen.setEnabled(false);
                btnNew.setEnabled(false);
                btnSave.setEnabled(true);
                btnSaveAs.setEnabled(true);
                btnBoot.setEnabled(true);
                btnClose.setEnabled(true);
                btnUserName.setEnabled(false);
                btnServer.setEnabled(false);
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
            updateStatus("Whiteboard saved to file " + fileName);
        } catch (IOException e) {
            updateStatus("Error saving whiteboard.");
            updateStatus("Error saving whiteboard. File not found.");
        }
    }

    private void openFile(String fileName) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName));
            Whiteboard wb = (Whiteboard) inputStream.readObject();
            inputStream.close();
            graphicsFinal = wb.getGraphicsFinal();
            wbName = wb.getWbName();
            //TODO: refresh canvas? Disable buttons? Update window title bar?
        } catch (FileNotFoundException e) {
            updateStatus("Could not open whiteboard. File not found.");
        } catch (IOException e) {
            updateStatus("Could not open whiteboard.");
        } catch (ClassNotFoundException e) {
            updateStatus("Could not open whiteboard. WB class not found.");
        }
    }

    private void updateUserName(String newUserName){
        userName = newUserName;
    }

    private void updateServerAddress(InetSocketAddress newAdd) {serverAddress = newAdd;}

    private void sendNewWBRequest(String mgrName, String wbName) {
        NewWBRequest wbr = new NewWBRequest(mgrName, wbName, clientPort);
        ClientMsgSender sender = new ClientMsgSender(wbr, serverAddress, this);
        sender.start();
    }

    private void sendCanvasUpdate() {
        CanvasUpdate canup = new CanvasUpdate(wbName, userName);
        ClientMsgSender sender = new ClientMsgSender(canup, serverAddress, this, graphicsBuffer);
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
        //TODO: YP to do. Called when
        graphicsFinal.addAll(graphicsNew);
        repaint();
    }

    public void incomingChatUpdate(String otherUserName, String chat) {
        //TODO: YP. For incoming chat / text from other users. Display on GUI with name of sending user
        //eg: "Bob: Hello, welcome to the canvas!"
    }

    public void incomingJoinRequest(String wbName, String userName) {
        // TODO: YP. For incoming request from other user (userName) to join wb (wbName) hosted by this manager.
        // This should display the request to the present user with 'accept / decline' button / option.
        // Then call method .sendJoinDecision() with boolean of true (accepted) or false (declined).
    }

    public void incomingJoinDecision(String wbName, boolean approved, ArrayList<ShapeWrapper> graphics) {
        //TODO: YP. For incoming decisions by managers in reply to an earlier request by this user to join a wb.
        //'graphics' will only be a complete arraylist if approved = true. Otherwise it will be null.
    }

    public void incomingBootUser(String wbName, String mgrName) {
        //TODO: YP. For incoming boot by manager. When manager boots this user from the wb.
        //After this is received, user will have no access to canvas/chat etc. Perhaps wipe canvas clean?
    }

    public void updateStatus(String update) {
        //TODO: YP: Change so that this updates GUI (rather than command line):
        System.out.println("UPDATE: " + update);

    }

}
