package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Chip8 {
    private short opcode;
    private char[] memory;
    private char cReg[];
    private short iReg;
    private short pCount;
    private char[] gfx;
    private char delayTimer;
    private char soundTimer;
    private short[] stack;
    private short sPoint;
    private char[] key;

    public Chip8(){
        this.pCount = 0x200;
        this.opcode = 0;
        this.iReg = 0;
        this.sPoint = 0;
        this.memory = new char[4096];
        this.cReg = new char[16];
        this.gfx = new char[2048];
        this.stack = new short[16];
        this.key = new char[16];
    }

    public void loadGame(String filename){
        try{
            FileInputStream fis = new FileInputStream("roms/" + filename);
            int ch = 0;
            try{
                while((ch = fis.read()) != -1){
                    System.out.println(ch);
                }
            }catch (IOException e){
                System.out.println("End of file unexpectedly reached.");
            }

        }catch(FileNotFoundException e){
            System.out.println("File does not exist. Check file name and try again.");
        }


    }
}
