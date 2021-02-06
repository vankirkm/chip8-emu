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
        //get opcode
        this.opcode = (short) (memory[pCount] << 8 | memory[pCount + 1]);
        System.out.println(Integer.toHexString(this.opcode & 0xF000));
        //decode opcode
        switch(this.opcode & 0xF000){

            //multiple cases where the first 4 bits of the opcode are 0, so break into another switch to look at
            //the last two bytes of the opcode
            case 0x0000:
                break;

            // 1nnn - set pCount to nnn
            case 0x1000:
                break;

            // 2nnn - increment sPoint, then put the current pCount on top of the stack. Then set pCount to nnn.
            case 0x2000:
                break;

            // 4xkk - compare the register Vx to kk and if the are not equal, increment pCount by 2
            case 0x4000:
                break;

            // 5xy0 - compare register Vx to Vy and if they are equal, increment pCount by 2
            case 0x5000:
                break;

            // 6xkk - put the value kk into register Vx
            case 0x6000:
                System.out.println("opcode found");
                break;

            // 7xkk - add the value of kk to the value of register Vx, then store the result in Vx
            case 0x7000:
                break;

            //multiple cases where the first 4 bits of the opcode are 8, so break into another switch to look at
            //the last 4 bits of the opcode
            case 0x8000:
                break;

            // 9xy0 - compare values of Vx and Vy and if they are not equal, increment pCount by 2
            case 0x9000:
                break;

            // Annn - set iReg to nnn
            case 0xA000:
                break;

            // Bnnn - set pCount to nnn plus the value of V0
            case 0xB000:
                break;

            // Cnnn - generate a random number from 0 - 255 and perform AND operation on it and the value kk.
            // store the results in Vx
            case 0xC000:
                break;

            // Dnnn - set iReg to nnn
            case 0xD000:
                break;

        }

    }
}
