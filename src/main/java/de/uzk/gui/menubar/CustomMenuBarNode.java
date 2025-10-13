package de.uzk.gui.menubar;

import javax.swing.*;
import java.util.Objects;

public abstract sealed class CustomMenuBarNode permits CustomMenu, CustomMenuItem, CustomMenuSeparator {
    private final JComponent component;
    private final String text;

    public CustomMenuBarNode(JComponent component, String text) {
        this.component = this.getComponent(component);
        this.text = this.setText(text);
    }

    private JComponent getComponent(JComponent component) {
        if (component == null) throw new NullPointerException("Component is null.");
        return component;
    }

    public JComponent getComponent() {
        return component;
    }

    public String getText() {
        return text;
    }

    private String setText(String text) {
        if (this.component instanceof JMenuItem item) item.setText(text);
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
