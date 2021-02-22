package main;

import javax.swing.*;

public class main {

    public static void main(String[] args){
        String filename = "TANK";
        Chip8 newChip = new Chip8();
        newChip.loadGame(filename);
        while(true){
            newChip.emulateCycle();
        }
    }
}
