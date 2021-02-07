package main;

public class main {

    public static void main(String[] args){
        String filename = "test_opcode.ch8";
        Chip8 newChip = new Chip8();
        newChip.loadGame(filename);
        /*while(true){
            newChip.emulateCycle();
        }*/
        for(int i = 0; i < 20; i++){
            newChip.emulateCycle();
        }

    }
}
