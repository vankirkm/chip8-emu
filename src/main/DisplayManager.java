package main;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class DisplayManager extends JFrame{
    private Screen media;

    public DisplayManager(){
        this.media = new Screen();
        media.setPreferredSize(new Dimension(640,320));
        Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.X_AXIS));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel mainDisplay = new JPanel();
        mainDisplay.setLayout(new BoxLayout(mainDisplay, BoxLayout.Y_AXIS));
        mainDisplay.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel debug = new JPanel();
        debug.setPreferredSize(new Dimension(200,600));
        debug.setLayout(new BoxLayout(debug, BoxLayout.Y_AXIS));
        debug.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));

        JPanel graphics = new JPanel();
        graphics.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
        graphics.setPreferredSize(new Dimension(1000, 1000));
        graphics.add(media);

        JPanel manual = new JPanel();
        manual.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
        manual.setPreferredSize(new Dimension(640, 200));
        mainDisplay.add(graphics);
        mainDisplay.add(Box.createRigidArea(new Dimension(640, 10)));
        mainDisplay.add(manual);
        root.add(mainDisplay);
        root.add(debug);

        this.getContentPane().add(root);
        this.setTitle("CHIP-8 Emu");
        this.setIconImage(new ImageIcon("img\\logo.jpg").getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public void updateScreen(){
        this.media.paintScreen();
    }

    public boolean getScreenPixel(int x, int y){
        return this.media.getPixel(x,y);
    }

    public void setScreenPixel(boolean p, int x, int y){
        this.media.setPixel(p, x, y);
    }

    public void clearGameScreen(){
        this.media.clearScreen();
    }

}
