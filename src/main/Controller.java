package main;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

public class Controller {

    private boolean[] keyPressed;
    private byte lastPressed;
    private int numKeysPressed;

    public Controller(){
        keyPressed = new boolean[16];
        getInput();
        numKeysPressed = 0;
    }

    public void getInput(){
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            switch(e.getID()){
                case KeyEvent.KEY_PRESSED:
                    if(setKey(e.getKeyCode(), true)){
                        numKeysPressed++;
                    }
                    break;
                    case KeyEvent.KEY_RELEASED:
                        if(setKey(e.getKeyCode(), false)){
                            numKeysPressed++;
                        }
                        break;
            }
            return false;
        });
    }

    public boolean setKey(int key, boolean pressed){
        switch(key){
            //1
            case KeyEvent.VK_1:
                keyPressed[0x1] = pressed;
                lastPressed = 0x1;
                break;

            //2
            case KeyEvent.VK_2:
                keyPressed[0x2] = pressed;
                lastPressed = 0x2;
                break;

            //3
            case KeyEvent.VK_3:
                keyPressed[0x3] = pressed;
                lastPressed = 0x3;
                break;

            //4
            case KeyEvent.VK_4:
                keyPressed[0xC] = pressed;
                lastPressed = 0xC;
                break;

            //Q
            case KeyEvent.VK_Q:
                keyPressed[0x4] = pressed;
                lastPressed = 0x4;
                break;

            //W
            case KeyEvent.VK_W:
                keyPressed[0x5] = pressed;
                lastPressed = 0x5;
                break;

            //E
            case KeyEvent.VK_E:
                keyPressed[0x6] = pressed;
                lastPressed = 0x6;
                break;

            //R
            case KeyEvent.VK_R:
                keyPressed[0xD] = pressed;
                lastPressed = 0xD;
                break;
            //A
            case KeyEvent.VK_A:
                keyPressed[0x7] = pressed;
                lastPressed = 0x7;
                break;

            //S
            case KeyEvent.VK_S:
                keyPressed[0x8] = pressed;
                lastPressed = 0x8;
                break;

            //D
            case KeyEvent.VK_D:
                keyPressed[0x9] = pressed;
                lastPressed = 0x9;
                break;

            //F
            case KeyEvent.VK_F:
                keyPressed[0xE] = pressed;
                lastPressed = 0xE;
                break;

            //Z
            case KeyEvent.VK_Z:
                keyPressed[0xA] = pressed;
                lastPressed = 0xA;
                break;

            //X
            case KeyEvent.VK_X:
                keyPressed[0x0] = pressed;
                lastPressed = 0x0;
                break;

            //C
            case KeyEvent.VK_C:
                keyPressed[0xB] = pressed;
                lastPressed = 0xB;
                break;

            //V
            case KeyEvent.VK_V:
                keyPressed[0xF] = pressed;
                lastPressed = 0xF;
                break;

            default:
                return false;
        }
        return true;
    }

    public byte waitForKeyPress(){
        while(numKeysPressed == 0){
            getInput();
        }
        return lastPressed;
    }


    public boolean isPressed(char x){
        return keyPressed[x & 0x000F];
    }

}
