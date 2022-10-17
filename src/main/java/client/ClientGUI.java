package client;

import message.*;
import whiteboard.ShapeWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
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

    private ArrayList<ShapeWrapper> graphicsFinal;
    private ArrayList<ShapeWrapper> graphicsBuffer;


    public ClientGUI(InetSocketAddress serverAddress, int clientPort) {
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
        this.wbName = DEFAULT_WB_NAME;
        this.userName = DEFAULT_USER_NAME;
    }

    public final static HashMap<String, String> COLOR = new HashMap<>();
    private JPanel panel1;
    private JPanel canvas;
    private JButton lineButton;
    private JButton circleButton;
    private JButton rectangleButton;
    private JButton freeButton;
    private JComboBox<String> colorBar;
    private JButton triangleButton;
    private JToolBar shapeBar;
    private String colorHex = "#000000";
    private String brush;
    private Path2D triPath = new Path2D.Float();
    public ArrayList<Shape> graphicsArrayList = new ArrayList<>(); // TODO:change to wrapper
    public ArrayList<String> colorArrayList = new ArrayList<>();
    private Point2D.Float p1 = new Point2D.Float();
    private Point2D.Float p2 = new Point2D.Float();


    public ClientGUI(String appName) {
        // From tutorial
        super(appName);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panel1);
        this.setBackground(Color.WHITE);
        this.pack();

        // Set size
        this.setBounds(30,30,900,650);

        // Color Bar
        colorBar.addItem("Black");
        COLOR.put("Black", "#000000");
        colorBar.addItem("Red");
        COLOR.put("Red", "#FF0000");
        colorBar.addItem("Maroon");
        COLOR.put("Maroon", "#800000");
        colorBar.addItem("Yellow");
        COLOR.put("Yellow", "#FFFF00");
        colorBar.addItem("Olive");
        COLOR.put("Olive", "#808000");
        colorBar.addItem("Green");
        COLOR.put("Green", "#008000");
        colorBar.addItem("Blue");
        COLOR.put("Blue", "#0000FF");
        colorBar.addItem("Purple");
        COLOR.put("Purple", "#800080");
        colorBar.addItem("Navy");
        COLOR.put("Navy", "#000080");
        colorBar.addItem("Aqua");
        COLOR.put("Aqua", "#00FFFF");
        colorBar.addItem("Fuchsia");
        COLOR.put("Fuchsia", "#FF00FF");

        // Color Bar listener
        colorBar.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String color = (String) e.getItem();
                colorHex = COLOR.get(color);
                System.out.println(colorHex);
            }
        });



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

        triangleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                brush = triangleButton.getText();
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
                        graphicsArrayList.add(line2D);
                        colorArrayList.add(colorHex);
                        repaint(); // Call paint(g)
                        // TODO: Send out arraylist
                        break;

                    case "Circle":
                        float x = p1.x;
                        float y = p1.y;
                        float w = p2.x - p1.x;
                        float h = p2.y - p1.y;
                        Ellipse2D.Float circle2D = new Ellipse2D.Float(x, y, w, h);
                        graphicsArrayList.add(circle2D);
                        colorArrayList.add(colorHex);
                        repaint();
                        // TODO: Send out arraylist
                        break;

                    case "Rectangle":
                        float x1 = p1.x;
                        float y1 = p1.y;
                        float w1 = p2.x - p1.x;
                        float h1 = p2.y - p1.y;
                        Rectangle2D.Float rectangle2D = new Rectangle2D.Float(x1, y1, w1, h1);
                        graphicsArrayList.add(rectangle2D);
                        colorArrayList.add(colorHex);
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

                if (brush.equals("FreeH")) {
                    if (p2.x != 0 && p2.y != 0) {
                        p1.x = p2.x;
                        p1.y = p2.y;
                    }
                    p2.setLocation(e.getX(), e.getY());
                    Line2D.Float line2D = new Line2D.Float(p1, p2);
                    graphicsArrayList.add(line2D);
                    colorArrayList.add(colorHex);
                    repaint(); // Call paint(g)

                }

                if (brush.equals("Triangle")) {
                    Point p = e.getPoint();
                    float tx = p.x - p1.x;
                    float ty = p.y - p1.y;
                    AffineTransform area = AffineTransform.getTranslateInstance(tx, ty);
                    triPath.transform(area);
                    repaint();
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

        int i = 0;

        // Iterate the lines array and draw
        for (Shape obj : graphicsArrayList) {
            String hexV = colorArrayList.get(i);
            g2.setColor(Color.decode(hexV));
            g2.draw(obj);
            i++;
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
        ChatUpdateRequest chatup = new ChatUpdateRequest(wbName, userName, chat);
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

    public void updateCanvas(ArrayList<ShapeWrapper> graphicsNew, String otherUserName) {
        //TODO: YP to do
    }

    public void updateChat(String otherUserName, String chat) {
        //TODO: YP. For incoming chat / text from other users. Display on GUI, eg: "Bob: Hello, welcome to the canvas!"
    }

    public void incomingJoinRequest(String wbName, String userName) {
        // TODO: YP. For incoming request from other user (userName) to join wb (wbName) hosted by this manager.
        // This should display the request to the present user with 'accept / decline' button / option.
        // Then call method .sendJoinDecision() with boolean of true (accepted) or false (declined).
    }

    public void incomingJoinDecision(String wbName, boolean approved, ArrayList<ShapeWrapper> graphics) {
        //TODO: YP. For incoming decisions by managers in reply to an earlier request by this user to join a wb.
        //'wbGraphics' will only be a complete arraylist if approved = true. Otherwise it will be null.
    }

    public void updateStatus(String update) {
        //TODO: YP: Change so that this updates GUI (rather than command line):
        System.out.println("UPDATE: " + update);

    }

}
