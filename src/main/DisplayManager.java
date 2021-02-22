package main;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;


public class DisplayManager extends JFrame{
    private Screen media;
    private JButton resetButton;
    private JComboBox gameList;

    public DisplayManager(){

        //set UI look and feel to Nimbus
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        media = new Screen();
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
        debug.setLayout(new BoxLayout(debug, BoxLayout.Y_AXIS));
        debug.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));

        JPanel graphics = new JPanel();
        graphics.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
        graphics.setPreferredSize(new Dimension(700, 350));
        graphics.add(media);

        Box gameSelectPanel = Box.createVerticalBox();
        gameSelectPanel.setMaximumSize(new Dimension(200, 200));
        gameSelectPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel gameSelectText = new JLabel("Game Select");
        gameSelectText.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.gameList = new JComboBox(getGameList().toArray());
        gameList.setMaximumSize(new Dimension(400,30));
        gameList.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.resetButton = new JButton("Reset Console");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        debug.add(gameSelectText);
        debug.add(gameList);
        debug.add(resetButton);

        JPanel manual = new JPanel();
        manual.setAlignmentX(Component.CENTER_ALIGNMENT);
        manual.setLayout(new BoxLayout(manual, BoxLayout.Y_AXIS));
        manual.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
        manual.setMaximumSize(new Dimension(640, 400));
        JLabel manLabel = new JLabel("Welcome to the Chip8 Emulator");
        manLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel reset = new JLabel("Resetting the Console\n");
        reset.setAlignmentX(Component.CENTER_ALIGNMENT);
        manual.add(manLabel);
        manual.add(Box.createRigidArea(new Dimension(600,10)));
        manual.add(reset);

        mainDisplay.add(graphics);
        mainDisplay.add(Box.createRigidArea(new Dimension(640, 10)));
        mainDisplay.add(manual);
        root.add(mainDisplay);
        root.add(debug);

        this.getContentPane().add(root);
        this.setTitle("CHIP-8 Emu");
        this.setIconImage(new ImageIcon("img\\logo.jpg").getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(1000, 530));
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

    //Get list of roms available in roms folder
    public ArrayList<String> getGameList(){
        ArrayList<String> gameList = new ArrayList();
        File romFolder = new File("roms/");
        for(File rom : romFolder.listFiles()){
            gameList.add(rom.getName());
        }
        return gameList;
    }

    //Get name of current selected game in gameList
    public String getSelectedGame(){
        return gameList.getSelectedItem().toString();
    }

    //Add actionListener from Chip8 class to resetButton
    public void addListenerToReset(ActionListener al){
        resetButton.addActionListener(al);
    }

}
