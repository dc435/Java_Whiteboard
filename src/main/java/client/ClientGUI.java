package client;

import message.CanvasUpdateRequest;
import message.NewWBRequest;

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
//        sendNewWBRequest("Bob", "My Whiteboard1");
        sendCanvasUpdate((float)1.345,(float)2.3339, "Line", "Black");
    }

    //DC: Example public method for making new whiteboard request to server:
    private void sendNewWBRequest(String mgrName, String wbName) {
        NewWBRequest wbr = new NewWBRequest(mgrName, wbName, clientPort);
        ClientMsgSender sender = new ClientMsgSender(wbr, serverAddress, this);
        sender.start();
    }

    private void sendCanvasUpdate(float x, float y, String brushType, String color) {
        CanvasUpdateRequest cup = new CanvasUpdateRequest(wbName, userName, x, y, brushType, color);
        ClientMsgSender sender = new ClientMsgSender(cup, serverAddress, this);
        sender.start();
    }

    public void updateCanvas(float x, float y, String brushType, String color) {

    }

    public void updateStatus(String update) {
        //TODO: Change so that this updates GUI (rather than command line):
        System.out.println("UPDATE: " + update);

    }

}
