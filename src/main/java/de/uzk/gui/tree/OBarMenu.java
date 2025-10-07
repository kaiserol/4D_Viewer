package de.uzk.gui.tree;

import javax.swing.*;

public final class OBarMenu extends OBarNode {
    public OBarMenu(String name, boolean toggleable) {
        super(new JMenu(), name, toggleable);
    }

    public OBarMenu(String name) {
        this(name, false);
    }


    public void add(OBarNode newNode) {
        if (newNode == null) throw new NullPointerException();
        this.nodes.add(newNode);
        this.component.add(newNode.getComponent());
    }

    public void add(OBarNode... newNodes) {
        if (null == newNodes) throw new NullPointerException();
        for (OBarNode node : newNodes) {
            add(node);
        }
    }

    public void addSeparator() {
        add(new OBarItem.OBarSeparator());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}