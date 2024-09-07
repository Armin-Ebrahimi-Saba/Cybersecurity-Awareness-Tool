package layouts;

import models.Query;
import services.adapters.QueryBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public class QueryLayout extends JFrame {


    private HashMap<String, Color> createColors() {
        HashMap<String, Color> colors = new HashMap<>();
        colors.put("CPE", new Color(93, 93, 138));
        colors.put("CVSS", new Color(255, 0, 0));
        colors.put("Metric", new Color(150, 60, 60));
        colors.put("Severity", new Color(169, 40, 40));
        colors.put("CVE", new Color(7, 124, 2));
        colors.put("Reference", new Color(12, 255, 0));
        colors.put("CWE", new Color(255, 128, 0));
        colors.put("DefaultVulnerability", new Color(243, 0, 0));
        colors.put("AttackPattern", new Color(57, 255, 255));
        colors.put("Machine", new Color(170, 0, 255));
        colors.put("Firewall", new Color(36, 150, 132));
        colors.put("Subnet", new Color(255, 233, 0));
        colors.put("Snort", new Color(121, 0, 0));
        colors.put("MissionTask", new Color(2, 114, 154));
        colors.put("MissionInformation", new Color(164, 124, 189));
        colors.put("CyberAsset", new Color(54, 12, 87, 65));
        colors.put("MissionObjective", new Color(54, 12, 87, 245));
        colors.put("Default", new Color(15, 255, 0));
        colors.put("Edge", Color.black);
        colors.put("Background", Color.white);

        return colors;
    }

    public QueryLayout(){
        Container cp = getContentPane();
        setTitle("Define Graph Database Query");
        cp.setLayout(new FlowLayout());
        cp.add(new JLabel("Edit your Graph query, then press the button below to execute ..."));
        JTextField txt = new JTextField(35);
        cp.add(txt);

        ArrayList<String> data = new ArrayList<String>();
        ArrayList<Query> queries= Query.getAllQueries();
        if (queries.size() != 0){
            for (Query query : queries){
                data.add(query.query);
            }
        }

        JComboBox list = new JComboBox(data.toArray());
        list.insertItemAt("", 0);
        list.setSelectedIndex(0);
        list.setPreferredSize(new Dimension(440,20));
        list.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                txt.setText((String)cb.getSelectedItem());
            }

        });
        cp.add(list);

        Button query = new Button("Execute Query");
        query.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (txt.getText().isBlank() || txt.getText().isEmpty()){
                    new MistakeQuery();
                }
                else {
                    new QueryBuilder(txt.getText(),"CyGraph", createColors());
                    list.removeAllItems();
                    if (queries.size() != 0) {
                        for (Query query : queries) {
                            list.addItem(query.query);
                        }
                    }
                    list.insertItemAt("", 0);
                    list.setSelectedIndex(0);
                }
            }
        });
        cp.add(query);

        Button cancel = new Button("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        cp.add(cancel);

        Button history = new Button("History");
        history.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new QueryHistoryLayout(createColors());
            }
        });
        cp.add(history,BorderLayout.WEST);

        pack();
        setVisible(true);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(445,150);
        setLocation(1000,0);
    }
}
