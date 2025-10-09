package de.uzk.gui.tabs;

import de.uzk.actions.ActionHandler;
import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.InteractiveContainer;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;

import static de.uzk.Main.imageHandler;
import static de.uzk.actions.ActionUtils.*;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.gui.Icons.*;

public class AreaTabs extends InteractiveContainer<JPanel> implements ActionTypeListener {

    private final ActionHandler actionHandler;
    private JToolBar toolBar;
    private JToggleButton pinTimeButton;

    public AreaTabs(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
        gui.addActionTypeListener(this);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout());
        this.container.setMinimumSize(new Dimension(0, 0));

        // toolbar
        initToolBar();
        this.container.add(this.toolBar, BorderLayout.NORTH);

        // tabbedPane
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        TabEdit tabEdit = new TabEdit(this.gui, this.actionHandler);
        tabbedPane.add(getWord("items.edit"), tabEdit.getContainer());

        TabNav tabNav = new TabNav(this.gui, this.actionHandler);
        tabbedPane.add(getWord("items.nav"), tabNav.getContainer());

        TabMarkers tabMarkers = new TabMarkers(this.gui);
        tabbedPane.add("Markers", tabMarkers.getContainer());

        this.container.add(tabbedPane, BorderLayout.CENTER);
    }

    private void initToolBar() {
        this.toolBar = new JToolBar();
        this.toolBar.setName(getWord("items.edit.toolbar"));

        // edit buttons
        this.pinTimeButton = new JToggleButton(ICON_PIN);
        this.initButton(pinTimeButton, getWord("items.edit.pinTime"), a -> {
            if (GuiUtils.isEnabled(pinTimeButton)) actionHandler.executeEdit(ACTION_PIN_TIME);
        });
        this.addJButton(ICON_TURN_LEFT, getWord("items.edit.turnImageLeft"), a -> actionHandler.executeEdit(ACTION_TURN_IMAGE_LEFT));
        this.addJButton(ICON_TURN_RIGHT, getWord("items.edit.turnImageRight"), a -> actionHandler.executeEdit(ACTION_TURN_IMAGE_RIGHT));
        this.toolBar.addSeparator(new Dimension(1, 20));

        // first, last in one-dimension buttons
        this.addJButton(ICON_FIRST_IMAGE, getWord("items.nav.image.first"), a -> actionHandler.keyPressed(ACTION_FIRST_IMAGE));
        this.addJButton(ICON_LAST_IMAGE, getWord("items.nav.image.last"), a -> actionHandler.keyPressed(ACTION_LAST_IMAGE));

        this.addJButton(ICON_FIRST_LEVEL, getWord("items.nav.level.first"), a -> actionHandler.keyPressed(ACTION_FIRST_LEVEL));
        this.addJButton(ICON_LAST_LEVEL, getWord("items.nav.level.last"), a -> actionHandler.keyPressed(ACTION_LAST_LEVEL));
    }

    private void addJButton(Icon icon, String toolTipText, ActionListener action) {
        initButton(new JButton(icon), toolTipText, action);
    }

    private void initButton(AbstractButton button, String toolTipText, ActionListener action) {
        button.setToolTipText(toolTipText);
        button.addActionListener(action);
        this.toolBar.add(button);
    }

    @Override
    public void toggleOn() {
        // toolbar
        for (Component c : toolBar.getComponents()) c.setEnabled(true);
    }

    @Override
    public void toggleOff() {
        // toolbar
        for (Component c : toolBar.getComponents()) c.setEnabled(false);
    }

    @Override
    public void updateUI() {
        this.container.setBorder(new MatteBorder(2, 2, 2, 2, GuiUtils.getBorderColor()));
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.UPDATE_PIN_TIME) {
            updateSecretly(pinTimeButton, imageHandler.hasPinTime());
        }
    }

    private void updateSecretly(JToggleButton button, boolean selected) {
        GuiUtils.updateSecretly(button, () -> button.setSelected(selected));
    }
}
