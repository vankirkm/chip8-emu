package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class Chip8 {
    private short opcode;
    private char[] memory;
    private char[] V;
    private short iReg;
    private short pCount;
    private char[][] gfx;
    private char delayTimer;
    private char soundTimer;
    private short[] stack;
    private byte sPoint;
    private boolean drawFlag;
    private char[] key;
    DisplayManager emuDisplay = new DisplayManager();
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
        this.gfx = new char[32][64];
        this.stack = new short[16];
        this.key = new char[16];

        //load fontset into memory
        for(int i = 0; i < fontSet.length; i++){
            this.memory[i] = fontSet[i];
        }
    }

    public void loadGame(String filename){
        int instCount = 0;
        try{
            FileInputStream fis = new FileInputStream("roms/" + filename);
            int ch = 0;
            try{
                while((ch = fis.read()) != -1){
                    System.out.println((instCount + 0x200) + " " + ch);
                    this.memory[instCount + 0x200] = (char) ch;
                    instCount++;
                }
            }catch (IOException e){
                System.out.println("End of file unexpectedly reached.");
            }

        }catch(FileNotFoundException e){
            System.out.println("File does not exist. Check file name and try again.");
        }
    }

    public void emulateCycle(){
        int x = 0;
        int y = 0;
        //get opcode
        this.opcode = (short) (memory[pCount] << 8 | memory[pCount + 1]);

        //decode opcode
        switch(this.opcode & 0xF000){

            //multiple cases where the first 4 bits of the opcode are 0, so break into another switch to look at
            //the last byte of the opcode
            case 0x0000:
                switch(this.opcode & 0x00FF){
                    // 00E0 - clear the display
                    case 0x00E0:
                        break;

                    // 00EE - set pCount to the address at the top of the stack, then subtract 1 from sPoint
                    case 0x00EE:
                        this.pCount = this.stack[sPoint];
                        this.sPoint = (byte)((this.sPoint - 1) & 0x00FF);
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
                stack[(++this.sPoint)] = this.pCount;
                this.pCount = (short)(opcode & 0x0FFF);
                break;

            // 3xkk - Jump over next instruction if register Vx and kk are equal
            case 0x3000:
                System.out.println("Command 3xkk - " + Integer.toHexString(this.opcode));
                System.out.println("Vx: " + Integer.toHexString(this.V[(this.opcode & 0x0F00) >> 8]));
                System.out.println("kk: " + Integer.toHexString(this.opcode & 0x00FF));
                if(this.V[(this.opcode & 0x0F00) >>> 8] == (this.opcode & 0x00FF)){
                    this.pCount += 4;
                }
                else{
                    this.pCount +=2;
                }
                break;

            // 4xkk - compare the register Vx to kk and if they are not equal, skip next instruction
            case 0x4000:
                System.out.println("Command 4xkk - " + Integer.toHexString(this.opcode));
                System.out.println("Vx: " + Integer.toHexString(this.V[(this.opcode & 0x0F00) >> 8]));
                System.out.println(Integer.toHexString((this.opcode & 0x0F00) >> 8));
                System.out.println("kk: " + Integer.toHexString(this.opcode & 0x00FF));
                if((this.V[(this.opcode & 0x0F00) >>> 8]) != (this.opcode & 0x00FF)){
                    this.pCount += 4;
                }
                else{
                    this.pCount += 2;
                }
                break;

            // 5xy0 - compare register Vx to Vy and if they are equal, skip next instruction
            case 0x5000:
                System.out.println("Command 5xy0 - " + Integer.toHexString(this.opcode));
                if(this.V[(this.opcode & 0x0F00) >>> 8] == this.V[(this.opcode & 0x00F0) >>> 4]){
                    this.pCount += 4;
                }
                else{
                    this.pCount += 2;
                }
                break;

            // 6xkk - put the value kk into register Vx, increment pCount by 2.
            case 0x6000:
                System.out.println("Command 6xkk - " + Integer.toHexString(this.opcode));
                System.out.println("x: " + Integer.toHexString((this.opcode & 0x0F00) >>> 8));
                System.out.println("kk: " + Integer.toHexString(this.opcode & 0x00FF));
                this.V[((this.opcode & 0x0F00) >>> 8)] = (char)(this.opcode & 0x00FF);
                pCount += 2;
                break;

            // 7xkk - add the value of kk to the value of register Vx, then store the result in Vx
            case 0x7000:
                System.out.println("Command 7xkk - " + Integer.toHexString(this.opcode));
                this.V[(this.opcode & 0x0F00) >>> 8] = (char)((this.V[(this.opcode & 0x0F00) >>> 8] + (this.opcode & 0x0FF)) & 0x00FF);
                System.out.println("Vx: " + Integer.toHexString(this.V[(this.opcode & 0x0F00) >>> 8]));
                this.pCount +=2;
                break;

            //multiple cases where the first 4 bits of the opcode are 8, so break into another switch to look at
            //the last 4 bits of the opcode
            case 0x8000:
                switch(this.opcode & 0x000F){
                    // 8xy0 - store the value of register Vy in register Vx
                    case 0x0000:
                        System.out.println("Command 8xy0 - " + Integer.toHexString(this.opcode & 0xFFFF));
                        this.V[(this.opcode & 0x0F00) >>> 8] = this.V[(this.opcode & 0x00F0) >>> 4];
                        this.pCount += 2;
                        break;

                    // 8xy1 - perform OR operation on values of Vx and Vy, then store the results in Vx
                    case 0x0001:
                        System.out.println("Command 8xy1 - " + Integer.toHexString(this.opcode & 0xFFFF));
                        this.V[(this.opcode & 0x0F00) >>> 8] = (char)(this.V[(this.opcode & 0x0F00) >>> 8] | this.V[(this.opcode & 0x00F0) >>> 4]);
                        this.pCount += 2;
                        break;

                    // 8xy2 - perform AND operation on values of Vx and Vy, then store the results in Vx
                    case 0x0002:
                        System.out.println("Command 8xy2 - " + Integer.toHexString(this.opcode & 0xFFFF));
                        this.V[(this.opcode & 0x0F00) >>> 8] = (char)(this.V[(this.opcode & 0x0F00) >>> 8] & this.V[(this.opcode & 0x00F0) >>> 4]);
                        this.pCount += 2;
                        break;

                    // 8xy3 - perform XOR operation on values of Vx and Vy, then store the results in Vx
                    case 0x0003:
                        System.out.println("Command 8xy3 - " + Integer.toHexString(this.opcode & 0xFFFF));
                        this.V[(this.opcode & 0x0F00) >>> 8] = (char)(this.V[(this.opcode & 0x0F00) >>> 8] ^ this.V[(this.opcode & 0x00F0) >>> 4]);
                        this.pCount += 2;
                        break;

                    // 8xy4 - The values of Vx and Vy are added together. If the result is greater than 8 bits,
                    //set VF to 1, otherwise 0. only the lowest 8 bits of the result are kept and stored in Vx
                    case 0x0004:
                        System.out.println("Command 8xy4 - " + Integer.toHexString(this.opcode & 0xFFFF));
                        if((this.V[(this.opcode & 0x0F00) >>> 8] + this.V[(this.opcode & 0x00F0) >>> 4]) > 255){
                            this.V[0xF] = 1;
                        }
                        else{
                            this.V[0xF] = 0;
                        }
                        this.V[(this.opcode & 0x0F00) >>> 8] = (char)((this.V[(this.opcode & 0x0F00) >>> 8] + this.V[(this.opcode & 0x00F0) >>> 4]) & 0x00FF);
                        this.pCount += 2;
                        break;

                    // 8xy5 - if Vx > Vy, set VF to 1, otherwise 0. then Vy is subtracted from Vx,
                    //and the results are stored in Vx
                    case 0x0005:
                        System.out.println("Command 8xy5 - " + Integer.toHexString(this.opcode & 0xFFFF));
                        if(this.V[(this.opcode & 0x0F00) >>> 8] > this.V[(this.opcode & 0x00F0) >>> 4]){
                            this.V[0xF] = 1;
                        }
                        else{
                            this.V[0xF] = 0;
                        }
                        this.V[(this.opcode & 0x0F00) >>> 8] = (char)((this.V[(this.opcode & 0x0F00) >>> 8]) - (this.V[(this.opcode & 0x00F0) >>> 4]));
                        this.pCount += 2;
                        break;

                    // 8xy6 - if the least significant bit of Vx is 1, then VF is set to 1, otherwise 0.
                    //Then Vx is divided by 2.
                    case 0x0006:
                        System.out.println("Command 8xy6 - " + Integer.toHexString(this.opcode));
                        this.V[0xF] = (char)(this.V[(opcode & 0x0F00) >> 8] & 0x0001);
                        this.V[(opcode & 0x0F00) >>> 8] = (char)(this.V[(opcode & 0x0F00) >>> 8] >>> 1);
                        this.pCount += 2;
                        break;

                    // 8xy7 - if Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy,
                    //and the results stored in Vx
                    case 0x0007:
                        System.out.println("Command 8xy7 - " + Integer.toHexString(this.opcode));
                        if(this.V[(opcode & 0x00F0) >> 4] > this.V[(opcode & 0x0F00) >> 8]){
                            this.V[0xF] = 1;
                        }
                        else{
                            this.V[0xF] = 0;
                        }
                        this.V[(opcode & 0x0F00) >> 8] = (char)((this.V[(opcode & 0x00F0) >> 4] - this.V[(opcode & 0x0F00) >> 8]) & 0x00FF);
                        this.pCount += 2;
                        break;

                    // 8xyE - if the most significant bit of Vx is 1, then VF is set to 1, otherwise 0.
                    //Then Vx is multiplied by 2.
                    case 0x000E:
                        System.out.println("Command 8xyE - " + Integer.toHexString(this.opcode));
                        char tmp = (char)(this.V[(opcode & 0x0F00) >> 8] >> 7);
                        System.out.println(Integer.toHexString(tmp));
                        this.V[0xF] = tmp;
                        System.out.println(Integer.toHexString(this.V[(opcode & 0x0F00) >> 8] * 2));
                        this.V[(opcode & 0x0F00) >> 8] = (char)((this.V[(opcode & 0x0F00) >> 8] * 2) & 0x00FF);
                        this.pCount += 2;
                        break;
                }
                break;

            // 9xy0 - compare values of Vx and Vy and if they are not equal, skip next instruction
            case 0x9000:
                System.out.println("Command 9xy0 - " + Integer.toHexString(this.opcode & 0xFFFF));
                if(this.V[(this.opcode & 0x0F00) >> 8] == this.V[(this.opcode & 0x00F0) >> 4]){
                    this.pCount += 2;
                }
                else{
                    this.pCount += 4;
                }
                break;

            // Annn - set iReg to nnn
            case 0xA000:
                System.out.println("Command Annn - " + Integer.toHexString(this.opcode & 0xFFFF));
                System.out.println("nnn: " + Integer.toHexString(this.opcode & 0x0FFF));
                this.iReg = (short)(this.opcode & 0x0FFF);
                this.pCount += 2;
                break;

            // Bnnn - set pCount to nnn plus the value of V0
            case 0xB000:
                System.out.println("Command Bnnn - " + Integer.toHexString(this.opcode & 0xFFFF));
                this.pCount = (short)(((this.opcode & 0x0FFF) + this.V[0]) & 0x0FFF);
                break;

            // Cxkk - generate a random number from 0 - 255 and perform AND operation on it and the value kk.
            // store the results in Vx
            case 0xC000:
                System.out.println("Command Cxyk - " + Integer.toHexString(this.opcode));
                Random r = new Random();
                int rand = r.nextInt(256);
                this.V[(this.opcode & 0x0F00) >> 8] = (char)((this.opcode & 0x00FF) & rand);
                this.pCount += 2;
                break;

            // Dxyn - COMPLICATED INSTRUCTIONS
            case 0xD000:
                System.out.println("Command Dxyn - " + Integer.toHexString(this.opcode & 0xFFFF));
                x = this.V[(opcode & 0x0F00) >>> 8];
                y = this.V[(opcode & 0x00F0) >>> 4];
                int n = opcode & 0x000F;
                this.V[0xF] = 0;
                for(int i = 0; i < n; i++){
                    for(int z = 0; z < 8; z++){
                        if (x < 64 && y < 32) {
                            int xcoord = x + z;
                            int ycoord = y + i;
                            // if pixel already exists, set carry (collision)
                            if (emuDisplay.getScreenPixel(xcoord, ycoord) == 1) {
                                this.V[0xF] = 1;
                            }
                            // draw via xor
                            emuDisplay.setScreenPixel(xcoord,ycoord);
                        }
                    }
                }
                drawFlag = true;
                this.pCount += 2;
                break;

            //multiple cases where the first 4 bits of the opcode are E, so break into another switch to look at
            //the last byte of the opcode
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
            //the last byte of the opcode
            case 0xF000:
                switch(this.opcode & 0x00FF){
                    // Fx07 - The value of delayTimer is placed into Vx
                    case 0x0007:
                        System.out.println("Command Fx07 - " + Integer.toHexString(this.opcode));
                        this.V[(this.opcode & 0x0F00) >>> 8] = delayTimer;
                        this.pCount += 2;
                        break;

                    // Fx0A - All execution stops until a key is pressed, then the value of that key is stored in Vx
                    case 0x000A:
                        System.out.println("Command Fx0A - " + Integer.toHexString(this.opcode));
                        break;

                    // Fx15 - delayTimer is set equal to the value of Vx
                    case 0x0015:
                        System.out.println("Command Fx15 - " + Integer.toHexString(this.opcode & 0xFFFF));
                        this.delayTimer = this.V[(this.opcode & 0x0F00) >>> 8];
                        System.out.println("delayTimer: " + Integer.toHexString(this.delayTimer));
                        this.pCount += 2;
                        break;

                    // Fx18 - soundTimer is set equal to the value of Vx
                    case 0x0018:
                        System.out.println("Command Fx18 - " + Integer.toHexString(this.opcode));
                        soundTimer = this.V[(this.opcode & 0x0F00) >>> 8];
                        this.pCount += 2;
                        break;

                    // Fx1E - the values of iReg and Vx are added, and the results are stored in iReg
                    case 0x001E:
                        System.out.println("Command Fx1E - " + Integer.toHexString(this.opcode));
                        iReg = (short)(iReg + this.V[(this.opcode & 0x0F00) >>> 8]);
                        this.pCount += 2;
                        break;

                    // Fx29 - the value of iReg is set to location of the hexadecimal sprite corresponding to the
                    // value of Vx
                    case 0x0029:
                        System.out.println("Command Fx29 - " + Integer.toHexString(this.opcode));
                        iReg = (short)(this.V[(this.opcode & 0x0F00) >>> 8]);
                        this.pCount += 2;
                        break;

                    // Fx33 - take the decimal value of Vx and place the hundreds digit in memory at I,
                    // the tens digit at location I+1, and the ones digit at location I+2.
                    case 0x0033:
                        System.out.println("Command Fx33 - " + Integer.toHexString(this.opcode));
                        int vx = this.V[(this.opcode & 0x0F00) >>> 8];
                        int hundreds = vx / 100;
                        vx = vx - (hundreds * 100);
                        int tens = vx / 10;
                        vx = vx - (tens * 10);
                        int units = vx;
                        this.memory[iReg] = (char)hundreds;
                        this.memory[iReg + 1] = (char)tens;
                        this.memory[iReg + 2] = (char)units;
                        this.pCount += 2;
                        break;

                    // Fx55 - copy the values of registers V0 through Vx into memory, starting at the address in iReg
                    case 0x0055:
                        System.out.println("Command Fx55 - " + Integer.toHexString(this.opcode));
                        int tmp = (this.opcode & 0x0F00) >> 8;
                        for(int i = 0; i <= tmp; i++){
                            this.memory[iReg + i] = this.V[i];
                        }
                        this.pCount += 2;
                        break;

                    // Fx65 - Read the values in memory starting at location iReg into the registers V0 through Vx
                    case 0x0065:
                        System.out.println("Command Fx65 - " + Integer.toHexString(this.opcode));
                        int tmpX = (this.opcode & 0x0F00) >> 8;
                        for(int i = 0; i <= tmpX; i++){
                            this.V[i] = this.memory[iReg + i];
                        }
                        this.pCount +=2;
                        break;
                }
                break;
        }

        //update screen
        if(drawFlag){
            emuDisplay.updateScreen();
            drawFlag = false;
        }

        //update delay timer
        if(this.delayTimer > 0){
            this.delayTimer -= 1;
        }

        //update sound timer
        if(this.soundTimer > 0) {
            this.soundTimer -= 1;
        }


    }
}
