package de.uzk.gui.tree;

import javax.swing.*;

// TODO: Umbenennen mitsamt der Geschwister-KLassen
public final class OBar extends OBarNode {

    public OBar(JMenuBar menuBar, boolean toggleable) {
        super(menuBar, "Root", toggleable);
    }

    public OBar() {
        this(new JMenuBar(), false);
    }

    public OBar(JMenuBar menuBar) {
        this(menuBar, false);
    }

    public void add(OBarMenu menu) {
        if (menu == null) throw new NullPointerException();
        this.nodes.add(menu);
    }

    public void add(OBarMenu... newMenus) {
        if (null == newMenus) throw new NullPointerException();
        for (OBarMenu menu : newMenus) {
            add(menu);
        }
    }
}