package main;

import javax.swing.*;
import java.awt.*;

public class Screen extends JPanel {
    private Graphics gfx;
    private int scale = 12;
    private int width = 64 * scale;
    private int height = 32 * scale;
    private boolean graphics[][] = new boolean[32][64];

    public void paintPixel(boolean pixel, int x, int y){
        if(pixel){
            gfx.setColor(Color.WHITE);
        }
        else{
            gfx.setColor(Color.BLACK);
        }
        gfx.fillRect(x * scale, y * scale, scale, scale);
    }

    public boolean getPixel(int x, int y){
        return graphics[y][x];
    }

    public void setPixel(int x, int y){
        boolean i = graphics[y][x] ^ true;
        graphics[y][x] = i;
    }

    public void paintScreen(){
        repaint();
    }

    public void paintFullScreen(){
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                paintPixel(graphics[y][x], x, y);
            }
        }
    }

    public void clearScreen(){
        for(int y = 0; y < 32; y++){
            for(int x = 0; x < 64; x++){
                this.graphics[y][x] = false;
            }
        }
    }

    @Override
    public void paintComponent(Graphics gfx){
        super.paintComponent(gfx);
        this.gfx = gfx;
        gfx.setColor(Color.BLACK);
        gfx.fillRect(0, 0, width, height);
        paintFullScreen();
    }
}
