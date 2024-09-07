package layouts;

import services.commands.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Launch extends JFrame {

    public Launch()
    {
        setTitle("CyGraph: Cyber Attack Graph Analytics and Visualization");
        Container cp = getContentPane();

        cp.setLayout(new FlowLayout(FlowLayout.LEFT));
        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);

        Button importData = new Button("IMPORT DATA");
        importData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new ImportVulnerabilitiesCommand();
                    new ImportCapecCommand();
                    new ImportSnortsCommand();
                    new ImportNetworkCommand();
                    new ImportMissionsCommand();
                    (new ImportCVECommand()).fire();
                    (new ImportCPECommand()).fire();
                    (new ImportCWECommand()).fire();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button query = new Button("QUERY");
        query.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new QueryLayout();
            }
        });
        toolbar.add(query);
        toolbar.addSeparator();

//        Button snorts = new Button("SNORTS");
//        snorts.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                new SnortsLayout();
//            }
//        });
//        toolbar.add(snorts);
//        toolbar.addSeparator();

        toolbar.add(importData);
        toolbar.addSeparator();

        cp.add(toolbar,BorderLayout.EAST);
        cp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pack();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setSize(1000,65);
    }

}
