package de.uzk.actions;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

// Benenne in Actions um
public final class ActionUtils {
    private ActionUtils() {
    }

    // edit actions
    public static final KeyEvent ACTION_PIN_TIME = getKeyEvent(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_TURN_IMAGE_LEFT = getKeyEvent(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_TURN_IMAGE_RIGHT = getKeyEvent(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_SCREENSHOT = getKeyEvent(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);

    // nav actions
    public static final KeyEvent ACTION_FIRST_IMAGE = getKeyEvent(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_PREV_IMAGE = getKeyEvent(KeyEvent.VK_LEFT, KeyEvent.VK_UNDEFINED);
    public static final KeyEvent ACTION_NEXT_IMAGE = getKeyEvent(KeyEvent.VK_RIGHT, KeyEvent.VK_UNDEFINED);
    public static final KeyEvent ACTION_LAST_IMAGE = getKeyEvent(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK);

    public static final KeyEvent ACTION_FIRST_LEVEL = getKeyEvent(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_PREV_LEVEL = getKeyEvent(KeyEvent.VK_UP, KeyEvent.VK_UNDEFINED);
    public static final KeyEvent ACTION_NEXT_LEVEL = getKeyEvent(KeyEvent.VK_DOWN, KeyEvent.VK_UNDEFINED);
    public static final KeyEvent ACTION_LAST_LEVEL = getKeyEvent(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK);

    // option actions
    public static final KeyEvent ACTION_CHANGE_LANGUAGE = getKeyEvent(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_TOGGLE_THEME = getKeyEvent(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_INCREASE_FONT = getKeyEvent(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_INCREASE_FONT_2 = getKeyEvent(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_DECREASE_FONT = getKeyEvent(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_DECREASE_FONT_2 = getKeyEvent(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_RESTORE_FONT = getKeyEvent(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK);

    // delay in ms
    public static final long MUCH_COMPUTING_TIME_DELAY = 80;
    public static final long KEY_PRESS_DELAY = 50;
    public static final long MOUSE_WHEEL_DELAY = 25;

    private static KeyEvent getKeyEvent(int keyCode, int modifiers) {
        char keyChar = KeyEvent.getKeyText(keyCode).charAt(0);

        // Placeholder or Dummy Component
        JComponent source = new JComponent() {
        };
        return new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, keyChar);
    }
}
