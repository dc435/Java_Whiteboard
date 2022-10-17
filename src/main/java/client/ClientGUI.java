package client;

import message.*;
import whiteboard.ShapeWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.net.InetSocketAddress;


public class ClientGUI extends JFrame {

    private final String DEFAULT_WB_NAME = "My New Whiteboard";
    private final String DEFAULT_USER_NAME = "New User";
    private InetSocketAddress serverAddress;
    private int clientPort;
    private String wbName;
    private String userName;

    private ArrayList<ShapeWrapper> graphicsFinal;
    private ArrayList<ShapeWrapper> graphicsBuffer;


    public ClientGUI(InetSocketAddress serverAddress, int clientPort) {
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
        this.wbName = DEFAULT_WB_NAME;
        this.userName = DEFAULT_USER_NAME;
    }

    private JPanel panel1;
    private JPanel canvas;
    private JButton lineButton;
    private JButton circleButton;
    private JButton rectangleButton;
    private JButton freeButton;
    private String brush;
    public ArrayList<Shape> graphicsArrayList = new ArrayList<Shape>();
    private Point2D.Float p1;
    private Point2D.Float p2;


    public ClientGUI(String appName) {
        // From tutorial
        super(appName);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panel1);
        this.setBackground(Color.WHITE);
        this.pack();

        // Set size
        this.setBounds(30,30,900,650);

        // MouseListener for switching brushes
        rectangleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = rectangleButton.getText();
                System.out.println(brush); //debug
            }
        });

        lineButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = lineButton.getText();
                System.out.println(brush); //debug
            }
        });

        circleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = circleButton.getText();
                System.out.println(brush); //debug
            }
        });

        freeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = freeButton.getText();
                System.out.println(brush); //debug
            }
        });



        // MouseListener for drawing on canvas
        canvas.addMouseListener(new MouseAdapter() {

            // Starting point of the shape
            @Override
            public void mousePressed(MouseEvent e) {
                p1 = new Point2D.Float(e.getX(), e.getY());
            }

            // Ending point of the shape
            @Override
            public void mouseReleased(MouseEvent e) {
                p2 = new Point2D.Float(e.getX(), e.getY());

                switch (brush) { //FIXME: weird error coming out

                    case "Line":
                        Line2D.Float line2D = new Line2D.Float(p1, p2);
                        graphicsArrayList.add(line2D);
                        repaint(); // Call paint(g)
                        // TODO: Send out arraylist
                        break;

                    case "Circle":
                        float x = (float) p1.getX();
                        float y = (float) p1.getY();
                        float w = (float) (p2.getX() - p1.getX());
                        float h = (float) (p2.getY() - p1.getY());
                        Ellipse2D.Float circle2D = new Ellipse2D.Float(x, y, w, h);
                        graphicsArrayList.add(circle2D);
                        repaint();
                        // TODO: Send out arraylist
                        break;

                    case "Rectangle":
                        float x1 = (float) p1.getX();
                        float y1 = (float) p1.getY();
                        float w1 = (float) (p2.getX() - p1.getX());
                        float h1 = (float) (p2.getY() - p1.getY());
                        Rectangle2D.Float rectangle2D = new Rectangle2D.Float(x1, y1, w1, h1);
                        graphicsArrayList.add(rectangle2D);
                        repaint();
                        // TODO: Send out arraylist
                        break;

                }
            }
        });


        canvas.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                if (brush.equals("FreeH")) { //FIXME: need to init p1 and p2 and clear positions

                    p1.x = p2.x;
                    p1.y = p2.y;
                    p2.setLocation(e.getX(), e.getY());
                    Line2D.Float line2D = new Line2D.Float(p1, p2);
                    graphicsArrayList.add(line2D);
                    repaint(); // Call paint(g)

                }
            }
        });
    }

    public static void main(String[] args) {
        ClientGUI frame = new ClientGUI("Tester");
        frame.setVisible(true);

    }


    @Override
    public void paint(Graphics g) {
        // Convert graphics objects to graphics2D objects
        Graphics2D g2 = (Graphics2D) g;



        // Iterate the lines array and draw
        for (Shape obj : graphicsArrayList) {
            g2.draw(obj);
        }

    }

    public ArrayList<Shape> getGraphicsArrayList() {return this.graphicsArrayList;}


    //DC: For Testing:
    public void guiTester() {
        Line2D line = new Line2D.Float();
        line.setLine(1,2,3,4);
        sendCanvasUpdate();
    }

    //DC: Example public method for making new whiteboard request to server:
    private void sendNewWBRequest(String mgrName, String wbName) {
        NewWBRequest wbr = new NewWBRequest(mgrName, wbName, clientPort);
        ClientMsgSender sender = new ClientMsgSender(wbr, serverAddress, this);
        sender.start();
    }

    private void sendCanvasUpdate() {
        CanvasUpdate canup = new CanvasUpdate(wbName, userName);
        ClientMsgSender sender = new ClientMsgSender(canup, serverAddress, this, graphicsBuffer);
        sender.start();
        graphicsFinal.addAll(graphicsBuffer);
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

    public void sendBootUser(String otherUserName) {
        BootUser btuser = new BootUser(wbName, userName, otherUserName);
        ClientMsgSender sender = new ClientMsgSender(btuser, serverAddress, this);
        sender.start();
    }

    public void incomingCanvasUpdate(ArrayList<ShapeWrapper> graphicsNew, String otherUserName) {
        //TODO: YP to do. Called when
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
