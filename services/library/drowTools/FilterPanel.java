package services.library.drowTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FilterPanel extends JPanel {

    DrawingCanvas canvas;
    JButton button;
    String panel_name;

    public FilterPanel(String panel_name, Color color) {

        setLayout(new GridLayout(1, 3));

        this.panel_name = panel_name;
        JLabel label = new JLabel(panel_name);
        JPanel plabel1 = new JPanel();

        plabel1.add(label);

        button = new JButton("Change");
        button.addActionListener(new helpAction(panel_name,color,this));
        JPanel pbutton1 = new JPanel();
        pbutton1.add(button);

        JPanel pcanvas = new JPanel();
        canvas = new DrawingCanvas(color);
        pcanvas.add(canvas);

        add(plabel1);
        add(pcanvas);
        add(pbutton1);
    }

    public DrawingCanvas getCanvas() {
        return canvas;
    }

    class helpAction implements ActionListener {
        FilterPanel fp;
        String panel_name;
        Color color;
        public helpAction(String panel_name,Color color, FilterPanel fp) {
            this.fp = fp;
            this.panel_name = panel_name;
            this.color = color;
        }


        public void actionPerformed(ActionEvent e) {
            new RGBColorChooserPanel(panel_name,color,fp);
        }
    }

    public String getPanel_name() {
        return panel_name;
    }
}
