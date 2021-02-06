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
    char[] fontSet =
    {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

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
        int instCount = 0;
        try{
            FileInputStream fis = new FileInputStream("roms/" + filename);
            int ch = 0;
            char memIndex = 0x200;
            try{
                while((ch = fis.read()) != -1){
                    System.out.println(Integer.toBinaryString(ch));
                    this.memory[memIndex] = (char) ch;
                    memIndex++;
                }
            }catch (IOException e){
                System.out.println("End of file unexpectedly reached.");
            }

        }catch(FileNotFoundException e){
            System.out.println("File does not exist. Check file name and try again.");
        }
    }

    public void emulateCycle(){
        System.out.println(Integer.toBinaryString(memory[pCount]));
        System.out.println(Integer.toBinaryString(memory[pCount+1]));
        this.opcode = (short) (memory[pCount] << 8 | memory[pCount + 1]);
        System.out.println(Integer.toBinaryString(this.opcode));
    }
}
