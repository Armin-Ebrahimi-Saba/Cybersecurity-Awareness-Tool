package layouts;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.graphdb.Node;
import services.adapters.ExecuteBuilder;
import services.adapters.QueryBuilder;
import services.db.DBConnection;
import services.library.cygraph.GraphChange;
import services.library.drowTools.InfoChange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.*;
import java.util.List;


public class ExecuteDynamicsLayout extends JFrame {

    public ExecuteDynamicsLayout(String query, int days, int hours, int minutes, int seconds, HashMap<String, Color> colors) {

        setTitle("Execute Dynamics");
        setLayout(new FlowLayout());

        JPanel messagePanel = new JPanel();
        JLabel messageLabel = new JLabel();
        messageLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        messageLabel.setFont(new Font("Serif", Font.BOLD, 15));
        messagePanel.add(messageLabel);
        add(messagePanel);

        ExecuteBuilder builder = new ExecuteBuilder();
        builder.ExecuteBuilder(query);
        int sizeNodes = builder.getNodes().size();
        int sizeEdges = builder.getEdges().size();

        int desiredTime = seconds + (minutes * 60) + (hours * 60 * 60) + (days * 24 * 60 * 60);
        long time = Clock.systemUTC().millis() / 1000 - desiredTime;

        TreeMap<Long, ArrayList<Document>> allChanges = new TreeMap();
        DBConnection dbConnection = new DBConnection("changes");
        ArrayList<Document> documents = dbConnection.getAll();
        for (Document document : documents) {
            long t = (long) document.get("time") / 1000;
            if (t > time) {
                ArrayList<Document> temp = new ArrayList<>();
                if (allChanges.containsKey(t)) {
                    temp = allChanges.get(t);
                    temp.add(document);
                } else {
                    temp.add(document);
                }
                allChanges.put(t, temp);
            }
        }

        List<Long> t = new ArrayList<>();
        t.addAll(allChanges.keySet());
        Collections.sort(t);

        TreeMap<Long, ArrayList<Document>> changes = new TreeMap();
        ArrayList<Long> clocks = new ArrayList();

        for(int i = t.size()-1; i >= 0; i--) {
            for (Document document : allChanges.get(t.get(i))) {
                clocks.add((Long) document.get("time"));
            }
            Collections.sort(clocks);

            for (int k = clocks.size()-1; k >= 0; k--) {
                for (Document document : allChanges.get(t.get(i))) {
                    if (document.get("time").equals(clocks.get(k))) {
                        if (document.get("document_type").equals("node")) {
                            if (document.get("operation").equals("add")) {
                                Node node = builder.storeOrGetNode((ObjectId) document.get("identifier"), (String) document.get("name"),
                                        (String) document.get("collection"), (String) document.get("type"));
                                builder.removeNodeAndRelations(node);
                            }
                            if (document.get("operation").equals("delete")) {
                                builder.storeOrGetNode((ObjectId) document.get("identifier"), (String) document.get("name"),
                                        (String) document.get("collection"), (String) document.get("type"));
                                for (Document relation: (ArrayList <Document>) document.get("relations")) {
                                    try {
                                        builder.relating((String) relation.get("startNode"), (String) relation.get("endNode"), (String) relation.get("relationName"));
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (document.get("document_type").equals("relation")) {
                            if (document.get("operation").equals("add")) {
                                builder.removeRelationship((String) document.get("startNode"), (String) document.get("endNode"), (String) document.get("relationName"));
                            }
                            if (document.get("operation").equals("delete")) {
                                try {
                                    builder.relating((String) document.get("startNode"), (String) document.get("endNode"), (String) document.get("relationName"));
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        builder.ExecuteBuilder(query);
                        int sizeNewNodes = builder.getNodes().size();
                        int sizeNewEdges = builder.getEdges().size();
                        if ((sizeNewNodes != sizeNodes) || (sizeNewEdges != sizeEdges)) {
                            ArrayList<Document> temp = new ArrayList();
                            if (changes.containsKey(t.get(i))) {
                                temp = changes.get(t.get(i));
                                temp.add(document);
                            } else {
                                temp.add(document);
                            }
                            changes.put(t.get(i),temp);
                            sizeNodes = sizeNewNodes;
                            sizeEdges = sizeNewEdges;
                        }
                    }
                }
            }
        }

        clocks = new ArrayList<>();
        for(int i = 0; i < t.size(); i++) {
            for (Document document : allChanges.get(t.get(i))) {
                clocks.add((Long) document.get("time"));
            }
            Collections.sort(clocks);

            for (int k = 0; k < clocks.size(); k++) {
                for (Document document : allChanges.get(t.get(i))) {
                    if (document.get("time").equals(clocks.get(k))) {
                        if (document.get("document_type").equals("node")) {
                            if (document.get("operation").equals("add")) {
                                builder.storeOrGetNode((ObjectId) document.get("identifier"), (String) document.get("name"),
                                        (String) document.get("collection"), (String) document.get("type"));
                                Document relation = (Document) document.get("relation");
                                try {
                                    builder.relating((String) relation.get("startNode"), (String) relation.get("endNode"), (String) relation.get("relationName"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (document.get("operation").equals("delete")) {
                                Node node = builder.storeOrGetNode((ObjectId) document.get("identifier"), (String) document.get("name"),
                                        (String) document.get("collection"), (String) document.get("type"));
                                for (Document relation: (ArrayList <Document>) document.get("relations")) {
                                    builder.removeRelationship((String) relation.get("startNode"), (String) relation.get("endNode"), (String) relation.get("relationName"));
                                }
                                builder.removeNodeAndRelations(node);
                            }
                        }
                        if (document.get("document_type").equals("relation")) {
                            if (document.get("operation").equals("add")) {
                                try {
                                    builder.relating((String) document.get("startNode"), (String) document.get("endNode"), (String) document.get("relationName"));
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            if (document.get("operation").equals("delete")) {
                                builder.removeRelationship((String) document.get("startNode"), (String) document.get("endNode"), (String) document.get("relationName"));
                            }
                        }
                    }
                }
            }
        }
        builder.shutDown();

        String message;
        if (changes.isEmpty()) {
            message = "No changes have occurred on the graph.";
        } else {
            message = "Changes have occurred in " + changes.size() + " times on the graph";
        }
        messageLabel.setText(message);

        Set<Long> setIndexes = changes.keySet();
        List<Long> times = new ArrayList<>();
        times.addAll(setIndexes);
        Collections.sort(times);

        JPanel panel = new JPanel(new FlowLayout());
        JPanel labelsPanel = new JPanel(new GridLayout(0, 1,5,5));
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 1,5,5));

        DateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

        if(!times.isEmpty()) {
            for (int i = 0; i <= times.size(); i++) {
                JPanel labelPanel = new JPanel();
                JLabel label = new JLabel();
                String labelMessage;
                if (i == 0) {
                    String g = sdf.format(times.get(i) * 1000);
                    labelMessage = "Graph status before " + g;
                    label.setText(labelMessage);
                } else if (i == times.size()) {
                    String h = sdf.format(times.get(i - 1) * 1000);
                    labelMessage = "Graph status after " + h;
                    label.setText(labelMessage);
                } else {
                    String g = sdf.format(times.get(i) * 1000);
                    String h = sdf.format(times.get(i - 1) * 1000);
                    labelMessage = "Graph status from " + h + " to " + g;
                    label.setText(labelMessage);
                }
                labelPanel.add(label);
                labelsPanel.add(labelPanel);
                JButton executeButton = new JButton("execute");
                int finalI = i;
                String finalMessage = labelMessage;
                buttonsPanel.add(executeButton);

                label.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {

                        if (finalI != 0) {
                            Long clock = times.get(finalI - 1);
                            ArrayList<Document> temp = new ArrayList();
                            ArrayList<Long> clocks = new ArrayList();
                            for (Document document : changes.get(clock))
                                clocks.add((Long) document.get("time"));
                            Collections.sort(clocks);
                            for (int k = 0; k < clocks.size(); k++) {
                                for (Document document : changes.get(clock)) {
                                    if (document.get("time").equals(clocks.get(k)))
                                        temp.add(document);
                                }
                            }
                            new InfoChange(temp);
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });

                GraphChange graphChange = new GraphChange(changes);

                executeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ArrayList<Long> subarray = new ArrayList();
                        ArrayList<Long> arraysub = new ArrayList();

                        for (int j = times.size() - 1; j >= finalI; j--)
                            subarray.add(times.get(j));
                        for (int j = finalI; j < times.size(); j++)
                            arraysub.add(times.get(j));

                        graphChange.applyChanges(subarray);
                        new QueryBuilder(query, finalMessage, colors);
                        graphChange.undoChanges(arraysub);
                    }
                });

            }
        }

        panel.add(labelsPanel);
        panel.add(buttonsPanel);
        JScrollPane scroll = new JScrollPane(panel);
        setSize(510, 210);
        scroll.setPreferredSize(new Dimension(500,132));
        add(scroll);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setLocation(450, 57);

    }
}
