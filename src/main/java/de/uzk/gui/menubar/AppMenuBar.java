package de.uzk.gui.menubar;

import de.uzk.actions.ActionHandler;
import de.uzk.config.ConfigHandler;
import de.uzk.config.Language;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.Icons;
import de.uzk.gui.InteractiveContainer;
import de.uzk.gui.viewer.OInfo;

import javax.swing.*;
import java.awt.event.ActionListener;

import static de.uzk.Main.config;
import static de.uzk.Main.logger;
import static de.uzk.actions.ActionUtils.*;
import static de.uzk.config.LanguageHandler.getWord;

public class AppMenuBar extends InteractiveContainer<JMenuBar> {
    private CustomMenuBar menuBar;

    public AppMenuBar(Gui gui, ActionHandler actionHandler) {
        super(new JMenuBar(), gui);
        init(actionHandler);
    }

    private void init(ActionHandler actionHandler) {
        this.menuBar = new CustomMenuBar(this.container);
        this.menuBar.add(getMenuEdit(actionHandler));
        this.menuBar.add(getMenuNavigate(actionHandler));
        this.menuBar.add(getMenuWindow());
        this.menuBar.add(getMenuDevTools());
    }

    private CustomMenu getMenuEdit(ActionHandler actionHandler) {
        CustomMenu menuEdit = new CustomMenu(getWord("items.edit"), true);

        // pin time, turn image left, right
        menuEdit.add(new CustomMenuItem(getWord("items.edit.pinTime"), Icons.ICON_PIN,
                a -> actionHandler.executeEdit(ACTION_PIN_TIME), ACTION_PIN_TIME));
        menuEdit.add(new CustomMenuItem(getWord("items.edit.turnImageLeft"), Icons.ICON_TURN_LEFT,
                a -> actionHandler.executeEdit(ACTION_TURN_IMAGE_LEFT), ACTION_TURN_IMAGE_LEFT));
        menuEdit.add(new CustomMenuItem(getWord("items.edit.turnImageRight"), Icons.ICON_TURN_RIGHT,
                a -> actionHandler.executeEdit(ACTION_TURN_IMAGE_RIGHT), ACTION_TURN_IMAGE_RIGHT));
        menuEdit.addSeparator();

        menuEdit.add(new CustomMenuItem(getWord("items.edit.screenshot"), Icons.ICON_SCREENSHOT, a -> actionHandler.executeEdit(ACTION_SCREENSHOT), ACTION_SCREENSHOT));
        return menuEdit;
    }

    private CustomMenu getMenuNavigate(ActionHandler actionHandler) {
        CustomMenu menuNavigate = new CustomMenu(getWord("items.nav"), true);
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.image.first"), Icons.ICON_FIRST_IMAGE, actionHandler, ACTION_FIRST_IMAGE));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.image.prev"), Icons.ICON_PREV_IMAGE, actionHandler, ACTION_PREV_IMAGE));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.image.next"), Icons.ICON_NEXT_IMAGE, actionHandler, ACTION_NEXT_IMAGE));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.image.last"), Icons.ICON_LAST_IMAGE, actionHandler, ACTION_LAST_IMAGE));
        menuNavigate.addSeparator();
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.level.first"), Icons.ICON_FIRST_LEVEL, actionHandler, ACTION_FIRST_LEVEL));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.level.prev"), Icons.ICON_PREV_LEVEL, actionHandler, ACTION_PREV_LEVEL));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.level.next"), Icons.ICON_NEXT_LEVEL, actionHandler, ACTION_NEXT_LEVEL));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.level.last"), Icons.ICON_LAST_LEVEL, actionHandler, ACTION_LAST_LEVEL));
        return menuNavigate;
    }

    private CustomMenu getMenuWindow() {
        CustomMenu menuWindow = new CustomMenu(getWord("items.window"));

        // add language, theme
        menuWindow.add(new CustomMenuItem(getWord("items.window.changeLanguage"), a -> changeLanguage(), ACTION_CHANGE_LANGUAGE));
        menuWindow.add(new CustomMenuItem(getWord("items.window.toggleTheme"), a -> GuiUtils.switchThemes(gui), ACTION_TOGGLE_THEME));
        menuWindow.addSeparator();

        // add font
        CustomMenuItem incrFontItem = new CustomMenuItem(getWord("items.window.fontSizeIncr"));
        CustomMenuItem decrFontItem = new CustomMenuItem(getWord("items.window.fontSizeDecr"));
        CustomMenuItem restoreFontItem = new CustomMenuItem(getWord("items.window.fontSizeRestore"));

        // set font Actions
        incrFontItem.setAction(updateFontItems(GuiUtils::incrFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_INCREASE_FONT, ACTION_INCREASE_FONT_2);
        decrFontItem.setAction(updateFontItems(GuiUtils::decrFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_DECREASE_FONT, ACTION_DECREASE_FONT_2);
        restoreFontItem.setAction(updateFontItems(GuiUtils::restoreFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), ACTION_RESTORE_FONT);

        menuWindow.add(incrFontItem, decrFontItem, restoreFontItem);
        updateFontItems(null,
                incrFontItem.getComponent(),
                decrFontItem.getComponent(),
                restoreFontItem.getComponent()).actionPerformed(null);

        return menuWindow;
    }

    private CustomMenu getMenuDevTools() {
        CustomMenu menuDevTools = new CustomMenu(getWord("items.dev-tools"));
        menuDevTools.add(new CustomMenuItem(getWord("items.dev-tools.openLogWindow"), a -> new OInfo(gui)));

        return menuDevTools;
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
        int option = JOptionPane.showConfirmDialog(AppMenuBar.this.gui.getFrame(),
                selectBox,
                getWord("items.window.changeLanguage"),
                JOptionPane.OK_CANCEL_OPTION);
        Language language = (Language) selectBox.getSelectedItem();
        if (language == null || oldLanguage == language || option != JOptionPane.OK_OPTION) return;

        // show dialog
        option = JOptionPane.showConfirmDialog(AppMenuBar.this.gui.getFrame(),
                getWord("items.window.languageChanged.body"),
                getWord("items.window.languageChanged.title"),
                JOptionPane.YES_NO_OPTION);

        // set language and save config
        logger.info("Changing language to '" + language + "'");
        config.setLanguage(language);
        config.saveConfig();

        // TODO: Warum wird beim bauen showLoadedImages() 2 mal hintereinander (siehe Logs) aufgerufen?
        if (option == JOptionPane.YES_OPTION) {
            gui.rebuild();
        }
    }

    @Override
    public void toggleOn() {
        enableMenus(this.menuBar, true);
    }

    @Override
    public void toggleOff() {
        enableMenus(this.menuBar, false);
    }

    private void enableMenus(CustomMenuNode parent, boolean enabled) {
        for (CustomMenuNode node : parent.getNodes()) {
            if (parent.isToggleable()) {
                node.getComponent().setEnabled(enabled);
            }
            enableMenus(node, enabled);
        }
    }
}