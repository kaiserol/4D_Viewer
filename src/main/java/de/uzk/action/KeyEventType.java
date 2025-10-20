package de.uzk.action;

import java.awt.event.KeyEvent;

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