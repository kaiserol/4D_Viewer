package de.uzk.gui.others;

import de.uzk.actions.ActionHandler;
import de.uzk.config.ConfigHandler;
import de.uzk.config.Language;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.Icons;
import de.uzk.gui.InteractiveContainer;
import de.uzk.gui.tree.OBar;
import de.uzk.gui.tree.OBarItem;
import de.uzk.gui.tree.OBarMenu;
import de.uzk.gui.tree.OBarNode;
import de.uzk.gui.viewer.OInfo;

import javax.swing.*;
import java.awt.event.ActionListener;

import static de.uzk.Main.config;
import static de.uzk.Main.logger;
import static de.uzk.actions.ActionUtils.*;
import static de.uzk.config.LanguageHandler.getWord;

public class OMenuBar extends InteractiveContainer<JMenuBar> {
    private OBar tree;

    public OMenuBar(Gui gui, ActionHandler actionHandler) {
        super(new JMenuBar(), gui);
        init(actionHandler);
    }

    private void init(ActionHandler actionHandler) {
        this.tree = new OBar(this.container);
        this.tree.add(getEditMenu(actionHandler));
        this.tree.add(getNavMenu(actionHandler));
        this.tree.add(getWindowMenu());
        this.tree.add(getDevToolsMenu());

        for (OBarNode nodes : this.tree.getNodes()) {
            this.container.add(nodes.getComponent());
        }
    }

    private OBarMenu getEditMenu(ActionHandler actionHandler) {
        OBarMenu editTreeMenu = new OBarMenu(getWord("items.edit"), true);

        // pin time, turn image left, right
        editTreeMenu.add(new OBarItem(getWord("items.edit.pinTime"), Icons.ICON_PIN,
                a -> actionHandler.executeEdit(ACTION_PIN_TIME), ACTION_PIN_TIME));
        editTreeMenu.add(new OBarItem(getWord("items.edit.turnImageLeft"), Icons.ICON_TURN_LEF,
                a -> actionHandler.executeEdit(ACTION_TURN_IMAGE_LEFT), ACTION_TURN_IMAGE_LEFT));
        editTreeMenu.add(new OBarItem(getWord("items.edit.turnImageRight"), Icons.ICON_TURN_RIGHT,
                a -> actionHandler.executeEdit(ACTION_TURN_IMAGE_RIGHT), ACTION_TURN_IMAGE_RIGHT));
        editTreeMenu.addSeparator();

        editTreeMenu.add(new OBarItem(getWord("items.edit.screenshot"), Icons.ICON_SCREENSHOT, a -> actionHandler.executeEdit(ACTION_SCREENSHOT), ACTION_SCREENSHOT));
        return editTreeMenu;
    }

