package main;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Display extends JFrame{
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;
    private int scale = 12;
    public int[][] graphics = new int[WIDTH][HEIGHT];

    public Display(){
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
        graphics.setPreferredSize(new Dimension(640, 480));
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
        this.setSize(500,300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }


}
