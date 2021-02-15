package main;

import javax.swing.*;
import java.awt.*;

public class Screen extends JPanel {
    private Graphics gfx;
    private int scale = 12;
    private int width = 64 * scale;
    private int height = 32 * scale;
    private int graphics[][] = new int[32][64];

    public void paintPixel(boolean white, int x, int y){
        if(white){
            gfx.setColor(Color.WHITE);
        }
        else{
            gfx.setColor(Color.BLACK);
        }
        gfx.fillRect(x * scale, y * scale, scale, scale);
    }

    public int getPixel(int x, int y){
        return graphics[x][y];
    }

    public void setPixel(int x, int y){
        this.graphics[x][y] ^= 1;
    }

    public void paintScreen(){
        repaint();
    }

    public void paintFullScreen(){
        boolean value;
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if(graphics[y][x] == 0){
                    value = false;
                }
                else{
                    value = true;
                }
                paintPixel(value, x, y);
            }
        }
    }

    public void paintComponent(Graphics gfx){
        super.paintComponent(gfx);
        this.gfx = gfx;
        gfx.setColor(Color.WHITE);
        gfx.fillRect(0, 0, width, height);
        paintFullScreen();
    }
}
