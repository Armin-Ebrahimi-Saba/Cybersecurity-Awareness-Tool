package services.library.drowTools;

import javax.swing.*;

public class DynamicsBuilder {

    public DynamicsBuilder(int days, int hours, int minutes, int seconds, JButton nextButton, JButton previousButton) {
        int time = seconds + (minutes*60) + (hours*60*60) + (days*24*60*60);
        nextButton.setEnabled(false);
//        previousButton.setEnabled(false);
    }
}
