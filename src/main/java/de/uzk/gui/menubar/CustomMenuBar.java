package de.uzk.gui.menubar;

import javax.swing.*;

public final class CustomMenuBar extends CustomMenuNode {
    public CustomMenuBar(JMenuBar menuBar, boolean toggleable) {
        super(menuBar, "Root", toggleable);
    }

    public CustomMenuBar() {
        this(new JMenuBar(), false);
    }

    public CustomMenuBar(JMenuBar menuBar) {
        this(menuBar, false);
    }

    public void add(CustomMenu menu) {
        if (menu == null) throw new NullPointerException();
        this.getNodes().add(menu);
        this.getComponent().add(menu.getComponent());
    }

    public void add(CustomMenu... menus) {
        if (null == menus) throw new NullPointerException();
        for (CustomMenu menu : menus) add(menu);
    }
}