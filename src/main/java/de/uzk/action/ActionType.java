package de.uzk.action;

import java.awt.event.KeyEvent;
import java.util.List;

public enum ActionType {
    // edit shortcuts
    SHORTCUT_TOGGLE_PIN_TIME(new Shortcut(KeyEvent.VK_P, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_TURN_IMAGE_90_LEFT(new Shortcut(KeyEvent.VK_L, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_TURN_IMAGE_90_RIGHT(new Shortcut(KeyEvent.VK_R, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_TAKE_SCREENSHOT(new Shortcut(KeyEvent.VK_S, Shortcut.CTRL_DOWN)),

    // navigate shortcuts
    SHORTCUT_GO_TO_FIRST_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_LEFT, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT__GO_TO_PREV_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_LEFT)),
    SHORTCUT_GO_TO_NEXT_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_RIGHT)),
    SHORTCUT_GO_TO_LAST_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_RIGHT, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),

    SHORTCUT_GO_TO_FIRST_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_UP, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_GO_TO_PREV_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_UP)),
    SHORTCUT_GO_TO_NEXT_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_DOWN)),
    SHORTCUT_GO_TO_LAST_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_DOWN, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),

    // window shortcuts
    SHORTCUT_FONT_SIZE_DECREASE(new Shortcut(KeyEvent.VK_MINUS, Shortcut.CTRL_DOWN),
            new Shortcut(KeyEvent.VK_SUBTRACT, Shortcut.CTRL_DOWN)),
    SHORTCUT_FONT_SIZE_RESTORE(new Shortcut(KeyEvent.VK_0, Shortcut.CTRL_DOWN),
            new Shortcut(KeyEvent.VK_EQUALS, Shortcut.CTRL_DOWN)),
    SHORTCUT_FONT_SIZE_INCREASE(new Shortcut(KeyEvent.VK_PLUS, Shortcut.CTRL_DOWN),
            new Shortcut(KeyEvent.VK_ADD, Shortcut.CTRL_DOWN)),

    SHORTCUT_SHOW_DISCLAIMER(new Shortcut(KeyEvent.VK_F1)),
    SHORTCUT_SHOW_LOG_VIEWER(new Shortcut(KeyEvent.VK_F2)),

    // settings shortcuts
    SHORTCUT_SELECT_LANGUAGE(new Shortcut(KeyEvent.VK_L, Shortcut.CTRL_DOWN)),
    SHORTCUT_TOGGLE_THEME(new Shortcut(KeyEvent.VK_T, Shortcut.CTRL_DOWN)),
    SHORTCUT_OPEN_SETTINGS(new Shortcut(KeyEvent.VK_COMMA, Shortcut.CTRL_DOWN)),

    // actions
    ACTION_EDIT_IMAGE,
    ACTION_ADD_MARKER,
    ACTION_REMOVE_MARKER,

    ACTION_UPDATE_PIN_TIME,
    ACTION_UPDATE_TIME_UNIT,
    ACTION_UPDATE_LEVEL_UNIT,
    ACTION_UPDATE_SCREENSHOT_COUNTER,
    ACTION_UPDATE_FONT;

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
}