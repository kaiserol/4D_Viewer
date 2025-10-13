package de.uzk.action;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public enum ActionType {
    // edit actions
    SHORTCUT_TOGGLE_PIN_TIME(new Shortcut(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK)),
    SHORTCUT_TURN_IMAGE_90_LEFT(new Shortcut(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK)),
    SHORTCUT_TURN_IMAGE_90_RIGHT(new Shortcut(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK)),
    SHORTCUT_TAKE_SCREENSHOT(new Shortcut(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)),

    ACTION_LOAD_IMAGES,
    ACTION_EDIT_IMAGE,
    ACTION_UPDATE_PIN_TIME,
    ACTION_UPDATE_SCREENSHOT_COUNTER,
    ACTION_ADD_MARKER,
    ACTION_REMOVE_MARKER,

    // nav actions
    SHORTCUT_GO_TO_FIRST_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)),
    SHORTCUT__GO_TO_PREV_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_LEFT, 0)),
    SHORTCUT_GO_TO_NEXT_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_RIGHT, 0)),
    SHORTCUT_GO_TO_LAST_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)),

    SHORTCUT_GO_TO_FIRST_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)),
    SHORTCUT_GO_TO_PREV_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_UP, 0)),
    SHORTCUT_GO_TO_NEXT_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_DOWN, 0)),
    SHORTCUT_GO_TO_LAST_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)),

    ACTION_UPDATE_TIME_UNIT,
    ACTION_UPDATE_LEVEL_UNIT,

    // window actions
    SHORTCUT_FONT_SIZE_DECREASE(new Shortcut(KeyEvent.VK_MINUS, InputEvent.META_DOWN_MASK),
            new Shortcut(KeyEvent.VK_SUBTRACT, InputEvent.META_DOWN_MASK)),
    SHORTCUT_FONT_SIZE_RESTORE(new Shortcut(KeyEvent.VK_0, InputEvent.META_DOWN_MASK)),
    SHORTCUT_FONT_SIZE_INCREASE(new Shortcut(KeyEvent.VK_PLUS, InputEvent.META_DOWN_MASK),
            new Shortcut(KeyEvent.VK_ADD, InputEvent.META_DOWN_MASK)),

    SHORTCUT_SHOW_DISCLAIMER(new Shortcut(KeyEvent.VK_F1, 0)),
    SHORTCUT_SHOW_LOG_VIEWER(new Shortcut(KeyEvent.VK_F12, 0)),

    ACTION_SELECT_LANGUAGE,
    ACTION_TOGGLE_THEME;

    // -------------------- enum declaration --------------------

    private final List<Shortcut> shortcuts;
    private final KeyEventType keyEventType;

    ActionType(KeyEventType keyEventType, Shortcut... shortcuts) {
        if (keyEventType == null) throw new NullPointerException("KeyEventType is null.");
        this.shortcuts = List.of(shortcuts);
        this.keyEventType = keyEventType;
    }

    ActionType(Shortcut... shortcuts) {
        this.shortcuts = List.of(shortcuts);
        this.keyEventType = this.shortcuts.isEmpty() ? KeyEventType.NONE : KeyEventType.RELEASED;
    }

    public List<Shortcut> getShortcuts() {
        return this.shortcuts;
    }

    public KeyEventType getKeyEventType() {
        return this.keyEventType;
    }

    public static ActionType getAction(KeyEvent e) {
        if (e != null) {
            for (ActionType actionType : ActionType.values()) {
                for (Shortcut shortcut : actionType.getShortcuts()) {
                    if (shortcut.equals(new Shortcut(e)) && actionType.getKeyEventType().getID() == e.getID()) {
                        return actionType;
                    }
                }
            }
        }
        return null;
    }

    public enum KeyEventType {
        NONE(-1),
        PRESSED(KeyEvent.KEY_PRESSED),
        RELEASED(KeyEvent.KEY_RELEASED);

        private final int id;

        KeyEventType(int id) {
            this.id = id;
        }

        public int getID() {
            return id;
        }
    }
}