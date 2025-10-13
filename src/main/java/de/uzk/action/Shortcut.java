package de.uzk.action;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Shortcut {
    private int extendedKeyCode;
    private int modifiers;

    public Shortcut(int extendedKeyCode, int modifiersEx) {
        setExtendedKeyCode(extendedKeyCode);
        setModifiers(modifiersEx);
    }

    public Shortcut(KeyEvent keyEvent) {
        this(keyEvent.getExtendedKeyCode(), keyEvent.getModifiersEx());
    }

    public int getExtendedKeyCode() {
        return extendedKeyCode;
    }

    private void setExtendedKeyCode(int extendedKeyCode) {
        this.extendedKeyCode = extendedKeyCode;
    }

    public int getModifiersEx() {
        return modifiers;
    }

    private void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public String getKeyText() {
        List<String> keyCharList = new ArrayList<>();

        // 1. Add Modifiers (in logical order)
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.meta", "Meta"));
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.control", "Ctrl"));
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.alt", "Alt"));
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.shift", "Shift"));
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.button1", "Button 1"));
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.button2", "Button 2"));
        if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.button3", "Button 3"));

        // 2. Add Keycode (if not modifier)
        String keyText = KeyEvent.getKeyText(extendedKeyCode);
        if (!keyCharList.contains(keyText)) {
            String errorCode = "keyCode: ";
            if (keyText.contains(errorCode)) {
                String keyName = keyText.substring(keyText.indexOf(errorCode) + errorCode.length());
                keyCharList.add("(" + keyName + ")");
            } else keyCharList.add(keyText);
        }

        return String.join("+", keyCharList);
    }

    @Override
    public String toString() {
        return getKeyText();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Shortcut other)) return false;
        return extendedKeyCode == other.getExtendedKeyCode() && modifiers == other.getModifiersEx();
    }

    @Override
    public int hashCode() {
        return Objects.hash(extendedKeyCode, modifiers);
    }
}