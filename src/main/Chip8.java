package main;

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
    }
}
