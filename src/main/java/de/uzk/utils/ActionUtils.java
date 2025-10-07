package de.uzk.utils;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public final class ActionUtils {
    private ActionUtils() {
    }

    // edit actions
    public static final KeyEvent PIN_TIME_ACTION = getKeyEvent(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent TURN_IMAGE_LEFT_ACTION = getKeyEvent(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent TURN_IMAGE_RIGHT_ACTION = getKeyEvent(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent SCREENSHOT_ACTION = getKeyEvent(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);

    // nav actions
    public static final KeyEvent FIRST_IMAGE_ACTION = getKeyEvent(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent PREV_IMAGE_ACTION = getKeyEvent(KeyEvent.VK_LEFT, KeyEvent.VK_UNDEFINED);
    public static final KeyEvent NEXT_IMAGE_ACTION = getKeyEvent(KeyEvent.VK_RIGHT, KeyEvent.VK_UNDEFINED);
    public static final KeyEvent LAST_IMAGE_ACTION = getKeyEvent(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK);

    public static final KeyEvent FIRST_LEVEL_ACTION = getKeyEvent(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent PREV_LEVEL_ACTION = getKeyEvent(KeyEvent.VK_UP, KeyEvent.VK_UNDEFINED);
    public static final KeyEvent NEXT_LEVEL_ACTION = getKeyEvent(KeyEvent.VK_DOWN, KeyEvent.VK_UNDEFINED);
    public static final KeyEvent LAST_LEVEL_ACTION = getKeyEvent(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK);

    // option actions
    public static final KeyEvent TOGGLE_THEME_ACTION = getKeyEvent(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent INCREASE_PLUS_FONT_ACTION = getKeyEvent(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent INCREASE_ADD_FONT_ACTION = getKeyEvent(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent DECREASE_MINUS_FONT_ACTION = getKeyEvent(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent DECREASE_SUBTRACT_FONT_ACTION = getKeyEvent(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent RESTORE_FONT_ACTION = getKeyEvent(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK);

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
