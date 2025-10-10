package de.uzk.actions;

import org.intellij.lang.annotations.MagicConstant;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public final class Actions {
    private Actions() {
    }

    // edit actions
    public static final KeyEvent ACTION_PIN_TIME = getKeyEvent(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_TURN_IMAGE_LEFT = getKeyEvent(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_TURN_IMAGE_RIGHT = getKeyEvent(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_SCREENSHOT = getKeyEvent(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);

    // nav actions
    public static final KeyEvent ACTION_FIRST_IMAGE = getKeyEvent(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK);
    public static final KeyEvent ACTION_PREV_IMAGE = getKeyEvent(KeyEvent.VK_LEFT, 0);
    public static final KeyEvent ACTION_NEXT_IMAGE = getKeyEvent(KeyEvent.VK_RIGHT, 0);
    public static final KeyEvent ACTION_LAST_IMAGE = getKeyEvent(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK);

    public static final KeyEvent ACTION_FIRST_LEVEL = getKeyEvent(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK);
    public static final KeyEvent ACTION_PREV_LEVEL = getKeyEvent(KeyEvent.VK_UP, 0);
    public static final KeyEvent ACTION_NEXT_LEVEL = getKeyEvent(KeyEvent.VK_DOWN, 0);
    public static final KeyEvent ACTION_LAST_LEVEL = getKeyEvent(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK);

    // window actions
    public static final KeyEvent ACTION_INCREASE_FONT = getKeyEvent(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_INCREASE_FONT_2 = getKeyEvent(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_DECREASE_FONT = getKeyEvent(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_DECREASE_FONT_2 = getKeyEvent(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK);
    public static final KeyEvent ACTION_RESTORE_FONT = getKeyEvent(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK);

    // get key event
    private static KeyEvent getKeyEvent(int keyCode, @MagicConstant(flags =
            {InputEvent.BUTTON1_DOWN_MASK, InputEvent.BUTTON2_DOWN_MASK, InputEvent.BUTTON3_DOWN_MASK,
                    InputEvent.META_DOWN_MASK, InputEvent.CTRL_DOWN_MASK, InputEvent.ALT_GRAPH_DOWN_MASK,
                    InputEvent.ALT_DOWN_MASK, InputEvent.SHIFT_DOWN_MASK}) int modifiers) {
        char keyChar = KeyEvent.getKeyText(keyCode).charAt(0);

        // Placeholder or Empty Component
        JComponent source = new JComponent() {
        };
        return new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, keyChar);
    }
}
