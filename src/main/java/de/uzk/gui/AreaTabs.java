package de.uzk.gui;

import de.uzk.action.ActionHandler;
import de.uzk.gui.tabs.TabEdit;
import de.uzk.gui.tabs.TabMarkers;
import de.uzk.gui.tabs.TabNav;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static de.uzk.config.LanguageHandler.getWord;

public class AreaTabs extends AreaContainerInteractive<JPanel>  {
    private final ActionHandler actionHandler;

    public AreaTabs(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout());
        this.container.setMinimumSize(new Dimension(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        Border border = new EmptyBorder(10, 10, 10, 10);

        // tabs
        TabEdit tabEdit = new TabEdit(this.gui, this.actionHandler);
        tabEdit.getContainer().setBorder(border);
        tabbedPane.add(getWord("items.edit"), tabEdit.getContainer());

        TabNav tabNav = new TabNav(this.gui, this.actionHandler);
        tabNav.getContainer().setBorder(border);
        tabbedPane.add(getWord("items.nav"), tabNav.getContainer());

        TabMarkers tabMarkers = new TabMarkers(this.gui);
        tabNav.getContainer().setBorder(border);
        tabbedPane.add(getWord("items.markers"), tabMarkers.getContainer());

        this.container.add(tabbedPane, BorderLayout.CENTER);
    }

    @Override
    public void updateUI() {
        this.container.setBorder(new MatteBorder(2, 2, 2, 2, GuiUtils.getBorderColor()));
    }
}
