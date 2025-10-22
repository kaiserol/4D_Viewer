package de.uzk.gui.menubar;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.config.ConfigHandler;
import de.uzk.gui.AreaContainerInteractive;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;

import javax.swing.*;

import static de.uzk.Main.configHandler;
import static de.uzk.action.ActionType.*;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.gui.Icons.*;

public class AppMenuBar extends AreaContainerInteractive<JMenuBar> {
    private final CustomMenuBar menuBar;
    private CustomMenuItem itemFontDecrease;
    private CustomMenuItem itemFontIncrease;
    private CustomMenuItem itemFontRestore;

    public AppMenuBar(Gui gui, ActionHandler actionHandler) {
        super(new JMenuBar(), gui);
        this.menuBar = new CustomMenuBar(this.container, "App MenuBar");
        this.menuBar.add(getMenuEdit(actionHandler));
        this.menuBar.add(getMenuNavigate(actionHandler));
        this.menuBar.add(getMenuWindow(actionHandler));
        this.menuBar.add(getMenuSettings(actionHandler));
    }

    private CustomMenu getMenuEdit(ActionHandler actionHandler) {
        CustomMenu menuEdit = new CustomMenu(getWord("items.edit"));

        // pinTime, turnImageLeft, turnImageRight
        menuEdit.add(new CustomMenuItem(getWord("items.edit.pinTime"), ICON_PIN, actionHandler, SHORTCUT_TOGGLE_PIN_TIME));
        menuEdit.add(new CustomMenuItem(getWord("items.edit.turnImageLeft"), ICON_TURN_LEFT, actionHandler, SHORTCUT_TURN_IMAGE_90_LEFT));
        menuEdit.add(new CustomMenuItem(getWord("items.edit.turnImageRight"), ICON_TURN_RIGHT, actionHandler, SHORTCUT_TURN_IMAGE_90_RIGHT));
        menuEdit.addSeparator();

        // takeScreenshot
        menuEdit.add(new CustomMenuItem(getWord("items.edit.takeScreenshot"), ICON_SCREENSHOT, actionHandler, SHORTCUT_TAKE_SCREENSHOT));
        return menuEdit;
    }

    private CustomMenu getMenuNavigate(ActionHandler actionHandler) {
        CustomMenu menuNavigate = new CustomMenu(getWord("items.nav"));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.axis.time.first"), ICON_ARROW_LEFT_START, actionHandler, SHORTCUT_GO_TO_FIRST_IMAGE, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.axis.time.prev"), ICON_ARROW_LEFT, actionHandler, SHORTCUT_GO_TO_PREV_IMAGE, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.axis.time.next"), ICON_ARROW_RIGHT, actionHandler, SHORTCUT_GO_TO_NEXT_IMAGE, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.axis.time.last"), ICON_ARROW_RIGHT_END, actionHandler, SHORTCUT_GO_TO_LAST_IMAGE, true));
        menuNavigate.addSeparator();
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.axis.level.first"), ICON_ARROW_UP_START, actionHandler, SHORTCUT_GO_TO_FIRST_LEVEL, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.axis.level.prev"), ICON_ARROW_UP, actionHandler, SHORTCUT_GO_TO_PREV_LEVEL, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.axis.level.next"), ICON_ARROW_DOWN, actionHandler, SHORTCUT_GO_TO_NEXT_LEVEL, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.axis.level.last"), ICON_ARROW_DOWN_END, actionHandler, SHORTCUT_GO_TO_LAST_LEVEL, true));
        return menuNavigate;
    }

    private CustomMenu getMenuWindow(ActionHandler actionHandler) {
        CustomMenu menuWindow = new CustomMenu(getWord("items.window"));

        // font: decrease, restore, increase
        menuWindow.add(itemFontDecrease = new CustomMenuItem(getWord("items.window.fontSizeDecrease"), actionHandler, SHORTCUT_FONT_SIZE_DECREASE));
        menuWindow.add(itemFontIncrease = new CustomMenuItem(getWord("items.window.fontSizeIncrease"), actionHandler, SHORTCUT_FONT_SIZE_INCREASE));
        menuWindow.add(itemFontRestore = new CustomMenuItem(getWord("items.window.fontSizeRestore"), actionHandler, SHORTCUT_FONT_SIZE_RESTORE));
        menuWindow.addSeparator();
        updateFontItems();

        // disclaimer, logViewer
        menuWindow.add(new CustomMenuItem(getWord("items.window.showDisclaimer"), actionHandler, SHORTCUT_SHOW_DISCLAIMER));
        menuWindow.add(new CustomMenuItem(getWord("items.window.showLogViewer"), actionHandler, SHORTCUT_SHOW_LOG_VIEWER));
        return menuWindow;
    }

    private CustomMenu getMenuSettings(ActionHandler actionHandler) {
        CustomMenu menuSettings = new CustomMenu(getWord("items.settings"));

        // language, theme, settings
        menuSettings.add(new CustomMenuItem(getWord("items.settings.selectLanguage"), actionHandler, SHORTCUT_SELECT_LANGUAGE));
        menuSettings.add(new CustomMenuItem(getWord("items.settings.toggleTheme"), actionHandler, SHORTCUT_TOGGLE_THEME));
        menuSettings.add(new CustomMenuItem(getWord("items.settings.openSettings"), actionHandler, SHORTCUT_OPEN_SETTINGS));
        return menuSettings;
    }

    @Override
    public void toggleOn() {
        toggleMenus(true);
    }

    @Override
    public void toggleOff() {
        toggleMenus(false);
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ACTION_UPDATE_FONT) updateFontItems();
    }

    private void updateFontItems() {
        int fontSize = configHandler.getFontSize();
        itemFontDecrease.getComponent().setEnabled(fontSize != ConfigHandler.MIN_FONT_SIZE);
        itemFontIncrease.getComponent().setEnabled(fontSize != ConfigHandler.MAX_FONT_SIZE);
        itemFontRestore.getComponent().setEnabled(fontSize != ConfigHandler.DEFAULT_FONT_SIZE);
        gui.updateUI();
    }

    private void toggleMenus(boolean enabled) {
        for (int i = 0; i < Math.min(2, menuBar.getMenus().size()); i++) {
            JComponent component = menuBar.getMenus().get(i).getComponent();
            GuiUtils.setEnabled(component, enabled);
        }
    }
}