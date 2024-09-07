package layouts;

import javax.swing.*;
import java.awt.*;

public class MistakeQuery extends JFrame {

    public MistakeQuery(){
        setTitle("ERROR");
        Container cp = getContentPane();
        cp.setLayout(new FlowLayout());
        JLabel label = new JLabel("an Error occurred in Query!!");
        cp.add(label);
        setVisible(true);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(300,70);
        setLocation(500,400);
    }
}
