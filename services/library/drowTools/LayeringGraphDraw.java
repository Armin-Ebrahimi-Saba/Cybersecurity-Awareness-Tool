package services.library.drowTools;

import models.*;
import models.missions.CyberAsset;
import models.missions.MissionInformation;
import models.missions.MissionObjective;
import models.missions.MissionTask;
import models.vulnerabilities.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LayeringGraphDraw extends JFrame {

    int width;
    int height;

    public Graphics g;

    public HashMap<String,Node> nodes;
    public ArrayList<edge> edges;
    HashMap<String, Color> colors;
    private String change;

    boolean attack_pattern = false;
    boolean machine = false;
    boolean firewall = false;
    boolean subnet = false;
    boolean def = false;

    public LayeringGraphDraw(String name, HashMap<String, Color> color) {
        //Construct with label
        this.setTitle(name);
        nodes = new HashMap<>();
        edges = new ArrayList<edge>();
        width = 30;
        height = 30;
        colors = color;
        change = "";

        this.addMouseListener(new MouseListener() {
            private int yPress;
            private int xPress;
            private boolean isNodePressed = false;
            private String selectedNodeId;
/*
             if (!change) {
                if (attack_pattern) {
                    g.setColor(colors.get("AttackPattern"));
                    g.fillOval(0, 70, 1000, 60);
                }
                if (machine) {
                    g.setColor(colors.get("Machine"));
                    g.fillOval(0, 170, 1000, 60);
                }
                if (subnet) {
                    g.setColor(colors.get("Subnet"));
                    g.fillOval(0, 270, 1000, 60);
                }
                if (firewall) {
                    g.setColor(colors.get("Firewall"));
                    g.fillOval(0, 370, 1000, 60);
                }
                if (def) {
                    g.setColor(Color.gray);
                    g.fillOval(0, 470, 1000, 60);
                }
            }
*/
            @Override
            public void mouseClicked(MouseEvent e) {
                LayeringGraphDraw frame = (LayeringGraphDraw)e.getSource();
                for (Map.Entry<String, Node> nodeInfo : frame.nodes.entrySet()) {
                    Node n = nodeInfo.getValue();
                    if (n.isClicked(e.getX(),e.getY())){
                        new ShowNode(Long.parseLong(nodeInfo.getKey()));
                        break;
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                this.xPress = e.getX();
                this.yPress = e.getY();
                LayeringGraphDraw frame = (LayeringGraphDraw)e.getSource();
                for (Map.Entry<String, Node> nodeInfo : frame.nodes.entrySet()) {
                    Node n = nodeInfo.getValue();
                    if (n.isClicked(e.getX(),e.getY())){
                        isNodePressed = true;
                        selectedNodeId = nodeInfo.getKey();
                        break;
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                int xDrag = e.getX();
                int yDrag = e.getY();
                LayeringGraphDraw frame = (LayeringGraphDraw)e.getSource();
                if (isNodePressed){
                    if (selectedNodeId != null){
                        Node n = frame.nodes.get(selectedNodeId);
                        n.setX(n.x+xDrag-this.xPress);
                        n.setY(n.y+yDrag-this.yPress);
                    }
                    isNodePressed = false;
                }
                else {
                    for (Map.Entry<String, Node> nodeInfo : frame.nodes.entrySet()) {
                        Node n = nodeInfo.getValue();
                        n.setX(n.x+xDrag-this.xPress);
                        n.setY(n.y+yDrag-this.yPress);
                    }
                }
                change = "click";
                frame.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

        });
//        this.setUndecorated(true);
        this.setSize(new Dimension(1000,770));
        this.setLocation(0,110);
        this.setVisible(true);
    }

    class edge {
        String i,j;
        String name;
        int relationship_kind;

        public edge(String ii, String jj,String name,int relationship_kind) {
            i = ii;
            j = jj;
            this.name = name;
            this.relationship_kind = relationship_kind;
        }
    }

    class Arrow extends JComponent
    {

        public double x1,y1,x2,y2;		// endpoints of this Arrow
        Color color;				// color of this Arrow


        public static final double angle = Math.PI/10;	// arrowhead angle to shaft

        public static final double len = 10;		// arrowhead length

        public Arrow (double xOne, double yOne, double xTwo, double yTwo)
        {
            x1 = xOne;
            x2 = xTwo;
            y1 = yOne;
            y2 = yTwo;
        }

        public Arrow (double xOne, double yOne, double xTwo, double yTwo, Color c)
        {  x1 = xOne;
            x2 = xTwo;
            y1 = yOne;
            y2 = yTwo;
            color = c;
        }


        double ax1,ay1, ax2, ay2;		// coordinates of arrowhead endpoints


        private void arrHead (double x1, double y1, double x2, double y2)
        {  double c,a,beta,theta,phi;
            c = Math.sqrt ((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
            if (Math.abs(x2-x1) < 1e-6)
                if (y2<y1) theta = Math.PI/2;
                else theta = - Math.PI/2;
            else
            { if (x2>x1)
                theta = Math.atan ((y1-y2)/(x2-x1)) ;
            else
                theta = Math.atan ((y1-y2)/(x1-x2));
            }
            a = Math.sqrt (len*len  + c*c - 2*len*c*Math.cos(angle));
            beta = Math.asin (len*Math.sin(angle)/a);
            phi = theta - beta;
            ay1 = y1 - a * Math.sin(phi);		// coordinates of arrowhead endpoint
            if (x2>x1)
                ax1 = x1 + a * Math.cos(phi);
            else
                ax1 = x1 - a * Math.cos(phi);
            phi = theta + beta;				// second arrowhead endpoint
            ay2 = y1 - a * Math.sin(phi);
            if (x2>x1)
                ax2 = x1 + a * Math.cos(phi);
            else
                ax2 = x1 - a * Math.cos(phi);
        }
    }

    public void addNode(String id,String name,String collection, int x, int y,String parent_id,String type) {
        //add a node at pixel (x,y)
        nodes.put(id,new Node(name,collection,x,y,parent_id,type));
        this.repaint();
    }

    public void addEdge(String name,String i, String j,int relationship_kind) {
        //add an edge between nodes i and j
        edges.add(new edge(i,j,name,relationship_kind));
        this.repaint();
    }

    public Node getNode(String id){
        return nodes.get(id);
    }

    public void paint(Graphics g) { // draw the nodes and edges
        super.paint (g);
        g.setColor(colors.get("Background"));
        g.fillRect(0,0,100000000,1000000);
        FontMetrics f = g.getFontMetrics();
        int nodeHeight = Math.max(height, f.getHeight());


        for (Map.Entry<String, Node> nodeInfo : nodes.entrySet()) {
            Node n = nodeInfo.getValue();
            switch (n.collection) {
                case AttackPattern.COLLECTION:
                    attack_pattern = true;
                    break;
                case Machine.COLLECTION:
                    machine = true;
                    break;
                case Firewall.COLLECTION:
                    firewall = true;
                    break;
                case Subnet.COLLECTION:
                    subnet = true;
                    break;
                default:
                    def = true;
            }
        }

        if (change == "") {
            if (attack_pattern) {
                g.setColor(colors.get("AttackPattern"));
                g.fillOval(0, 70, 1000, 60);
            }
            if (machine) {
                g.setColor(colors.get("Machine"));
                g.fillOval(0, 170, 1000, 60);
            }
            if (subnet) {
                g.setColor(colors.get("Subnet"));
                g.fillOval(0, 270, 1000, 60);
            }
            if (firewall) {
                g.setColor(colors.get("Firewall"));
                g.fillOval(0, 370, 1000, 60);
            }
            if (def) {
                g.setColor(Color.gray);
                g.fillOval(0, 470, 1000, 60);
            }
        }
        g.setColor(colors.get("Edge"));
        for (edge e : edges) {
            int xi = nodes.get(e.i).x;
            int yi = nodes.get(e.i).y;
            int xj = nodes.get(e.j).x;
            int yj = nodes.get(e.j).y;
            // draw line
            g.drawLine(xi, yi, xj, yj);

            // draw Arrow
            double middleLineX = (double) (xi + xj)/2;
            double middleLineY = (double) (yi + yj)/2;
            double theta;
            if (xj == xi){
                if (yj > yi){
                    theta = Math.PI/2;
                }
                else {
                    theta = 3*Math.PI/2;
                }
            }
            else if (yj == yi){
                if (xj > xi){
                    theta = 0;
                }
                else {
                    theta = Math.PI;
                }
            }
            else {
                theta = Math.atan((double) (yj - yi)/(xj - xi));
                if (xj < xi){
                    theta += Math.PI;
                }
                else if (yj < yi){
                    theta += 2*Math.PI;
                }
            }
            switch (e.relationship_kind){
                case 0: // j is end node
                    drawArrow(g,middleLineX,middleLineY,theta);
                    break;
                case 1: // i is end node
                    theta += Math.PI;
                    theta = theta >= 2*Math.PI ? (theta % (2*Math.PI)) : theta;
                    drawArrow(g,middleLineX,middleLineY,theta);
                    break;
                case 2: // i,j are end nodes
                    drawArrow(g,middleLineX,middleLineY,theta);
                    theta += Math.PI;
                    theta = theta >= 2*Math.PI ? (theta % (2*Math.PI)) : theta;
                    drawArrow(g,middleLineX,middleLineY,theta);
                    break;
            }

            // draw relation name
            g.drawString(e.name, (xi + xj)/2, (yi + yj)/2);
        }

        for (Map.Entry<String, Node> nodeInfo : nodes.entrySet()) {
            Node n = nodeInfo.getValue();
            int nodeWidth = Math.max(width, f.stringWidth(n.name)+width/2);
            switch (n.collection){
                case Vulnerability.COLLECTION:
                {
                    switch (n.type){
                        case CPE.TYPE:
                            g.setColor(colors.get("CPE"));
                            break;
                        case CVSS.TYPE:
                            g.setColor(colors.get("CVSS"));
                            break;
                        case Metric.TYPE:
                            g.setColor(colors.get("Metric"));
                            break;
                        case Severity.TYPE:
                            g.setColor(colors.get("Severity"));
                            break;
                        case CVE.TYPE:
                            g.setColor(colors.get("CVE"));
                            break;
                        case Reference.TYPE:
                            g.setColor(colors.get("Reference"));
                            break;
                        case CWE.TYPE:
                            g.setColor(colors.get("CWE"));
                            break;
                        default:
                            g.setColor(colors.get("DefaultVulnerability"));
                            break;
                    }
                }
                break;
                case AttackPattern.COLLECTION:
                    g.setColor(colors.get("AttackPattern"));
                    break;
                case Machine.COLLECTION:
                    g.setColor(colors.get("Machine"));
                    break;
                case Firewall.COLLECTION:
                    g.setColor(colors.get("Firewall"));
                    break;
                case Subnet.COLLECTION:
                    g.setColor(colors.get("Subnet"));
                    break;
                case Snort.COLLECTION:
                    g.setColor(colors.get("Snort"));
                    break;
                case MissionTask.COLLECTION:
                    g.setColor(colors.get("MissionTask"));
                    break;
                case MissionInformation.COLLECTION:
                    g.setColor(colors.get("MissionInformation"));
                    break;
                case CyberAsset.COLLECTION:
                    g.setColor(colors.get("CyberAsset"));
                    break;
                case MissionObjective.COLLECTION:
                    g.setColor(colors.get("MissionObjective"));
                    break;
                default:
                    g.setColor(colors.get("Default"));
                    break;
            }
            g.fillOval(n.x-nodeWidth/2, n.y-nodeHeight/2,
                    nodeWidth, nodeHeight);
            g.setColor(Color.black);
            g.drawOval(n.x-nodeWidth/2, n.y-nodeHeight/2,
                    nodeWidth, nodeHeight);

            g.drawString(n.name, n.x-f.stringWidth(n.name)/2,
                    n.y+f.getHeight()/2);
        }
    }

    public void drawArrow(Graphics g, double middleX, double middleY, double theta) {
        Graphics2D graphics2D = (Graphics2D) g;
//        Color oldColor;
//        oldColor = graphics2D.getColor();
//        graphics2D.setColor (color);

        int length = 50;
        Arrow arrow = new Arrow(
                middleX,
                middleY,
                middleX + length * Math.cos(theta),
                middleY + length * Math.sin(theta)
        );
        // paint the shaft
        graphics2D.draw (new Line2D.Double (arrow.x1,arrow.y1,arrow.x2,arrow.y2));

        // paint arrowhead
        arrow.arrHead (arrow.x1,arrow.y1,arrow.x2,arrow.y2);
        graphics2D.draw (new Line2D.Double (arrow.x2,arrow.y2,arrow.ax1,arrow.ay1));
        graphics2D.draw (new Line2D.Double (arrow.x2,arrow.y2,arrow.ax2,arrow.ay2));
//        graphics2D.setColor (oldColor);
    }

    public double[][] getNewLocations(int nodesNumber, double x, double y, double preX, double preY){
        double[][] locations = new double[nodesNumber][2];
        double theta = 0;
        double scopeSize = 2*Math.PI;
        long length = 150; // length of edge

        double m = getSlope(preX,preY,x,y);
        theta = Math.atan(m);

        if ((x < preX && y < preY) || (y > preY && x < preX) || (y == preY && x < preX)){
            theta += Math.PI;
        }
        if (y < preY && x > preX){
            theta += 2*Math.PI;
        }
        scopeSize = Math.PI/(nodesNumber+1);

        if (x == preX && y == preY){
            scopeSize *= (double) 2 * (nodesNumber + 1) /nodesNumber;

        }

        for (int i = 0;i<nodesNumber;i++){
            double k = theta - Math.PI/2 + (i+1) * scopeSize;
            locations[i][0] = x + length * Math.cos(k);
            locations[i][1] = y + length * Math.sin(k);
        }

        return locations;
    }

    public double getSlope(double x1,double y1,double x2,double y2){
        if (x2 == x1) {
            return y2 > y1 ? 5000 : -5000;
        }
        return (y2-y1)/(x2-x1);
    }
}
