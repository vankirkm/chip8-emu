package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Chip8 {
    private short opcode;
    private char[] memory;
    private char V[];
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
        this.V = new char[16];
        this.gfx = new char[2048];
        this.stack = new short[16];
        this.key = new char[16];

        //load fontset into memory
        for(int i = 0; i < fontSet.length; i++){
            this.memory[0x50 + i] = fontSet[i];
        }
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

        //decode opcode
        switch(this.opcode & 0xF000){

            //multiple cases where the first 4 bits of the opcode are 0, so break into another switch to look at
            //the last two bytes of the opcode
            case 0x0000:
                switch(this.opcode & 0x00FF){
                    // 00E0 - clear the display
                    case 0x00E0:
                        break;

                    // 00EE - set pCount to the address at the top of the stack, then subtract 1 from sPoint
                    case 0x00EE:
                        this.pCount = this.stack[sPoint];
                        this.sPoint = (short)(this.sPoint - 1);
                        break;
                }
                break;

            // 1nnn - set pCount to nnn
            case 0x1000:
                System.out.println("Command 1nnn - " + Integer.toHexString(this.opcode));
                System.out.println("nnn: " + Integer.toHexString((this.opcode & 0x0FFF)));
                this.pCount = (short)(this.opcode & 0x0FFF);
                break;

            // 2nnn - increment sPoint, then put the current pCount on top of the stack. Then set pCount to nnn.
            case 0x2000:
                System.out.println("Command 2nnn - " + Integer.toHexString(this.opcode));
                break;

            // 3xkk - compare the register Vx to kk and if the are equal, increment pCount by 2
            case 0x3000:
                char x = (char)(this.opcode & 0x0F00);
                char kk = (char)(this.opcode & 0x00FF);
                if(this.V[x] == kk){
                    this.pCount += 2;
                }
                break;

            // 4xkk - compare the register Vx to kk and if the are not equal, increment pCount by 2
            case 0x4000:
                System.out.println("Command 4xkk - " + Integer.toHexString(this.opcode));

                break;

            // 5xy0 - compare register Vx to Vy and if they are equal, increment pCount by 2
            case 0x5000:
                System.out.println("Command 5xy0 - " + Integer.toHexString(this.opcode));
                break;

            // 6xkk - put the value kk into register Vx, increment pCount by 2.
            case 0x6000:
                System.out.println("Command 6xkk - " + Integer.toHexString(this.opcode));
                System.out.println("x: " + Integer.toHexString((this.opcode & 0x0F00) >> 8));
                System.out.println("kk: " + Integer.toHexString(this.opcode & 0x00FF));
                this.V[((this.opcode & 0x0F00) >> 8)] = (char)(this.opcode & 0x00FF);
                pCount += 2;
                break;

            // 7xkk - add the value of kk to the value of register Vx, then store the result in Vx
            case 0x7000:
                System.out.println("Command 7xkk - " + Integer.toHexString(this.opcode));
                break;

            //multiple cases where the first 4 bits of the opcode are 8, so break into another switch to look at
            //the last 4 bits of the opcode
            case 0x8000:
                switch(this.opcode & 0x000F){
                    // 8xy0 - store the value of register Vy in register Vx
                    case 0x0000:
                        System.out.println("Command 8xy0 - " + Integer.toHexString(this.opcode));
                        break;

                    // 8xy1 - perform OR operation on values of Vx and Vy, then store the results in Vx
                    case 0x0001:
                        System.out.println("Command 8xy1 - " + Integer.toHexString(this.opcode));
                        break;

                    // 8xy2 - perform AND operation on values of Vx and Vy, then store the results in Vx
                    case 0x0002:
                        System.out.println("Command 8xy2 - " + Integer.toHexString(this.opcode));
                        break;

                    // 8xy3 - perform XOR operation on values of Vx and Vy, then store the results in Vx
                    case 0x0003:
                        System.out.println("Command 8xy3 - " + Integer.toHexString(this.opcode));
                        break;

                    // 8xy4 - The values of Vx and Vy are added together. If the result is greater than 8 bits,
                    //set VF to 1, otherwise 0. only the lowest 8 bits of the result are kept and stored in Vx
                    case 0x0004:
                        System.out.println("Command 8xy4 - " + Integer.toHexString(this.opcode));
                        break;

                    // 8xy5 - if Vx > Vy, set VF to 1, otherwise 0. then Vy is subtracted from Vx,
                    //and the results are stored in Vx
                    case 0x0005:
                        System.out.println("Command 8xy5 - " + Integer.toHexString(this.opcode));
                        break;

                    // 8xy6 - if the least significant bit of Vx is 1, then VF is set to 1, otherwise 0.
                    //Then Vx is divided by 2.
                    case 0x0006:
                        System.out.println("Command 8xy6 - " + Integer.toHexString(this.opcode));
                        break;

                    // 8xy7 - if Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy,
                    //and the results stored in Vx
                    case 0x0007:
                        System.out.println("Command 8xy7 - " + Integer.toHexString(this.opcode));
                        break;

                    // 8xyE - if the most significant bit of Vx is 1, then VF is set to 1, otherwise 0.
                    //Then Vx is multiplied by 2.
                    case 0x000E:
                        System.out.println("Command 8xyE - " + Integer.toHexString(this.opcode));
                        break;
                }
                break;

            // 9xy0 - compare values of Vx and Vy and if they are not equal, increment pCount by 2
            case 0x9000:
                System.out.println("Command 9xy0 - " + Integer.toHexString(this.opcode));
                break;

            // Annn - set iReg to nnn
            case 0xA000:
                System.out.println("Command Annn - " + Integer.toHexString(this.opcode));
                break;

            // Bnnn - set pCount to nnn plus the value of V0
            case 0xB000:
                System.out.println("Command Bnnn - " + Integer.toHexString(this.opcode));
                break;

            // Cxkk - generate a random number from 0 - 255 and perform AND operation on it and the value kk.
            // store the results in Vx
            case 0xC000:
                System.out.println("Command Cxyk - " + Integer.toHexString(this.opcode));
                break;

            // Dxyn - COMPLICATED INSTRUCTIONS
            case 0xD000:
                System.out.println("Command Dxyn - " + Integer.toHexString(this.opcode));
                break;

            //multiple cases where the first 4 bits of the opcode are E, so break into another switch to look at
            //the last 2 bytes of the opcode
            case 0xE000:
                switch(this.opcode & 0x00FF){
                    // Ex9E - checks the keyboard, and if the key corresponding to the value of Vx is currently
                    //in the down position, pCount is incremented by 2
                    case 0x009E:
                        System.out.println("Command Ex9E - " + Integer.toHexString(this.opcode));
                        break;

                    // ExA1 - checks the keyboard, and if the key corresponding to the value of Vx is currently
                    //in the up position, pCount is incremented by 2
                    case 0x00A1:
                        System.out.println("Command ExA1 - " + Integer.toHexString(this.opcode));
                        break;
                }
                break;

            //multiple cases where the first 4 bits of the opcode are F, so break into another switch to look at
            //the last 2 bytes of the opcode
            case 0xF000:
                switch(this.opcode & 0x00FF){
                    // Fx07 - The value of delayTimer is placed into Vx
                    case 0x0007:
                        System.out.println("Command Fx07 - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx0A - All execution stops until a key is pressed, then the value of that key is stored in Vx
                    case 0x000A:
                        System.out.println("Command Fx0A - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx15 - delayTimer is set equal to the value of Vx
                    case 0x0015:
                        System.out.println("Command Fx15 - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx18 - soundTimer is set equal to the value of Vx
                    case 0x0018:
                        System.out.println("Command Fx18 - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx1E - the values of iReg and Vx are added, and the results are stored in iReg
                    case 0x001E:
                        System.out.println("Command Fx1E - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx29 - the value of iReg is set to location of the hexadecimal sprite corresponding to the
                    // value of Vx
                    case 0x0029:
                        System.out.println("Command Fx29 - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx33 - take the decimal value of Vx and place the hundreds digit in memory at I,
                    // the tens digit at location I+1, and the ones digit at location I+2.
                    case 0x0033:
                        System.out.println("Command Fx33 - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx55 - copy the values of registers V0 through Vx into memory, starting at the address in iReg
                    case 0x0055:
                        System.out.println("Command Fx55 - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx65 - Read the values in memory starting at location iReg into the registers V0 through Vx
                    case 0x0065:
                        System.out.println("Command Fx65 - " + Integer.toHexString(this.opcode));
                        break;
                }
                break;
        }

    }
}
