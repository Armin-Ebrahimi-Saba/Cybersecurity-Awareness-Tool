package layouts;

import models.Snort;
import org.neo4j.graphdb.Node;
import services.library.cygraph.GraphGeneratorWithBFS;
import services.library.cygraph.GraphGeneratorWithDFS;
import services.library.neo4j.Neo4jActivities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class SnortsLayout extends JFrame {
    JComboBox list;
    private static String snort_id;
    private static boolean isDFS = false;

    public SnortsLayout(){
        setTitle("Select Snort");
        Container cp = getContentPane();
        cp.setLayout(new FlowLayout(FlowLayout.CENTER));

        ArrayList data = new ArrayList();

        for (Snort snort : Snort.getAllSnorts()){
            data.add(snort.id);
        }

        list = new JComboBox(data.toArray());
        list.setSelectedIndex(0);
        list.setPreferredSize(new Dimension(200,20));
        list.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                snort_id = (String)cb.getSelectedItem();
            }

        });

        JRadioButton r1=new JRadioButton("DFS");
        JRadioButton r2=new JRadioButton("BFS");
        ButtonGroup bg=new ButtonGroup();
        r2.setSelected(true);
        bg.add(r1);
        bg.add(r2);
        r1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(((JRadioButton)e.getSource()).isSelected()){
                    isDFS = true;
                }
            }
        });
        r2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(((JRadioButton)e.getSource()).isSelected()){
                    isDFS = false;
                }
            }
        });


        Button snortSelector = new Button("SELECT SNORT");
        snortSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Snort snort = Snort.findById(snort_id);
                try {
                    Node origin = Neo4jActivities.storeOrGetNode(snort._id,snort.title,snort.getCollection(),null);
                    if (isDFS){
                        new GraphGeneratorWithDFS(origin);
                    }
                    else {
                        new GraphGeneratorWithBFS(origin);
                    }
                } catch (IOException ex) {
                    new MistakeQuery();
                }
            }
        });

        Button cancel = new Button("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        cp.add(new JScrollPane(list), BorderLayout.EAST);
        cp.add(r1, BorderLayout.EAST);
        cp.add(r2, BorderLayout.EAST);
        cp.add(snortSelector,BorderLayout.NORTH);
        cp.add(cancel,BorderLayout.NORTH);
        pack();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setSize(445,100);
        setLocation(1000,175);
    }
}
