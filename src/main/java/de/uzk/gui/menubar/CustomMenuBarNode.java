package de.uzk.gui.menubar;

import javax.swing.*;
import java.util.Objects;

public abstract sealed class CustomMenuBarNode permits CustomMenu, CustomMenuItem, CustomMenuSeparator {
    private final JComponent component;
    private final String text;

    public CustomMenuBarNode(JComponent component, String text) {
        if (component == null) throw new NullPointerException("Component is null.");
        if (text == null) throw new NullPointerException("Text is null.");
        this.component = component;
        this.text = text;
        if (this.component instanceof JMenuItem item) {
            item.setText(text);
        }
    }

    public JComponent getComponent() {
        return component;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomMenuBarNode node = (CustomMenuBarNode) o;
        return Objects.equals(text, node.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return text;
    }
}
