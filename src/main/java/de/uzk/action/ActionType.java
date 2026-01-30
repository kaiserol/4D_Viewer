package de.uzk.action;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// Actions -> nicht mit Shortcuts verbundene Aktionen
// Shortcut → mit Shortcuts verbundene Aktionen
public enum ActionType {
    // Projekte Shortcuts
    SHORTCUT_OPEN_FOLDER(new Shortcut(KeyEvent.VK_O, Shortcut.CTRL_DOWN)),
    SHORTCUT_OPEN_RECENT(new Shortcut(KeyEvent.VK_O, Shortcut.CTRL_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_SAVE_PROJECT(new Shortcut(KeyEvent.VK_S, Shortcut.CTRL_DOWN)),
    SHORTCUT_CLOSE_PROJECT(new Shortcut(KeyEvent.VK_W, Shortcut.CTRL_DOWN)),

    // Bearbeiten Shortcuts
    SHORTCUT_PIN_TIME(new Shortcut(KeyEvent.VK_P, Shortcut.CTRL_DOWN)),
    SHORTCUT_TURN_IMAGE_90_LEFT(new Shortcut(KeyEvent.VK_LEFT, Shortcut.ALT_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_TURN_IMAGE_90_RIGHT(new Shortcut(KeyEvent.VK_RIGHT, Shortcut.ALT_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_TAKE_SNAPSHOT(new Shortcut(KeyEvent.VK_S, Shortcut.ALT_DOWN | Shortcut.SHIFT_DOWN)),
    SHORTCUT_UNDO(new Shortcut(KeyEvent.VK_Z, Shortcut.CTRL_DOWN)),
    SHORTCUT_REDO(new Shortcut(KeyEvent.VK_Y, Shortcut.CTRL_DOWN)),

    ACTION_EDIT_IMAGE,
    ACTION_UPDATE_SNAPSHOT_COUNTER,
    ACTION_UPDATE_UNIT,

    // Markierungen Shortcuts
    ACTION_ADD_MARKER,
    ACTION_EDIT_MARKER,
    ACTION_REMOVE_MARKER,

    // Navigieren Shortcuts
    SHORTCUT_GO_TO_FIRST_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_LEFT, Shortcut.SHIFT_DOWN)),
    SHORTCUT_GO_TO_PREV_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_LEFT)),
    SHORTCUT_GO_TO_NEXT_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_RIGHT)),
    SHORTCUT_GO_TO_LAST_IMAGE(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_RIGHT, Shortcut.SHIFT_DOWN)),

    SHORTCUT_GO_TO_FIRST_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_UP, Shortcut.SHIFT_DOWN)),
    SHORTCUT_GO_TO_PREV_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_UP)),
    SHORTCUT_GO_TO_NEXT_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_DOWN)),
    SHORTCUT_GO_TO_LAST_LEVEL(KeyEventType.PRESSED, new Shortcut(KeyEvent.VK_DOWN, Shortcut.SHIFT_DOWN)),

    // Fenster Shortcuts
    SHORTCUT_FONT_SIZE_DECREASE(new Shortcut(KeyEvent.VK_MINUS, Shortcut.CTRL_DOWN), new Shortcut(KeyEvent.VK_SUBTRACT, Shortcut.CTRL_DOWN)),
    SHORTCUT_FONT_SIZE_INCREASE(new Shortcut(KeyEvent.VK_PLUS, Shortcut.CTRL_DOWN), new Shortcut(KeyEvent.VK_ADD, Shortcut.CTRL_DOWN)),
    SHORTCUT_FONT_SIZE_RESTORE(new Shortcut(KeyEvent.VK_0, Shortcut.CTRL_DOWN), new Shortcut(KeyEvent.VK_EQUALS, Shortcut.CTRL_DOWN)),
    SHORTCUT_OPEN_SETTINGS(new Shortcut(KeyEvent.VK_COMMA, Shortcut.CTRL_DOWN)),

    ACTION_UPDATE_FONT,

    // Hilfe Shortcuts
    SHORTCUT_SHOW_ABOUT,
    SHORTCUT_SHOW_LEGAL,
    SHORTCUT_SHOW_HISTORY,
    SHORTCUT_SHOW_LOG_VIEWER(new Shortcut(KeyEvent.VK_F12));

    private final List<Shortcut> shortcuts;
    private final KeyEventType keyEventType;

    ActionType(KeyEventType keyEventType, Shortcut... shortcuts) {
        if (shortcuts == null) throw new NullPointerException("Shortcuts are null.");
        if (Arrays.stream(shortcuts).anyMatch(Objects::isNull)) throw new NullPointerException("Shortcut is null.");
        if (keyEventType == null) throw new NullPointerException("KeyEventType is null.");
        this.shortcuts = List.of(shortcuts);
        this.keyEventType = keyEventType;
    }

    ActionType(Shortcut... shortcuts) {
        if (shortcuts == null) throw new NullPointerException("Shortcuts are null.");
        if (Arrays.stream(shortcuts).anyMatch(Objects::isNull)) throw new NullPointerException("Shortcut is null.");
        this.shortcuts = List.of(shortcuts);
        this.keyEventType = !this.shortcuts.isEmpty() ? KeyEventType.RELEASED : KeyEventType.NONE;
    }

    public List<Shortcut> getShortcuts() {
        return this.shortcuts;
    }

    private KeyEventType getKeyEventType() {
        return this.keyEventType;
    }

    public static ActionType fromKeyEvent(KeyEvent e) {
        if (e != null) {
            for (ActionType actionType : ActionType.values()) {
                for (Shortcut shortcut : actionType.getShortcuts()) {
                    boolean sameShortcut = Objects.equals(shortcut, new Shortcut(e));
                    boolean sameID = actionType.getKeyEventType().getID() == e.getID();
                    if (sameShortcut && sameID) return actionType;
                }
            }
        }
        return null;
    }

    /**
     * Mögliche KeyEvents
     */
    private enum KeyEventType {
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