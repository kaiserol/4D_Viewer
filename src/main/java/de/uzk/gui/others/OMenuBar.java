package de.uzk.gui.others;

import de.uzk.actions.ActionHandler;
import de.uzk.config.ConfigHandler;
import de.uzk.config.Language;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.IconUtils;
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
        this.tree.add(getOptMenu());

        for (OBarNode nodes : this.tree.getNodes()) {
            this.container.add(nodes.getComponent());
        }
    }

    private OBarMenu getEditMenu(ActionHandler actionHandler) {
        OBarMenu editTreeMenu = new OBarMenu(getWord("items.edit"), true);
        editTreeMenu.add(new OBarItem(getWord("items.edit.turnImageLeft"), IconUtils.TURN_LEFT_ICON, a -> actionHandler.executeEdit(ACTION_TURN_IMAGE_LEFT), ACTION_TURN_IMAGE_LEFT),
                new OBarItem(getWord("items.edit.turnImageRight"), IconUtils.TURN_RIGHT_ICON, a -> actionHandler.executeEdit(ACTION_TURN_IMAGE_RIGHT), ACTION_TURN_IMAGE_RIGHT),
                new OBarItem(getWord("items.edit.pinTime"), IconUtils.PIN_ICON, a -> actionHandler.executeEdit(ACTION_PIN_TIME), ACTION_PIN_TIME));
        editTreeMenu.addSeparator();
        editTreeMenu.add(new OBarItem(getWord("items.edit.screenshot"), IconUtils.SCREENSHOT_ICON, a -> actionHandler.executeEdit(ACTION_SCREENSHOT), ACTION_SCREENSHOT));
        return editTreeMenu;
    }

    private OBarMenu getNavMenu(ActionHandler actionHandler) {
        OBarMenu navTreeMenu = new OBarMenu(getWord("items.nav"), true);
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.first"), IconUtils.FIRST_IMAGE_ICON, actionHandler, ACTION_FIRST_IMAGE));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.prev"), IconUtils.PREV_IMAGE_ICON, actionHandler, ACTION_PREV_IMAGE));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.next"), IconUtils.NEXT_IMAGE_ICON, actionHandler, ACTION_NEXT_IMAGE));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.last"), IconUtils.LAST_IMAGE_ICON, actionHandler, ACTION_LAST_IMAGE));
        navTreeMenu.addSeparator();
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.first"), IconUtils.FIRST_LEVEL_ICON, actionHandler, ACTION_FIRST_LEVEL));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.prev"), IconUtils.PREV_LEVEL_ICON, actionHandler, ACTION_PREV_LEVEL));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.next"), IconUtils.NEXT_LEVEL_ICON, actionHandler, ACTION_NEXT_LEVEL));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.last"), IconUtils.LAST_LEVEL_ICON, actionHandler, ACTION_LAST_LEVEL));
        return navTreeMenu;
    }

    // TODO: opt in Window umbenennen!!!
    private OBarMenu getOptMenu() {
        OBarMenu optTreeMenu = new OBarMenu(getWord("items.opt"));

        // add language, theme
        optTreeMenu.add(new OBarItem(getWord("items.opt.changeLanguage"), a -> changeLanguage(), ACTION_CHANGE_LANGUAGE));
        optTreeMenu.add(new OBarItem(getWord("items.opt.toggleTheme"), a -> GuiUtils.switchThemes(gui), ACTION_TOGGLE_THEME));
        optTreeMenu.addSeparator();

        OBarItem incrFontItem = new OBarItem(getWord("items.opt.fontSizeIncr"));
        OBarItem decrFontItem = new OBarItem(getWord("items.opt.fontSizeDecr"));
        OBarItem restoreFontItem = new OBarItem(getWord("items.opt.fontSizeRestore"));

        // set Actions
        incrFontItem.setAction(updateFontItems(GuiUtils::incrFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_INCREASE_FONT, ACTION_INCREASE_FONT_2);
        decrFontItem.setAction(updateFontItems(GuiUtils::decrFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_DECREASE_FONT, ACTION_DECREASE_FONT_2);
        restoreFontItem.setAction(updateFontItems(GuiUtils::restoreFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_RESTORE_FONT);

        // add font
        optTreeMenu.add(incrFontItem, decrFontItem, restoreFontItem);
        updateFontItems(null,
                incrFontItem.getComponent(),
                decrFontItem.getComponent(),
                restoreFontItem.getComponent()).actionPerformed(null);
        optTreeMenu.addSeparator();

        // add info
        optTreeMenu.add(new OBarItem(getWord("items.opt.openLogWindow"), a -> new OInfo(gui)));

        return optTreeMenu;
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

    // TODO: BUG (Antwort Buttons bleiben immer auf deutsch oder englisch abhängig vom Locale), wenn man Locale aber ändert,
    // dann würde sich vermutlich auch die SYSTEM_LANGUAGE mitändern, was zu verhindern ist.
    private void changeLanguage() {
        Language oldLanguage = config.getLanguage();
        JComboBox<Language> selectBox = new JComboBox<>(Language.values());
        selectBox.setSelectedItem(oldLanguage);

        // show dialog
        int option = JOptionPane.showConfirmDialog(OMenuBar.this.gui.getFrame(),
                selectBox,
                getWord("items.opt.changeLanguage"),
                JOptionPane.OK_CANCEL_OPTION);
        Language language = (Language) selectBox.getSelectedItem();
        if (language == null || oldLanguage == language || option != JOptionPane.OK_OPTION) return;

        // show dialog
        option = JOptionPane.showConfirmDialog(OMenuBar.this.gui.getFrame(),
                getWord("items.opt.languageChanged.body"),
                getWord("items.opt.languageChanged.title"),
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