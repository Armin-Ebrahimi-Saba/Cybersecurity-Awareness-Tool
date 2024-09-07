package layouts;

import models.Query;
import services.adapters.QueryBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public class QueryHistoryLayout extends JFrame {

    public QueryHistoryLayout(HashMap<String, Color> colors){
        Container cp = getContentPane();
        setTitle("Select Graph Database Query");
        cp.setLayout(new FlowLayout());


        ArrayList<String> data = new ArrayList<String>();
        for (Query query : Query.getAllQueries()){
            data.add(query.query);
        }
        JComboBox list = new JComboBox(data.toArray());
        list.add(new ScrollPane());
        list.setSelectedIndex(0);
        list.setPreferredSize(new Dimension(440,20));
        cp.add(list);

        Button query = new Button("Execute Query");
        query.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String txt = (String) list.getSelectedItem();

                if (txt == null || txt.isEmpty() || txt.isBlank()){
                    new MistakeQuery();
                }
                else {
                    new QueryBuilder(txt,"CyGraph", colors);
                    list.removeAllItems();
                    for (Query query : Query.getAllQueries()){
                        list.addItem(query.query);
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
        
        pack();
        setVisible(true);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(445,100);
        setLocation(1000,175);
    }
}
