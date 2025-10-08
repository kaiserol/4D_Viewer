package de.uzk.gui.menubar;

import javax.swing.*;

public final class CustomMenu extends CustomMenuNode {
    public CustomMenu(String name, boolean toggleable) {
        super(new JMenu(), name, toggleable);
    }

    public CustomMenu(String name) {
        this(name, false);
    }

    public void add(CustomMenuNode node) {
        if (node == null) throw new NullPointerException();
        this.nodes.add(node);
        this.component.add(node.getComponent());
    }

    public void add(CustomMenuNode... nodes) {
        if (null == nodes) throw new NullPointerException();
        for (CustomMenuNode node : nodes) add(node);
    }

    public void addSeparator() {
        add(new CustomMenuSeparator());
    }
}