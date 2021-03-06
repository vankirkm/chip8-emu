package main;

import javax.sound.midi.Soundbank;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class Chip8 {
    private int cpuFreq;
    private int cycToRefresh;
    private int refreshCycles;
    private short opcode;
    private char[] memory;
    private char[] V;
    private short iReg;
    private short pCount;
    private char delayTimer;
    private char soundTimer;
    private short[] stack;
    private byte sPoint;
    private boolean drawFlag;
    private Controller controller;
    private DisplayManager emuDisplay;
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
        cpuFreq = 900;
        cycToRefresh = cpuFreq / 60;
        refreshCycles = 0;
        pCount = 0x200;
        opcode = 0;
        iReg = 0;
        sPoint = 0;
        memory = new char[4096];
        V = new char[16];
        stack = new short[16];
        controller = new Controller();
        emuDisplay = new DisplayManager();
        addListenerToReset();

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
                    memory[instCount + 0x200] = (char) ch;
                    instCount++;
                }
            }catch (IOException e){
                System.out.println("End of file unexpectedly reached.");
            }
            try{
                fis.close();
            }catch(IOException e1){
                e1.printStackTrace();
            }
        }catch(FileNotFoundException e){
            System.out.println("File does not exist. Check file name and try again.");
        }
    }

    public void emulateCycle(){


        //get opcode
        opcode = (short) (memory[pCount] << 8 | memory[pCount + 1]);
        char x = getX(opcode);
        char y = getY(opcode);
        char n = getN(opcode);
        char kk = getKK(opcode);
        short nnn = getNNN(opcode);


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
                jmpNotEqual(x, kk);
                break;

            // 5xy0
            case 0x5000:
                cmpEqual(x, y);
                break;

            // 6xkk
            case 0x6000:
                putKK(x, kk);
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
                        putVY(x, y);
                        break;

                    // 8xy1
                    case 0x0001:
                        orVY(x, y);
                        break;

                    // 8xy2
                    case 0x0002:
                        andVY(x, y);
                        break;

                    // 8xy3
                    case 0x0003:
                        xorVY(x, y);
                        break;

                    // 8xy4
                    case 0x0004:
                        addVY(x, y);
                        break;

                    // 8xy5
                    case 0x0005:
                        subVY(x, y);
                        break;

                    // 8xy6
                    case 0x0006:
                        leastSigBit(x, y);
                        break;

                    // 8xy7
                    case 0x0007:
                        subtractVX(x, y);
                        break;

                    // 8xyE
                    case 0x000E:
                        mostSigBit(x, y);
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
                    // Ex9E
                    case 0x009E:
                        isKeyPressed(x);
                        break;

                    // ExA1 - checks the keyboard, and if the key corresponding to the value of Vx is currently
                    //in the up position, pCount is incremented by 2
                    case 0x00A1:
                        isKeyReleased(x);
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
                        pauseExec(x);
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


        if(refreshCycles % cycToRefresh == 0){
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
        refreshCycles++;
        try{
            TimeUnit.NANOSECONDS.sleep(5);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    // 00E0 - clear the display
    public void clearScreen(){
        emuDisplay.clearGameScreen();
        pCount += 2;
    }

    // 00EE - set pCount to the address at the top of the stack, then subtract 1 from sPoint
    public void pCountStack(){
        pCount = stack[sPoint];
        sPoint = (byte)(sPoint - (byte)0x01);
        pCount += 2;
    }

    // 1nnn - set pCount to nnn
    public void setPN(short nnn){
        pCount = nnn;
    }

    // 2nnn - increment sPoint, then put the current pCount on top of the stack. Then set pCount to nnn.
    public void incSPoint(short nnn){
        sPoint = (byte)(sPoint + (byte)0x01);
        stack[sPoint] = pCount;
        pCount = nnn;
    }

    // 3xkk - Jump over next instruction if register Vx and kk are equal
    public void jmpEqual(char x, char kk){
        if(V[x] == kk){
            pCount += 4;
        }
        else{
            pCount +=2;
        }
    }

    // 4xkk - compare the register Vx to kk and if they are not equal, skip next instruction
    public void jmpNotEqual(char x, char kk){
        if((V[x] != kk)){
            pCount += 4;
        }
        else{
            pCount += 2;
        }
    }

    // 5xy0 - compare register Vx to Vy and if they are equal, skip next instruction
    public void cmpEqual(char x, char y){
        if(V[x] == V[y]){
            pCount += 4;
        }
        else{
            pCount += 2;
        }
    }

    // 6xkk - put the value kk into register Vx, increment pCount by 2.
    public void putKK(char x, char kk){
        V[x] = kk;
        pCount += 2;
    }

    // 7xkk - add the value of kk to the value of register Vx, then store the result in Vx
    public void addKK(char x, char kk){
        V[x] = (char)(((V[x] + kk)) & 0x00FF);
        pCount += 2;
    }

    // 8xy0 - store the value of register Vy in register Vx
    public void putVY(char x, char y){
        V[x] = V[y];
        pCount += 2;
    }

    // 8xy1 - perform OR operation on values of Vx and Vy, then store the results in Vx
    public void orVY(char x, char y){
        char tmp = (char)(V[x] | V[y]);
        V[x] = tmp;
        pCount += 2;
    }

    // 8xy2 - perform AND operation on values of Vx and Vy, then store the results in Vx
    public void andVY(char x, char y){
        char tmp = (char)(V[x] & V[y]);
        V[x] = tmp;
        pCount += 2;
    }

    // 8xy3 - perform XOR operation on values of Vx and Vy, then store the results in Vx
    public void xorVY(char x, char y){
        char tmp = (char)(V[x] ^ V[y]);
        V[x] = tmp;
        pCount += 2;
    }

    // 8xy4 - The values of Vx and Vy are added together. If the result is greater than 8 bits,
    //set VF to 1, otherwise 0. only the lowest 8 bits of the result are kept and stored in Vx
    public void addVY(char x, char y){
        if((V[x] + V[y]) > 255){
            V[0xF] = 1;
        }
        else{
            V[0xF] = 0;
        }
        char tmp = (char)((V[x] + V[y]) & 0x00FF);
        V[x] = tmp;
        pCount += 2;
    }

    // 8xy5 - if Vx > Vy, set VF to 1, otherwise 0. then Vy is subtracted from Vx,
    //and the results are stored in Vx
    public void subVY(char x, char y){
        if(V[x] > V[y]){
            V[0xF] = 1;
        }
        else{
            V[0xF] = 0;
        }
        char tmp = (char)((V[x] - V[y]) & 0x00FF);
        V[x] = tmp;
        pCount += 2;
    }

    // 8xy6 - if the least significant bit of Vx is 1, then VF is set to 1, otherwise 0.
    //Then Vx is divided by 2.
    public void leastSigBit(char x, char y){
        V[0xF] = (char)(V[x] & 0x0001);
        V[x] = (char)(V[x] >>> 1);
        pCount += 2;
    }

    // 8xy7 - if Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy,
    //and the results stored in Vx
    public void subtractVX(char x, char y){
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
    public void mostSigBit(char x, char y){
        char tmp = (char)(V[x] >> 7);
        V[0xF] = tmp;
        V[x] = (char)((V[x] << 1) & 0x00FF);
        pCount += 2;
    }

    // 9xy0 - compare values of Vx and Vy and if they are not equal, skip next instruction
    public void cmpVXVY(char x, char y){
        if(V[x] != V[y]){
            pCount += 4;
        }
        else{
            pCount += 2;
        }
    }

    // Annn - set iReg to nnn
    public void setIReg(short nnn){
        iReg = (short)(nnn & 0x0FFF);
        pCount += 2;
    }

    // Bnnn - set pCount to nnn plus the value of V0
    public void setPCount(short nnn){
        pCount = (short)((nnn + V[0]));
    }

    // Cxkk - generate a random number from 0 - 255 and perform AND operation on it and the value kk.
    // store the results in Vx
    public void putRandVX(char x, char kk){
        Random r = new Random();
        int rand = r.nextInt(256);
        V[x] = (char)((kk & rand) & 0x00FF);
        pCount += 2;
    }

    // Ex9E - checks the keyboard, and if the key corresponding to the value of Vx is currently
    //in the down position, pCount is incremented by 2
    public void isKeyPressed(char x){
        if(controller.isPressed((char)(V[x] & 0x000F))){
            pCount += 4;
        }
        else{
            pCount += 2;
        }
    }

    // ExA1 - checks the keyboard, and if the key corresponding to the value of Vx is currently
    //in the up position, pCount is incremented by 2
    public void isKeyReleased(char x){
        if(!controller.isPressed((char)(V[x] & 0x000F))){
            pCount += 4;
        }
        else{
            pCount += 2;
        }
    }

    // Fx07 - The value of delayTimer is placed into Vx
    public void putDelayTimer(char x){
        V[x] = delayTimer;
        pCount += 2;
    }

    // Fx0A - All execution stops until a key is pressed, then the value of that key is stored in Vx
    public void pauseExec(char x){
        V[x] = (char)(controller.waitForKeyPress());
        pCount += 2;
    }

    // Fx15 - delayTimer is set equal to the value of Vx
    public void setDelayTimerVX(char x){
        delayTimer = V[x];
        pCount += 2;
    }

    // Fx18 - soundTimer is set equal to the value of Vx
    public void setSoundTimer(char x){
        soundTimer = V[x];
        pCount += 2;
    }

    // Fx1E - the values of iReg and Vx are added, and the results are stored in iReg
    public void addIReg(char x){
        int int_vx = V[x] & 0xFF;
        int int_i = iReg & 0xFFFF;
        iReg = (short)(int_vx + int_i);
        pCount += 2;
    }

    // Fx29 - the value of iReg is set to location of the hexadecimal sprite corresponding to the
    // value of Vx
    public void setIRegHex(char x){
        iReg = (short)((V[x] * 5) & 0x00FF);
        pCount += 2;
    }

    // Fx33 - take the decimal value of Vx and place the hundreds digit in memory at I,
    // the tens digit at location I+1, and the ones digit at location I+2.
    public void putIRegValues(char x){
        int vx = V[x];
        int hundreds = vx / 100;
        vx = vx - (hundreds * 100);
        int tens = vx / 10;
        vx = vx - (tens * 10);
        memory[iReg] = (char)hundreds;
        memory[iReg + 1] = (char)tens;
        memory[iReg + 2] = (char)vx;
        pCount += 2;
    }

    // Fx55 - copy the values of registers V0 through Vx into memory, starting at the address in iReg
    public void regToMem(char x){
        int tmp = x;
        for(int i = 0; i <= tmp; i++){
            memory[iReg + i] = V[i];
        }
        pCount += 2;
    }

    //// Fx65 - Read the values in memory starting at location iReg into the registers V0 through Vx
    public void memToReg(char x){
        int tmpX = x;
        for(int i = 0; i <= tmpX; i++){
            V[i] = memory[iReg + i];
        }
        pCount +=2;
    }


    //dxyn - draw method
    public void drawSprite(char x, char y, int n){

        byte vf = (byte)0x0;
        for(int i = 0; i < n; i++){

            byte spriteByte = (byte)(memory[iReg + i]);
            for(int z = 0; z <=7; z++){

                int int_x = V[x] & 0xFF;
                int int_y = V[y] & 0xFF;
                int xCoord = (int_x + z)%64;
                int yCoord = (int_y + i)%32;

                //get previous bit held in the screen and XOR it with the corresponding bit in spriteByte
                boolean previousPixel = emuDisplay.getScreenPixel(xCoord, yCoord);
                boolean newPixel = previousPixel ^ isPixelSet(spriteByte,7-z);

                emuDisplay.setScreenPixel(newPixel,xCoord, yCoord);

                if(previousPixel == true && newPixel == false){
                    //Set VF to 1 if there has been a pixel collision
                    vf = (byte)0x01;
                }

            }
            V[0xF] = (char)vf;
        }
        drawFlag = true;
        pCount += 2;
    }

    private  Boolean isPixelSet(byte b, int bit)
    {
        return (b & (1 << bit)) != 0;
    }



    //methods to extract data from opcodes
    public char getX(short opcode){
        return (char)((opcode & 0x0F00) >>> 8);
    }

    public char getY(short opcode){
        return (char)((opcode & 0x00F0) >>> 4);
    }

    public char getN(short opcode){
        return (char)(opcode & 0x000F);
    }

    public short getNNN(short opcode){
        return (short)(opcode & 0x0FFF);
    }

    public char getKK(short opcode){
        return (char)(opcode & 0x00FF);
    }

    //reset console and load new game
    public void loadNewGame(String game){
        cpuFreq = 900;
        cycToRefresh = cpuFreq / 60;
        refreshCycles = 0;
        pCount = 0x200;
        opcode = 0;
        iReg = 0;
        sPoint = 0;
        resetMem();
        resetStack();
        resetV();
        emuDisplay.clearGameScreen();

        //load fontset into memory
        for(int i = 0; i < fontSet.length; i++){
            char bte = fontSet[i];
            memory[i] = bte;
        }
        loadGame(game);
    }

    public void resetMem(){
        for(int i = 0; i < 4096; i++){
            memory[i] = 0;
        }
    }

    public void resetV(){
        for(int i = 0; i < 16; i++){
            V[i] = 0;
        }
    }

    public void resetStack(){
        for(int i = 0; i < 16; i++){
            stack[i] = 0;
        }
    }

    //create actionlistener for reset button and add it to resetButton
    public void addListenerToReset(){
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadNewGame(emuDisplay.getSelectedGame());
            }
        };
        emuDisplay.addListenerToReset(al);
    }
}
