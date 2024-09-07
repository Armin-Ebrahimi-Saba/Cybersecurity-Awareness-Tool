package layouts;

import services.adapters.GroupingBuilder;
import services.adapters.LayeringBuilder;
import services.adapters.MissionsHierarchyBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;


public class AnalyzeLayout extends JFrame {

    public AnalyzeLayout(String query, HashMap<String, Color> colors) {

        setTitle("Analytical features");
        Container cp = getContentPane();
        cp.setLayout(new FlowLayout(FlowLayout.LEFT));
        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);

        Button grouping = new Button("NETWORK GROUPING");
        grouping.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new GroupingBuilder(query, colors);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolbar.add(grouping);
        toolbar.addSeparator();

        Button layering = new Button("LAYERING");
        layering.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new LayeringBuilder(query, colors);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolbar.add(layering);
        toolbar.addSeparator();

        Button missions_hierarchy = new Button("MISSIONS DEPENDENCY HIERARCHY");
        missions_hierarchy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new MissionsHierarchyBuilder(query, colors);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolbar.add(missions_hierarchy);
        toolbar.addSeparator();

        Button filtering = new Button("FILTERING");
        filtering.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new FilteringLayout(query, colors);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolbar.add(filtering);
        toolbar.addSeparator();

        Button dynamics = new Button("Dynamics");
        dynamics.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new DynamicsLayout(query, colors);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolbar.add(dynamics);
        toolbar.addSeparator();

        cp.add(toolbar,BorderLayout.EAST);
        cp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pack();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setSize(1000,65);
        setLocation(0,57);
    }
}
