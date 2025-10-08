package de.uzk.gui.menubar;

import javax.swing.*;

final class CustomMenuSeparator extends CustomMenuNode {
    public CustomMenuSeparator() {
        super(new JSeparator(SwingConstants.HORIZONTAL), "SEP", false);
    }
}