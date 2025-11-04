package de.uzk.gui;

import de.uzk.gui.tabs.TabEdit;
import de.uzk.gui.tabs.TabMarkers;
import de.uzk.gui.tabs.TabNavigate;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import static de.uzk.config.LanguageHandler.getWord;

public class AreaTabs extends AreaContainerInteractive<JPanel> {
    // GUI-Elemente
    private JTabbedPane tabbedPane;

    public AreaTabs(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout());
        this.container.setMinimumSize(new Dimension(0, 0));

        // TabbedPane erstellen
        this.tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        // Erzwingt Fokus nach Tab-Wechsel
        this.tabbedPane.addChangeListener(e -> this.tabbedPane.requestFocusInWindow());
        this.tabbedPane.setFocusable(true);

        // Tabs hinzufügen
        this.tabbedPane.add(getWord("items.edit"), new TabEdit(this.gui).getContainer());
        this.tabbedPane.add(getWord("items.nav"), new TabNavigate(this.gui).getContainer());
        this.tabbedPane.add(getWord("items.markers"), new TabMarkers(this.gui).getContainer());
        this.container.add(this.tabbedPane, BorderLayout.CENTER);
    }

    // ========================================
    // Observer Methoden
    // ========================================
    @Override
    public void updateTheme() {
        // TabbedPane Border aktualisieren
        this.container.setBorder(BorderFactory.createLineBorder(GuiUtils.getBorderColor()));

        // Tabs Border aktualisieren
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            if (this.tabbedPane.getComponentAt(i) instanceof JPanel panel) {
                panel.setBorder(emptyBorder);
            }
        }
    }
}
