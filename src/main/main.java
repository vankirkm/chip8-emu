package main;

public class main {

    public static void main(String[] args){
        String filename = "Pong.ch8";
        Chip8 newChip = new Chip8();
        newChip.loadGame(filename);
        newChip.emulateCycle();
    }
}
