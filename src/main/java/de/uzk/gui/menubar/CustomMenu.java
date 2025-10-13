package de.uzk.gui.menubar;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class CustomMenu extends CustomMenuBarNode {
    private final List<CustomMenuBarNode> nodes;

    public CustomMenu(String name) {
        super(new JMenu(), name);
        this.nodes = new ArrayList<>();
    }

    public List<CustomMenuBarNode> getNodes() {
        return nodes;
    }

    public void add(CustomMenuBarNode node) {
        if (node == null) throw new NullPointerException("Node is null.");
        this.getNodes().add(node);
        this.getComponent().add(node.getComponent());
    }

    public void add(CustomMenuBarNode... nodes) {
        if (null == nodes) throw new NullPointerException("Nodes is null.");
        for (CustomMenuBarNode node : nodes) add(node);
    }

    public void addSeparator() {
        add(new CustomMenuSeparator());
    }
}