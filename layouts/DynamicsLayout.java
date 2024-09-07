package layouts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class DynamicsLayout extends JFrame {

    public DynamicsLayout(String query, HashMap<String, Color> colors) {

        setTitle("Dynamics");
        Container cp = getContentPane();
        cp.setLayout(new FlowLayout());

        JLabel label = new JLabel("Enter the desired time to check the attack graph");
        cp.add(label);

        JPanel secondPanel = new JPanel();
        secondPanel.setLayout(new GridLayout(2,2));

        JPanel panel1 = new JPanel();
        JLabel days = new JLabel(" Number of Days : ");
        days.setLocation(0, 0);
        days.setSize(70, 40);
        days.setHorizontalAlignment(4);
        panel1.add(days);

        JTextField daysField = new JTextField("0",5);
        daysField.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        daysField.setLocation(0, 0);
        daysField.setSize(100, 30);
        panel1.add(daysField);
        secondPanel.add(panel1);

        JPanel panel2 = new JPanel();
        JLabel hours = new JLabel(" Number of Hours : ");
        hours.setLocation(0, 0);
        hours.setSize(70, 40);
        hours.setHorizontalAlignment(4);
        panel2.add(hours);

        JTextField hoursField = new JTextField("0",5);
        hoursField.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        hoursField.setLocation(0, 0);
        hoursField.setSize(100, 30);
        panel2.add(hoursField);
        secondPanel.add(panel2);

        JPanel panel3 = new JPanel();
        JLabel minutes = new JLabel(" Number of Minutes : ");
        minutes.setLocation(0, 0);
        minutes.setSize(70, 40);
        minutes.setHorizontalAlignment(4);
        panel3.add(minutes);

        JTextField minutesField = new JTextField("0",5);
        minutesField.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        minutesField.setLocation(0, 0);
        minutesField.setSize(100, 30);
        panel3.add(minutesField);
        secondPanel.add(panel3);

        JPanel panel4 = new JPanel();
        JLabel seconds = new JLabel(" Number of Seconds : ");
        seconds.setLocation(0, 0);
        seconds.setSize(70, 40);
        seconds.setHorizontalAlignment(4);
        panel4.add(seconds);

        JTextField secondsField = new JTextField("0",5);
        secondsField.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        secondsField.setLocation(0, 0);
        secondsField.setSize(100, 30);
        panel4.add(secondsField);
        secondPanel.add(panel4);
        cp.add(secondPanel);

        JPanel thirdPanel = new JPanel();
        thirdPanel.setLayout(new GridLayout(2,1));
        JPanel panel5 = new JPanel();
        JLabel warningLabel = new JLabel();
        warningLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        warningLabel.setForeground(Color.red);
        warningLabel.setSize(70, 40);
        panel5.add(warningLabel);

        JPanel panel6 = new JPanel();
        JButton applyButton = new JButton("Apply");
        applyButton.setLocation(0, 0);
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    warningLabel.setText("");
                    int days = Integer.parseInt(daysField.getText());
                    int hours = Integer.parseInt(hoursField.getText());
                    int minutes = Integer.parseInt(minutesField.getText());
                    int seconds = Integer.parseInt(secondsField.getText());
                    new ExecuteDynamicsLayout(query, days, hours, minutes, seconds, colors);
                } catch (Exception ex) {
                    warningLabel.setText("The input is incorrect.");
                }
            }
        });
        panel6.add(applyButton);
        thirdPanel.add(panel6);
        thirdPanel.add(panel5);
        cp.add(thirdPanel);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setSize(450,200);
        setLocation(0,57);

    }
}
