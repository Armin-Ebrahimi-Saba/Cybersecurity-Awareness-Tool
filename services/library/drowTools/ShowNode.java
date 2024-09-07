package services.library.drowTools;

import models.*;
import models.missions.*;
import models.vulnerabilities.*;
import services.adapters.GetNodeFromNeo4j;
import services.adapters.ModelAdapter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ShowNode  extends JFrame {

    private Base node;

    private int x,y,diff;

    public ShowNode(long id){
        GetNodeFromNeo4j node = new GetNodeFromNeo4j(id);
        this.node = ModelAdapter.getNode(
                node.identifier,
                node.collection,
                node.type
        );
        this.x = 10;
        this.y = 50;
        diff = 0;
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setVisible(true);
        this.setSize(600,470);

        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                y += e.getWheelRotation();
                repaint();
            }
        });
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_UP){
                    y += 20;
                    repaint();
                }
                else if (ke.getKeyCode() == KeyEvent.VK_DOWN){
                    y -= 20;
                    repaint();
                }
            }
        });
    }

    public void paint(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0,0,1000000000,10000000);
        g.setColor(Color.black);
        g.drawString("collection :: "+ node.getCollection(),x,y);
        setY(50);
        g.drawString("title ::",x,y);
        printSentences(g,separatingString(node.title));
        g.drawString("description ::",x,y);
        printSentences(g,separatingString(node.description));
        if (node instanceof CPE){
            g.drawString("cpe23Uri ::",x,y);
            printSentences(g,separatingString(((CPE) node).cpe23Uri));
            g.drawString("vulnerable ::",x,y);
            printSentences(g,separatingString(String.valueOf(((CPE) node).vulnerable)));
        }
        else if (node instanceof CVE){
            g.drawString("id ::",x,y);
            printSentences(g,separatingString(String.valueOf((((CVE) node).id))));
        }
        else if (node instanceof CVSS){
            g.drawString("baseScore ::",x,y);
            printSentences(g,separatingString(String.valueOf((((CVSS) node).baseScore))));
            g.drawString("version :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((CVSS) node).version))));
            g.drawString("availabilityImpact :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((CVSS) node).availabilityImpact))));
            g.drawString("confidentialityImpact :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((CVSS) node).confidentialityImpact))));
            g.drawString("integrityImpact ::",x,y);
            printSentences(g,separatingString(String.valueOf((((CVSS) node).integrityImpact))));
            g.drawString("vectorString :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((CVSS) node).vectorString))));
        }
        else if (node instanceof CWE){
            g.drawString("id :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((CWE) node).id))));
        }
        else if (node instanceof Metric){
            g.drawString("impactScore :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Metric)node).impactScore))));
            g.drawString("exploitabilityScore :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Metric)node).exploitabilityScore))));
        }
        else if (node instanceof Reference){
            g.drawString("url :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Reference)node).url))));
            g.drawString("refsource :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Reference)node).refsource))));
            g.drawString("tags :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Reference)node).tags))));
        }
        else if (node instanceof Severity){
            g.drawString("severity :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Severity)node).severity))));
        }
        else if (node instanceof AttackPattern){
            g.drawString("id :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((AttackPattern) node).id))));
            g.drawString("status :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((AttackPattern) node).status))));
            g.drawString("severity :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((AttackPattern) node).severity))));
            g.drawString("likelihoodAttack :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((AttackPattern) node).likelihoodAttack))));
            g.drawString("taxonomyMappings :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((AttackPattern) node).taxonomyMappings))));
        }
        else if (node instanceof Firewall){

        }
        else if (node instanceof Machine){
            g.drawString("id :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Machine) node).id))));
            g.drawString("source_ip :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Machine) node).source_ip))));
            g.drawString("source_port :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Machine) node).source_port))));
            g.drawString("destination_ip :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Machine) node).destination_ip))));
            g.drawString("destination_port :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Machine) node).destination_port))));
            g.drawString("protocol :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Machine) node).protocol))));
            g.drawString("stage :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Machine) node).stage))));
        }
        else if (node instanceof Snort){
            g.drawString("id :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((Snort) node).id))));
        }
        else if (node instanceof Subnet){

        }
        else if (node instanceof MissionBase){
            g.drawString("nodeImpact :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((MissionBase) node).nodeImpact))));
            g.drawString("relativeWeight :: ",x,y);
            printSentences(g,separatingString(String.valueOf((((MissionBase) node).relativeWeight))));
        }
        y -= diff;
        diff = 0;
    }

    private String[] separatingString(String input){
        if (input == null){
            String[] str = new String[1];
            str[0] = "";
            return str;
        }
        String[] words = input.split(" ");
        int separateIndex = 10;
        int len = (words.length / separateIndex) + ((words.length % separateIndex != 0) ? 1 : 0);
        String[] result = new String[len];
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < words.length; i++){
            if ((i+1) % separateIndex == 0){
                result[(i+1)/separateIndex-1] = str.toString();
                str = new StringBuilder();
            }
            str.append(" ").append(words[i]);
        }
        if (words.length % separateIndex != 0){
            result[words.length / separateIndex] = str.toString();
        }
        return result;
    }

    private void printSentences(Graphics g, String[] sentences){
        int i = 1;
        int distance = 20;
        for (String str : sentences) {
            setY(i*distance);
            g.drawString(str,x,y);
        }
        setY(50);
    }

    public void setY(int diff) {
        this.y += diff;
        this.diff += diff;
    }
}
