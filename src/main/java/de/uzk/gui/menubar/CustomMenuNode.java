package de.uzk.gui.menubar;

import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract sealed class CustomMenuNode permits CustomMenuBar, CustomMenu, CustomMenuItem, CustomMenuSeparator {
    private final List<CustomMenuNode> nodes;
    private final JComponent component;
    private final String text;
    private final boolean toggleable;

    CustomMenuNode(JComponent component, String text, boolean toggleable) {
        this.nodes = new ArrayList<>();
        this.component = this.getComponent(component);
        this.text = this.getText(text);
        this.toggleable = toggleable;
    }

    public final List<CustomMenuNode> getNodes() {
        return nodes;
    }

    private JComponent getComponent(JComponent component) {
        if (component == null) throw new NullPointerException();
        return component;
    }

    public final JComponent getComponent() {
        return component;
    }

    private String getText(String text) {
        if (this.component instanceof JMenuItem menuItem) {
            menuItem.setText(text);
        }
        return text;
    }

    public final String getText() {
        return text;
    }

    public final boolean isToggleable() {
        return toggleable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomMenuNode node = (CustomMenuNode) o;
        return Objects.equals(text, node.text) && Objects.equals(nodes, node.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, nodes);
    }

    @Override
    public String toString() {
        return toString(this, 0);
    }

    private String toString(CustomMenuNode node, int depth) {
        final String tab = "  ";
        final String indent = tab.repeat(Math.max(0, depth));
        final StringBuilder result = new StringBuilder();

        // return the separator
        if (node instanceof CustomMenuSeparator) {
            result.append("-".repeat(10)).append(StringUtils.NEXT_LINE);
            return result.toString();
        }

        // return the menuBar
        result.append(node.getText()).append(StringUtils.NEXT_LINE);
        for (CustomMenuNode innerNode : node.getNodes()) {
            result.append(indent).append(tab);
            if (innerNode instanceof CustomMenu) result.append("# ");
            else if (innerNode instanceof CustomMenuItem) result.append("> ");

            result.append(toString(innerNode, depth + 1));
        }
        return result.toString();
    }
}
