package de.uzk.action;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.uzk.Main.operationSystem;

public class Shortcut {
    public static final int UNDEFINED = 0;
    public static final int CTRL_DOWN = operationSystem.isMacOS() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
    public static final int ALT_DOWN = InputEvent.ALT_DOWN_MASK;
    public static final int SHIFT_DOWN = InputEvent.SHIFT_DOWN_MASK;
    public static final int ALT_GRAPH_DOWN = InputEvent.ALT_GRAPH_DOWN_MASK;
    public static final int BUTTON1_DOWN = InputEvent.BUTTON1_DOWN_MASK;
    public static final int BUTTON2_DOWN = InputEvent.BUTTON2_DOWN_MASK;
    public static final int BUTTON3_DOWN = InputEvent.BUTTON3_DOWN_MASK;

    private int extendedKeyCode;
    private int modifiers;

    public Shortcut(int extendedKeyCode, int modifiersEx) {
        setExtendedKeyCode(extendedKeyCode);
        setModifiers(modifiersEx);
    }

    public Shortcut(int extendedKeyCode) {
        this(extendedKeyCode, UNDEFINED);
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
        // 1. Modifiers in logischer Reihenfolge hinzufügen
        List<String> keyCharList = getModifiersList(modifiers);

        // 2. Keycodes hinzufügen (wenn KeyCode kein Modifier ist)
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

    public static List<String> getModifiersList(int modifiersEx) {
        List<String> keyCharList = new ArrayList<>();

        if ((modifiersEx & InputEvent.META_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.meta", "Meta"));
        if ((modifiersEx & InputEvent.CTRL_DOWN_MASK) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.control", "Ctrl"));
        if ((modifiersEx & ALT_DOWN) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.alt", "Alt"));
        if ((modifiersEx & ALT_GRAPH_DOWN) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
        if ((modifiersEx & SHIFT_DOWN) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.shift", "Shift"));
        if ((modifiersEx & BUTTON1_DOWN) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.button1", "Button 1"));
        if ((modifiersEx & BUTTON2_DOWN) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.button2", "Button 2"));
        if ((modifiersEx & BUTTON3_DOWN) != 0)
            keyCharList.add(Toolkit.getProperty("AWT.button3", "Button 3"));

        return keyCharList;
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

    @Override
    public String toString() {
        return getKeyText();
    }
}