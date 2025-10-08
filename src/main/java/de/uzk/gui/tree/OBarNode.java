package de.uzk.gui.tree;

import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract sealed class OBarNode permits OBarItem, OBarMenu, OBarItem.OBarSeparator, OBar {
    protected final List<OBarNode> nodes;
    protected final JComponent component;
    protected final String text;
    protected final boolean toggleable;

    protected OBarNode(JComponent component, String text, boolean toggleable) {
        this.nodes = new ArrayList<>();
        this.component = this.getComponent(component);
        this.text = this.getText(text);
        this.toggleable = toggleable;
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

    public final List<OBarNode> getNodes() {
        return nodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OBarNode node = (OBarNode) o;
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

    private String toString(OBarNode node, int depth) {
        StringBuilder result = new StringBuilder();
        final String tab = "  ";
        final String indent = tab.repeat(Math.max(0, depth));

        // OTreeMenu -> getName
        result.append(node.getText()).append(StringUtils.NEXT_LINE);

        for (OBarNode innerNode : node.getNodes()) {
            result.append(indent).append(tab);
            if (innerNode instanceof OBarItem) result.append("> ");

            result.append(toString(innerNode, depth + 1));
        }
        return result.toString();
    }
}
