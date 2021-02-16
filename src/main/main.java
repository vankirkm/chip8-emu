package main;

public class main {

    public static void main(String[] args){
        String filename = "INVADERS";
        Chip8 newChip = new Chip8();
        newChip.loadGame(filename);
        /*while(true){
            newChip.emulateCycle();
        }*/
        for(;;){
            newChip.emulateCycle();
        }
    }
}
