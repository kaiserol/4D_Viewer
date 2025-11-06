package de.uzk.action;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum ActionType {
    // Bearbeiten Shortcuts
    SHORTCUT_TURN_IMAGE_90_LEFT(new Shortcut(KeyEvent.VK_LEFT, Shortcut.ALT_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_TURN_IMAGE_90_RIGHT(new Shortcut(KeyEvent.VK_RIGHT, Shortcut.ALT_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_TAKE_SCREENSHOT(new Shortcut(KeyEvent.VK_S, Shortcut.CTRL_DOWN)),

    ACTION_EDIT_IMAGE,
    ACTION_UPDATE_SCREENSHOT_COUNTER,

    // Navigieren Shortcuts
    SHORTCUT_GO_TO_FIRST_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_LEFT, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_GO_TO_PREV_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_LEFT)),
    SHORTCUT_GO_TO_NEXT_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_RIGHT)),
    SHORTCUT_GO_TO_LAST_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_RIGHT, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),

    SHORTCUT_GO_TO_FIRST_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_UP, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_GO_TO_PREV_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_UP)),
    SHORTCUT_GO_TO_NEXT_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_DOWN)),
    SHORTCUT_GO_TO_LAST_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_DOWN, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),

    //Projekte Shortcuts
    SHORTCUT_OPEN_RECENT(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_O, Shortcut.CTRL_DOWN)),
    SHORTCUT_OPEN_FOLDER(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_O, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_SAVE_CONFIG(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_S, Shortcut.CTRL_DOWN)),
    SHORTCUT_CLOSE_PROJECT(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_BACK_SPACE, Shortcut.CTRL_DOWN)),
    // Markierungen Shortcuts
    ACTION_ADD_MARKER,
    ACTION_REMOVE_MARKER,

    // Sonstige Shortcuts
    SHORTCUT_TOGGLE_PIN_TIME(new Shortcut(KeyEvent.VK_P, Shortcut.CTRL_DOWN)),

    // Fenster Shortcuts
    SHORTCUT_FONT_SIZE_DECREASE(new Shortcut(KeyEvent.VK_MINUS, Shortcut.CTRL_DOWN),
            new Shortcut(KeyEvent.VK_SUBTRACT, Shortcut.CTRL_DOWN)),
    SHORTCUT_FONT_SIZE_INCREASE(new Shortcut(KeyEvent.VK_PLUS, Shortcut.CTRL_DOWN),
            new Shortcut(KeyEvent.VK_ADD, Shortcut.CTRL_DOWN)),
    SHORTCUT_FONT_SIZE_RESTORE(new Shortcut(KeyEvent.VK_0, Shortcut.CTRL_DOWN),
            new Shortcut(KeyEvent.VK_EQUALS, Shortcut.CTRL_DOWN)),
    SHORTCUT_OPEN_SETTINGS(new Shortcut(KeyEvent.VK_COMMA, Shortcut.CTRL_DOWN)),

    ACTION_UPDATE_FONT,

    // Hilfe Shortcuts
    SHORTCUT_SHOW_DISCLAIMER(new Shortcut(KeyEvent.VK_F1)),
    SHORTCUT_SHOW_VERSIONS(new Shortcut(KeyEvent.VK_F2)),
    SHORTCUT_SHOW_LOG_VIEWER(new Shortcut(KeyEvent.VK_F3));

    // ========================================
    // Enum Deklaration
    // ========================================
    private final KeyEventType keyEventType;
    private final List<Shortcut> shortcuts;

    ActionType(KeyEventType keyEventType, Shortcut... shortcuts) {
        if (keyEventType == null) throw new NullPointerException("KeyEventType is null.");
        if (shortcuts == null) throw new NullPointerException("Shortcuts are null.");
        if (Arrays.stream(shortcuts).anyMatch(Objects::isNull)) throw new NullPointerException("Shortcut is null.");
        this.keyEventType = keyEventType;
        this.shortcuts = List.of(shortcuts);
    }

    ActionType(Shortcut... shortcuts) {
        if (shortcuts == null) throw new NullPointerException("Shortcuts are null.");
        if (Arrays.stream(shortcuts).anyMatch(Objects::isNull)) throw new NullPointerException("Shortcut is null.");
        this.shortcuts = List.of(shortcuts);
        this.keyEventType = !this.shortcuts.isEmpty() ? KeyEventType.RELEASED : KeyEventType.NONE;
    }

    public KeyEventType getKeyEventType() {
        return this.keyEventType;
    }

    public List<Shortcut> getShortcuts() {
        return this.shortcuts;
    }

    public static ActionType fromKeyEvent(KeyEvent e) {
        if (e != null) {
            for (ActionType actionType : ActionType.values()) {
                for (Shortcut shortcut : actionType.getShortcuts()) {
                    boolean sameShortcut = shortcut.equals(new Shortcut(e));
                    boolean sameID = actionType.getKeyEventType().getID() == e.getID();
                    if (sameShortcut && sameID) return actionType;
                }
            }
        }
        return null;
    }
}