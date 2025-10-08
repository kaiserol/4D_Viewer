package de.uzk.gui.tabs;

import de.uzk.gui.Gui;
import de.uzk.gui.InteractiveContainer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

class TabContent extends InteractiveContainer<JPanel> {
    public TabContent(JPanel container, Gui gui) {
        super(container, gui);
    }

    @Override
    public void updateUI() {
        this.container.setBorder(new EmptyBorder(10, 10, 10, 10));
    }
}
