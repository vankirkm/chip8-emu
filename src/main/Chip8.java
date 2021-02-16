package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class Chip8 {
    private int cpuFreq;
    private int cycToRefresh;
    private int refreshCycles;
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
        cpuFreq = 500;
        cycToRefresh = cpuFreq / 60;
        refreshCycles = 0;
        pCount = 0x200;
        opcode = 0;
        iReg = 0;
        sPoint = 0;
        memory = new char[4096];
        V = new char[16];
        gfx = new char[32][64];
        stack = new short[16];
        key = new char[16];

        //load fontset into memory
        for(int i = 0; i < fontSet.length; i++){
            char bte = fontSet[i];
            memory[i] = bte;
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
                    memory[instCount + 0x200] = (char) ch;
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


        //get opcode
        opcode = (short) (memory[pCount] << 8 | memory[pCount + 1]);
        byte x = getX();
        byte y = getY();
        byte n = getN();
        char kk = getKK();
        short nnn = getNNN();

        //decode opcode
        switch(opcode & 0xF000){

            //multiple cases where the first 4 bits of the opcode are 0, so break into another switch to look at
            //the last byte of the opcode
            case 0x0000:
                switch(opcode & 0x00FF){
                    // 00E0
                    case 0x00E0:
                        clearScreen();
                        break;

                    // 00EE
                    case 0x00EE:
                        pCountStack();
                        break;
                }
                break;

            // 1nnn
            case 0x1000:
                setPN(nnn);
                break;

            // 2nnn
            case 0x2000:
                incSPoint(nnn);
                break;

            // 3xkk
            case 0x3000:
                jmpEqual(x, kk);
                break;

            // 4xkk
            case 0x4000:
                jmpNotEqual();
                break;

            // 5xy0
            case 0x5000:
                cmpEqual();
                break;

            // 6xkk
            case 0x6000:
                putKK();
                break;

            // 7xkk
            case 0x7000:
                addKK(x, kk);
                break;

            //multiple cases where the first 4 bits of the opcode are 8, so break into another switch to look at
            //the last 4 bits of the opcode
            case 0x8000:
                switch(opcode & 0x000F){
                    // 8xy0
                    case 0x0000:
                        putVY();
                        break;

                    // 8xy1
                    case 0x0001:
                        orVY();
                        break;

                    // 8xy2
                    case 0x0002:
                        andVY();
                        break;

                    // 8xy3
                    case 0x0003:
                        xorVY();
                        break;

                    // 8xy4
                    case 0x0004:
                        addVY();
                        break;

                    // 8xy5
                    case 0x0005:
                        subVY();
                        break;

                    // 8xy6
                    case 0x0006:
                        leastSigBit();
                        break;

                    // 8xy7
                    case 0x0007:
                        subtractVX(x, y);
                        break;

                    // 8xyE
                    case 0x000E:
                        mostSigBit();
                        break;
                }
                break;

            // 9xy0
            case 0x9000:
                cmpVXVY(x, y);
                break;

            // Annn
            case 0xA000:
                setIReg(nnn);
                break;

            // Bnnn
            case 0xB000:
                setPCount(nnn);
                break;

            // Cxkk
            case 0xC000:
                putRandVX(x, kk);
                break;

            // Dxyn - COMPLICATED INSTRUCTIONS
            case 0xD000:
                drawSprite(x, y, n);
                break;

            //multiple cases where the first 4 bits of the opcode are E, so break into another switch to look at
            //the last byte of the opcode
            case 0xE000:
                switch(opcode & 0x00FF){
                    // Ex9E - checks the keyboard, and if the key corresponding to the value of Vx is currently
                    //in the down position, pCount is incremented by 2
                    case 0x009E:
                        System.out.println("Command Ex9E - " + Integer.toHexString(opcode));
                        break;

                    // ExA1 - checks the keyboard, and if the key corresponding to the value of Vx is currently
                    //in the up position, pCount is incremented by 2
                    case 0x00A1:
                        System.out.println("Command ExA1 - " + Integer.toHexString(opcode));
                        break;
                }
                break;

            //multiple cases where the first 4 bits of the opcode are F, so break into another switch to look at
            //the last byte of the opcode
            case 0xF000:
                switch(opcode & 0x00FF){
                    // Fx07
                    case 0x0007:
                        putDelayTimer(x);
                        break;

                    // Fx0A
                    case 0x000A:
                        pauseExec();
                        break;

                    // Fx15
                    case 0x0015:
                        setDelayTimerVX(x);
                        break;

                    // Fx18
                    case 0x0018:
                        setSoundTimer(x);
                        break;

                    // Fx1E
                    case 0x001E:
                        addIReg(x);
                        break;

                    // Fx29
                    case 0x0029:
                        setIRegHex(x);
                        break;

                    // Fx33
                    case 0x0033:
                        putIRegValues(x);
                        break;

                    // Fx55
                    case 0x0055:
                        regToMem(x);
                        break;

                    // Fx65
                    case 0x0065:
                        memToReg(x);
                        break;
                }
                break;
        }

        refreshCycles++;
        if(cycToRefresh % refreshCycles == 0){
            refreshCycles = 0;
            //update screen
            if(drawFlag){
                emuDisplay.updateScreen();
                drawFlag = false;
            }

            //update delay timer
            if(delayTimer > 0){
                delayTimer -= 1;
            }

            //update sound timer
            if(soundTimer > 0) {
                soundTimer -= 1;
            }
        }



    }

    private boolean isBitSet(byte b, int bit){
        return (b & (1 << bit)) != 0;
    }

    // 00E0 - clear the display
    public void clearScreen(){
        emuDisplay.clearGameScreen();
        pCount += 2;
    }

    // 00EE - set pCount to the address at the top of the stack, then subtract 1 from sPoint
    public void pCountStack(){
        short tmp = stack[sPoint];
        pCount = tmp;
        sPoint -= 1;
    }

    // 1nnn - set pCount to nnn
    public void setPN(short nnn){
        System.out.println("Command 1nnn - " + Integer.toHexString(opcode));
        System.out.println("nnn: " + Integer.toHexString((opcode & 0x0FFF)));
        pCount = nnn;
    }

    // 2nnn - increment sPoint, then put the current pCount on top of the stack. Then set pCount to nnn.
    public void incSPoint(short nnn){
        System.out.println("Command 2nnn - " + Integer.toHexString(opcode));
        short tmp = pCount;
        sPoint++;
        stack[sPoint] = tmp;
        pCount = (short)nnn;
    }

    // 3xkk - Jump over next instruction if register Vx and kk are equal
    public void jmpEqual(byte x, char kk){
        System.out.println("Command 3xkk - " + Integer.toHexString(opcode));
        System.out.println("Vx: " + Integer.toHexString(V[(opcode & 0x0F00) >> 8]));
        System.out.println("kk: " + Integer.toHexString(opcode & 0x00FF));
        if(V[(opcode & 0x0F00) >>> 8] == (opcode & 0x00FF)){
            pCount += 4;
        }
        else{
            pCount +=2;
        }
    }

    // 4xkk - compare the register Vx to kk and if they are not equal, skip next instruction
    public void jmpNotEqual(){
        System.out.println("Command 4xkk - " + Integer.toHexString(opcode));
        System.out.println("Vx: " + Integer.toHexString(V[(opcode & 0x0F00) >> 8]));
        System.out.println(Integer.toHexString((opcode & 0x0F00) >> 8));
        System.out.println("kk: " + Integer.toHexString(opcode & 0x00FF));
        if((V[(opcode & 0x0F00) >>> 8]) != (opcode & 0x00FF)){
            pCount += 4;
        }
        else{
            pCount += 2;
        }
    }

    // 5xy0 - compare register Vx to Vy and if they are equal, skip next instruction
    public void cmpEqual(){
        System.out.println("Command 5xy0 - " + Integer.toHexString(opcode));
        if(V[(opcode & 0x0F00) >>> 8] == V[(opcode & 0x00F0) >>> 4]){
            pCount += 4;
        }
        else{
            pCount += 2;
        }
    }

    // 6xkk - put the value kk into register Vx, increment pCount by 2.
    public void putKK(){
        System.out.println("Command 6xkk - " + Integer.toHexString(opcode));
        System.out.println("x: " + Integer.toHexString((opcode & 0x0F00) >>> 8));
        System.out.println("kk: " + Integer.toHexString(opcode & 0x00FF));
        V[((opcode & 0x0F00) >>> 8)] = (char)(opcode & 0x00FF);
        pCount += 2;
    }

    // 7xkk - add the value of kk to the value of register Vx, then store the result in Vx
    public void addKK(byte x, char kk){
        System.out.println("Command 7xkk - " + Integer.toHexString(opcode));
        char tmp = (char)((V[x] + kk) + 0x00FF);
        V[x] = tmp;
        pCount +=2;
    }

    // 8xy0 - store the value of register Vy in register Vx
    public void putVY(){
        System.out.println("Command 8xy0 - " + Integer.toHexString(opcode & 0xFFFF));
        char tmp = V[(opcode & 0x00F0) >>> 4];
        V[(opcode & 0x0F00) >>> 8] = tmp;
        pCount += 2;
    }

    // 8xy1 - perform OR operation on values of Vx and Vy, then store the results in Vx
    public void orVY(){
        System.out.println("Command 8xy1 - " + Integer.toHexString(opcode & 0xFFFF));
        char tmp = (char)(V[(opcode & 0x0F00) >>> 8] | V[(opcode & 0x00F0) >>> 4]);
        V[(opcode & 0x0F00) >>> 8] = tmp;
        pCount += 2;
    }

    // 8xy2 - perform AND operation on values of Vx and Vy, then store the results in Vx
    public void andVY(){
        System.out.println("Command 8xy2 - " + Integer.toHexString(opcode & 0xFFFF));
        char tmp = (char)(V[(opcode & 0x0F00) >>> 8] & V[(opcode & 0x00F0) >>> 4]);
        V[(opcode & 0x0F00) >>> 8] = tmp;
        pCount += 2;
    }

    // 8xy3 - perform XOR operation on values of Vx and Vy, then store the results in Vx
    public void xorVY(){
        System.out.println("Command 8xy3 - " + Integer.toHexString(opcode & 0xFFFF));
        char tmp = (char)(V[(opcode & 0x0F00) >>> 8] ^ V[(opcode & 0x00F0) >>> 4]);
        V[(opcode & 0x0F00) >>> 8] = tmp;
        pCount += 2;
    }

    // 8xy4 - The values of Vx and Vy are added together. If the result is greater than 8 bits,
    //set VF to 1, otherwise 0. only the lowest 8 bits of the result are kept and stored in Vx
    public void addVY(){
        System.out.println("Command 8xy4 - " + Integer.toHexString(opcode & 0xFFFF));
        if((V[(opcode & 0x0F00) >>> 8] + V[(opcode & 0x00F0) >>> 4]) > 255){
            V[0xF] = 1;
        }
        else{
            V[0xF] = 0;
        }
        char tmp = (char)((V[(opcode & 0x0F00) >>> 8] + V[(opcode & 0x00F0) >>> 4]) & 0x00FF);
        V[(opcode & 0x0F00) >>> 8] = tmp;
        pCount += 2;
    }

    // 8xy5 - if Vx > Vy, set VF to 1, otherwise 0. then Vy is subtracted from Vx,
    //and the results are stored in Vx
    public void subVY(){
        System.out.println("Command 8xy5 - " + Integer.toHexString(opcode & 0xFFFF));
        if(V[(opcode & 0x0F00) >>> 8] > V[(opcode & 0x00F0) >>> 4]){
            V[0xF] = 1;
        }
        else{
            V[0xF] = 0;
        }
        char tmp = (char)((V[(opcode & 0x0F00) >>> 8]) - (V[(opcode & 0x00F0) >>> 4]));
        V[(opcode & 0x0F00) >>> 8] = tmp;
        pCount += 2;
    }

    // 8xy6 - if the least significant bit of Vx is 1, then VF is set to 1, otherwise 0.
    //Then Vx is divided by 2.
    public void leastSigBit(){
        System.out.println("Command 8xy6 - " + Integer.toHexString(opcode));
        V[0xF] = (char)(V[(opcode & 0x0F00) >> 8] & 0x0001);
        V[(opcode & 0x0F00) >>> 8] = (char)(V[(opcode & 0x0F00) >>> 8] >>> 1);
        pCount += 2;
    }

    // 8xy7 - if Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy,
    //and the results stored in Vx
    public void subtractVX(byte x, byte y){
        System.out.println("Command 8xy7 - " + Integer.toHexString(opcode));
        if(V[y] > V[x]){
            V[0xF] = 1;
        }
        else{
            V[0xF] = 0;
        }
        char tmp = (char)((V[y] - V[x]) & 0x00FF);
        V[x] = tmp;
        pCount += 2;
    }

    // 8xyE - if the most significant bit of Vx is 1, then VF is set to 1, otherwise 0.
    //Then Vx is multiplied by 2.
    public void mostSigBit(){
        System.out.println("Command 8xyE - " + Integer.toHexString(opcode));
        char tmp = (char)(V[(opcode & 0x0F00) >> 8] >> 7);
        System.out.println(Integer.toHexString(tmp));
        V[0xF] = tmp;
        System.out.println(Integer.toHexString(V[(opcode & 0x0F00) >> 8] * 2));
        V[(opcode & 0x0F00) >> 8] = (char)((V[(opcode & 0x0F00) >> 8] * 2) & 0x00FF);
        pCount += 2;
    }

    // 9xy0 - compare values of Vx and Vy and if they are not equal, skip next instruction
    public void cmpVXVY(byte x, byte y){
        System.out.println("Command 9xy0 - " + Integer.toHexString(opcode & 0xFFFF));
        if(V[(opcode & 0x0F00) >> 8] == V[(opcode & 0x00F0) >> 4]){
            pCount += 2;
        }
        else{
            pCount += 4;
        }
    }


    // Annn - set iReg to nnn
    public void setIReg(short nnn){
        System.out.println("Command Annn - " + Integer.toHexString(opcode & 0xFFFF));
        iReg = nnn;
        pCount += 2;
    }

    // Bnnn - set pCount to nnn plus the value of V0
    public void setPCount(short nnn){
        System.out.println("Command Bnnn - " + Integer.toHexString(opcode & 0xFFFF));
        pCount = (short)((nnn + V[0]) & 0x0FFF);
    }

    // Cxkk - generate a random number from 0 - 255 and perform AND operation on it and the value kk.
    // store the results in Vx
    public void putRandVX(byte x, char kk){
        System.out.println("Command Cxyk - " + Integer.toHexString(opcode));
        Random r = new Random();
        int rand = r.nextInt(256);
        V[x] = (char)(kk & rand);
        pCount += 2;
    }

    // Fx07 - The value of delayTimer is placed into Vx
    public void putDelayTimer(byte x){
        System.out.println("Command Fx07 - " + Integer.toHexString(opcode));
        V[x] = delayTimer;
        pCount += 2;
    }

    // Fx0A - All execution stops until a key is pressed, then the value of that key is stored in Vx
    public void pauseExec(){
        System.out.println("Command Fx0A - " + Integer.toHexString(opcode));
    }

    // Fx15 - delayTimer is set equal to the value of Vx
    public void setDelayTimerVX(byte x){
        System.out.println("Command Fx15 - " + Integer.toHexString(opcode & 0xFFFF));
        delayTimer = V[x];
        pCount += 2;
    }

    // Fx18 - soundTimer is set equal to the value of Vx
    public void setSoundTimer(byte x){
        System.out.println("Command Fx18 - " + Integer.toHexString(opcode));
        soundTimer = V[x];
        pCount += 2;
    }

    // Fx1E - the values of iReg and Vx are added, and the results are stored in iReg
    public void addIReg(byte x){
        System.out.println("Command Fx1E - " + Integer.toHexString(opcode));
        short tmp = (short)(iReg + V[x]);
        iReg = tmp;
        pCount += 2;
    }

    // Fx29 - the value of iReg is set to location of the hexadecimal sprite corresponding to the
    // value of Vx
    public void setIRegHex(byte x){
        System.out.println("Command Fx29 - " + Integer.toHexString(opcode));
        iReg = (short)(memory[V[x]]);
        pCount += 2;
    }

    // Fx33 - take the decimal value of Vx and place the hundreds digit in memory at I,
    // the tens digit at location I+1, and the ones digit at location I+2.
    public void putIRegValues(byte x){
        System.out.println("Command Fx33 - " + Integer.toHexString(opcode));
        int vx = V[x];
        int hundreds = vx / 100;
        vx = vx - (hundreds * 100);
        int tens = vx / 10;
        vx = vx - (tens * 10);
        int units = vx;
        memory[iReg] = (char)hundreds;
        memory[iReg + 1] = (char)tens;
        memory[iReg + 2] = (char)units;
        pCount += 2;
    }

    // Fx55 - copy the values of registers V0 through Vx into memory, starting at the address in iReg
    public void regToMem(byte x){
        System.out.println("Command Fx55 - " + Integer.toHexString(opcode));
        int tmp = x;
        for(int i = 0; i <= tmp; i++){
            memory[iReg + i] = V[i];
        }
        pCount += 2;
    }

    //// Fx65 - Read the values in memory starting at location iReg into the registers V0 through Vx
    public void memToReg(byte x){
        System.out.println("Command Fx65 - " + Integer.toHexString(opcode));
        int tmpX = x;
        for(int i = 0; i <= tmpX; i++){
            V[i] = memory[iReg + i];
        }
        pCount +=2;
    }


    //dxyn - draw method
    public void drawSprite(int x, int y, int n){
        System.out.println("Command Dxyn - " + Integer.toHexString(opcode & 0xFFFF));
        byte readBytes = 0;
        byte vf = (byte)0x0;
        while(readBytes < n){

            byte currentByte = (byte)memory[(iReg + readBytes)]; //Read one byte
            for(int i = 0; i <=7; i++){

                //Calculate real coordinate
                int int_x = V[x];
                int int_y = V[y];
                //System.out.println(int_x + " " + int_y);
                int real_x = (int_x + i)%64;
                int real_y = (int_y + readBytes)%32;

                boolean previousPixel = emuDisplay.getScreenPixel(real_x,real_y); //Previous value of pixel

                boolean newPixel = previousPixel ^ isBitSet(currentByte,7-i); //XOR
                System.out.println(previousPixel + " " + newPixel);

                emuDisplay.setScreenPixel(newPixel, real_x, real_y);

                if(previousPixel == true && newPixel == false){
                    vf = (byte)0x01;
                }

            }

            V[0xF] = (char)vf;
            readBytes++;
        }

        drawFlag = true;
        pCount += 2;
    }





    //methods to extract data from opcodes
    public byte getX(){
        return (byte)(opcode & 0x0F00 >> 8);
    }

    public byte getY(){
        return (byte)(opcode & 0x00F0 >> 4);
    }

    public byte getN(){
        return (byte)(opcode & 0x000F);
    }

    public short getNNN(){
        return (short)(opcode & 0x0FFF);
    }

    public char getKK(){
        return (char)(opcode & 0x00FF);
    }

}
