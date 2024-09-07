package services.library.drowTools;

import java.awt.*;

public class DrawingCanvas extends Canvas {

    Color color;

    public DrawingCanvas(Color color) {
        setSize(25, 25);
        setBackgroundColor(color);
    }

    public void setBackgroundColor(Color color) {
        this.color = color;
        setBackground(color);
    }

    public Color getColor() {
        return color;
    }
}
