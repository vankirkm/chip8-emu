package main;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Controller {

    private boolean[] keyPressed;
    private byte lastPressed;

    public Controller(){
        keyPressed = new boolean[16];
        prepareInput();
    }

    public void prepareInput(){
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                synchronized (Controller.class){
                    switch(e.getID()){
                        case KeyEvent.KEY_PRESSED:
                            setKey(e.getKeyCode(), true);
                            break;
                        case KeyEvent.KEY_RELEASED:
                            setKey(e.getKeyCode(), false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    public void setKey(int key, boolean pressed){
        switch(key){
            //1
            case 49:
                keyPressed[0x1] = pressed;
                lastPressed = 0x1;
                break;

            //2
            case 50:
                keyPressed[0x2] = pressed;
                lastPressed = 0x2;
                break;

            //3
            case 51:
                keyPressed[0x3] = pressed;
                lastPressed = 0x3;
                break;

            //4
            case 52:
                keyPressed[0xC] = pressed;
                lastPressed = 0xC;
                break;

            //Q
            case 81:
                keyPressed[0x4] = pressed;
                lastPressed = 0x4;
                break;

            //W
            case 87:
                keyPressed[0x5] = pressed;
                lastPressed = 0x5;
                break;

            //E
            case 69:
                keyPressed[0x6] = pressed;
                lastPressed = 0x6;
                break;

            //R
            case 82:
                keyPressed[0xD] = pressed;
                lastPressed = 0xD;
                break;
            //A
            case 65:
                keyPressed[0x7] = pressed;
                lastPressed = 0x7;
                break;

            //S
            case 83:
                keyPressed[0x8] = pressed;
                lastPressed = 0x8;
                break;

            //D
            case 68:
                keyPressed[0x9] = pressed;
                lastPressed = 0x9;
                break;

            //F
            case 70:
                keyPressed[0xE] = pressed;
                lastPressed = 0xE;
                break;

            //Z
            case 90:
                keyPressed[0xA] = pressed;
                lastPressed = 0xA;
                break;

            //X
            case 88:
                keyPressed[0x0] = pressed;
                lastPressed = 0x0;
                break;

            //C
            case 67:
                keyPressed[0xB] = pressed;
                lastPressed = 0xB;
                break;

            //V
            case 86:
                keyPressed[0xF] = pressed;
                lastPressed = 0xF;
                break;
        }
    }

    public boolean isPressed(char x){
        return keyPressed[x];
    }

}
