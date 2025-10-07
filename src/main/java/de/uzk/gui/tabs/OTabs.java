package de.uzk.gui.tabs;

import de.uzk.gui.ActionType;
import de.uzk.gui.ActionTypeListener;
import de.uzk.gui.Gui;
import de.uzk.gui.InteractiveContainer;
import de.uzk.handler.ActionHandler;
import de.uzk.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;

import static de.uzk.Main.imageHandler;
import static de.uzk.utils.ActionUtils.*;
import static de.uzk.utils.IconUtils.*;
import static de.uzk.handler.LanguageHandler.getWord;

public class OTabs extends InteractiveContainer<JPanel> implements ActionTypeListener {
    private final ActionHandler actionHandler;
    private JToolBar toolBar;
    private JToggleButton pinTimeButton;

    public OTabs(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
        gui.addActionTypeListener(this);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout());
        this.container.setMinimumSize(new Dimension(0,0));

        // toolbar
        initToolBar();
        this.container.add(this.toolBar, BorderLayout.NORTH);

        // tabbedPane
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        OEdit edit = new OEdit(this.gui, this.actionHandler);
        tabbedPane.add(getWord("items.edit"), edit.getContainer());

        ONavigation navigation = new ONavigation(this.gui, this.actionHandler);
        tabbedPane.add(getWord("items.nav"), navigation.getContainer());

        this.container.add(tabbedPane, BorderLayout.CENTER);
    }

    private void initToolBar() {
        this.toolBar = new JToolBar();
        this.toolBar.setName(getWord("items.edit.toolbar"));

        // edit buttons
        this.pinTimeButton = new JToggleButton(PIN_ICON);
        this.initButton(pinTimeButton, getWord("items.edit.pinTime"), a -> {
            if (GuiUtils.isEnabled(pinTimeButton)) actionHandler.executeEdit(PIN_TIME_ACTION);
        });
        this.addJButton(TURN_LEFT_ICON, getWord("items.edit.turnImageLeft"), a -> actionHandler.executeEdit(TURN_IMAGE_LEFT_ACTION));
        this.addJButton(TURN_RIGHT_ICON, getWord("items.edit.turnImageRight"), a -> actionHandler.executeEdit(TURN_IMAGE_RIGHT_ACTION));
        this.toolBar.addSeparator(new Dimension(1, 20));

        // first, last in one dimension buttons
        this.addJButton(FIRST_IMAGE_ICON, getWord("items.nav.image.first"), a -> actionHandler.keyPressed(FIRST_IMAGE_ACTION));
        this.addJButton(LAST_IMAGE_ICON, getWord("items.nav.image.last"), a -> actionHandler.keyPressed(LAST_IMAGE_ACTION));

        this.addJButton(FIRST_LEVEL_ICON, getWord("items.nav.level.first"), a -> actionHandler.keyPressed(FIRST_LEVEL_ACTION));
        this.addJButton(LAST_LEVEL_ICON, getWord("items.nav.level.last"), a -> actionHandler.keyPressed(LAST_LEVEL_ACTION));
        this.toolBar.addSeparator(new Dimension(1, 20));

        // first, last in both dimensions buttons
        this.addJButton(FIRST_IMAGE_LEVEL_ICON, getWord("items.nav.both.first"), a -> actionHandler.keyPressed(FIRST_IMAGE_LEVEL_ACTION));
        this.addJButton(LAST_IMAGE_LEVEL_ICON, getWord("items.nav.both.last"), a -> actionHandler.keyPressed(LAST_IMAGE_LEVEL_ACTION));
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