    private OBarMenu getNavMenu(ActionHandler actionHandler) {
        OBarMenu navTreeMenu = new OBarMenu(getWord("items.nav"), true);
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.first"), Icons.ICON_FIRST_IMAGE, actionHandler, ACTION_FIRST_IMAGE));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.prev"), Icons.ICON_PREV_IMAGE, actionHandler, ACTION_PREV_IMAGE));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.next"), Icons.ICON_NEXT_IMAGE, actionHandler, ACTION_NEXT_IMAGE));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.last"), Icons.ICON_LAST_IMAGE, actionHandler, ACTION_LAST_IMAGE));
        navTreeMenu.addSeparator();
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.first"), Icons.ICON_FIRST_LEVEL, actionHandler, ACTION_FIRST_LEVEL));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.prev"), Icons.ICON_PREV_LEVEL, actionHandler, ACTION_PREV_LEVEL));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.next"), Icons.ICON_NEXT_LEVEL, actionHandler, ACTION_NEXT_LEVEL));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.last"), Icons.ICON_LAST_LEVEL, actionHandler, ACTION_LAST_LEVEL));
        return navTreeMenu;
    }

    private OBarMenu getWindowMenu() {
        OBarMenu windowTreeMenu = new OBarMenu(getWord("items.window"));

        // add language, theme
        windowTreeMenu.add(new OBarItem(getWord("items.window.changeLanguage"), a -> changeLanguage(), ACTION_CHANGE_LANGUAGE));
        windowTreeMenu.add(new OBarItem(getWord("items.window.toggleTheme"), a -> GuiUtils.switchThemes(gui), ACTION_TOGGLE_THEME));
        windowTreeMenu.addSeparator();

        // add font
        OBarItem incrFontItem = new OBarItem(getWord("items.window.fontSizeIncr"));
        OBarItem decrFontItem = new OBarItem(getWord("items.window.fontSizeDecr"));
        OBarItem restoreFontItem = new OBarItem(getWord("items.window.fontSizeRestore"));

        // set font Actions
        incrFontItem.setAction(updateFontItems(GuiUtils::incrFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_INCREASE_FONT, ACTION_INCREASE_FONT_2);
        decrFontItem.setAction(updateFontItems(GuiUtils::decrFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_DECREASE_FONT, ACTION_DECREASE_FONT_2);
        restoreFontItem.setAction(updateFontItems(GuiUtils::restoreFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_RESTORE_FONT);

        windowTreeMenu.add(incrFontItem, decrFontItem, restoreFontItem);
        updateFontItems(null,
                incrFontItem.getComponent(),
                decrFontItem.getComponent(),
                restoreFontItem.getComponent()).actionPerformed(null);

        return windowTreeMenu;
    }

    private OBarMenu getDevToolsMenu() {
        OBarMenu devToolsMenu = new OBarMenu(getWord("items.dev-tools"));
        devToolsMenu.add(new OBarItem(getWord("items.dev-tools.openLogWindow"), a -> new OInfo(gui)));

        return devToolsMenu;
    }

    private ActionListener updateFontItems(Runnable runnable, JComponent incrFontItem, JComponent decrFontItem, JComponent restoreFontItem) {
        return a -> {
            if (runnable != null) runnable.run();
            int fontSize = config.getFontSize();
            incrFontItem.setEnabled(fontSize != ConfigHandler.MAX_FONT_SIZE);
            decrFontItem.setEnabled(fontSize != ConfigHandler.MIN_FONT_SIZE);
            restoreFontItem.setEnabled(fontSize != ConfigHandler.DEFAULT_FONT_SIZE);
        };
    }

    private void changeLanguage() {
        Language oldLanguage = config.getLanguage();
        JComboBox<Language> selectBox = new JComboBox<>(Language.values());
        selectBox.setSelectedItem(oldLanguage);

        // show dialog
        int option = JOptionPane.showConfirmDialog(OMenuBar.this.gui.getFrame(),
                selectBox,
                getWord("items.window.changeLanguage"),
                JOptionPane.OK_CANCEL_OPTION);
        Language language = (Language) selectBox.getSelectedItem();
        if (language == null || oldLanguage == language || option != JOptionPane.OK_OPTION) return;

        // show dialog
        option = JOptionPane.showConfirmDialog(OMenuBar.this.gui.getFrame(),
                getWord("items.window.languageChanged.body"),
                getWord("items.window.languageChanged.title"),
                JOptionPane.YES_NO_OPTION);

        // set language and save config
        config.setLanguage(language);
        config.saveConfig();
        if (option == JOptionPane.YES_OPTION) {
            logger.info("Changing language to '" + language + "'");

            // TODO: Warum wird beim bauen showLoadedImages() 2 mal hintereinander (siehe Logs) aufgerufen?
            gui.rebuild();
        }
    }

    @Override
    public void toggleOn() {
        enableMenus(this.tree, true);
    }

    @Override
    public void toggleOff() {
        enableMenus(this.tree, false);
    }

    private void enableMenus(OBarNode parent, boolean enabled) {
        for (OBarNode node : parent.getNodes()) {
            if (parent.isToggleable()) {
                node.getComponent().setEnabled(enabled);
            }
            enableMenus(node, enabled);
        }
    }
}