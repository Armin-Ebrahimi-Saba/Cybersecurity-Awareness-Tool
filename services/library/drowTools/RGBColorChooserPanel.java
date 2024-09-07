package services.library.drowTools;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class RGBColorChooserPanel extends JDialog {

    private final JColorChooser jCC;
    private final JPanel        panel;

    public RGBColorChooserPanel(String title,Color color, FilterPanel filterPanel) {
        super();
        this.setTitle(title);
        this.jCC = new JColorChooser();
        this.modifyJColorChooser();
        this.panel = new JPanel() {
            @Override
            protected void paintComponent(final Graphics g) {
                final Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

                super.paintComponent(g);
            }
        };
        this.panel.setLayout(new GridBagLayout());

        this.panel.add(this.jCC);
        this.panel.setOpaque(false);
        this.jCC.setOpaque(false);
        this.jCC.setPreviewPanel(new JPanel());
        this.jCC.setColor(color);
        this.add(this.panel, BorderLayout.CENTER);

        JButton button = new JButton("OK");
        this.jCC.getColor();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterPanel.getCanvas().setBackgroundColor(jCC.getColor());
            }
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel pbutton = new JPanel();
        pbutton.add(button);
        pbutton.add(cancel);
        this.add(pbutton,BorderLayout.SOUTH);

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.pack();

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((screenSize.width - this.getWidth()) / 2,
                (screenSize.height - this.getHeight()) / 2);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void modifyJColorChooser() {
        final AbstractColorChooserPanel[] panels = this.jCC.getChooserPanels();
        for (final AbstractColorChooserPanel accp : panels) {
            if (!accp.getDisplayName().equals("RGB")) {
                this.jCC.removeChooserPanel(accp);
            }
        }

        final AbstractColorChooserPanel[] colorPanels = this.jCC.getChooserPanels();
        final AbstractColorChooserPanel cp = colorPanels[0];

        Field f = null;
        try {
            f = cp.getClass().getDeclaredField("panel");
        } catch (NoSuchFieldException | SecurityException e) {

        }
        f.setAccessible(true);

        Object colorPanel = null;
        try {
            colorPanel = f.get(cp);
        } catch (IllegalArgumentException | IllegalAccessException e) {

        }

        Field f2 = null;
        try {
            f2 = colorPanel.getClass().getDeclaredField("spinners");
        } catch (NoSuchFieldException | SecurityException e4) {

        }
        f2.setAccessible(true);
        Object rows = null;
        try {
            rows = f2.get(colorPanel);
        } catch (IllegalArgumentException | IllegalAccessException e3) {

        }

        final Object transpSlispinner = Array.get(rows, 3);
        Field f3 = null;
        try {
            f3 = transpSlispinner.getClass().getDeclaredField("slider");
        } catch (NoSuchFieldException | SecurityException e) {

        }
        f3.setAccessible(true);
        JSlider slider = null;
        try {
            slider = (JSlider) f3.get(transpSlispinner);
        } catch (IllegalArgumentException | IllegalAccessException e2) {

        }
        slider.setVisible(false);
        Field f4 = null;
        try {
            f4 = transpSlispinner.getClass().getDeclaredField("spinner");
        } catch (NoSuchFieldException | SecurityException e1) {

        }
        f4.setAccessible(true);
        JSpinner spinner = null;
        try {
            spinner = (JSpinner) f4.get(transpSlispinner);
        } catch (IllegalArgumentException | IllegalAccessException e) {

        }
        spinner.setVisible(false);
        Field f5 = null;
        try {
            f5 = transpSlispinner.getClass().getDeclaredField("label");
        } catch (NoSuchFieldException | SecurityException e1) {

        }
        f5.setAccessible(true);
        JLabel label = null;
        try {
            label = (JLabel) f5.get(transpSlispinner);
        } catch (IllegalArgumentException | IllegalAccessException e) {

        }
        label.setVisible(false);

        Field f6 = null;
        try {
            f6 = transpSlispinner.getClass().getDeclaredField("value");
        } catch (NoSuchFieldException | SecurityException e1) {

        }
        f6.setAccessible(true);
        float value = 0;
        try {
            value = (float) f6.get(transpSlispinner);
        } catch (IllegalArgumentException | IllegalAccessException e) {

        }
    }
}
