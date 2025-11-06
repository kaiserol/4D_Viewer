package de.uzk.gui.menubar;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.config.Settings;
import de.uzk.gui.areas.AreaContainerInteractive;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.settings;
import static de.uzk.action.ActionType.*;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.gui.Icons.*;

public class AppMenuBar extends AreaContainerInteractive<JMenuBar> {
    // MenuBar Elemente
    private final CustomMenuBar menuBar;
    private CustomMenuItem itemFontDecrease;
    private CustomMenuItem itemFontIncrease;
    private CustomMenuItem itemFontRestore;

    public AppMenuBar(Gui gui) {
        super(new JMenuBar(), gui);
        this.menuBar = new CustomMenuBar(this.container, "MenuBar");
        init(gui.getActionHandler());
    }

    private void init(ActionHandler actionHandler) {
        this.menuBar.add(getMenuEdit(actionHandler));
        this.menuBar.add(getMenuNavigate(actionHandler));
        this.menuBar.add(getMenuWindow(actionHandler));
        this.menuBar.add(getMenuHelp(actionHandler));
    }

    private CustomMenu getMenuEdit(ActionHandler actionHandler) {
        CustomMenu menuEdit = new CustomMenu(getWord("items.edit"));

        menuEdit.add(new CustomMenuItem(getWord("items.edit.pinTime"), ICON_PIN, actionHandler, SHORTCUT_TOGGLE_PIN_TIME));
        menuEdit.add(new CustomMenuItem(getWord("items.edit.turnImageLeft"), ICON_ARROW_LEFT_TURN, actionHandler, SHORTCUT_TURN_IMAGE_90_LEFT));
        menuEdit.add(new CustomMenuItem(getWord("items.edit.turnImageRight"), ICON_ARROW_RIGHT_TURN, actionHandler, SHORTCUT_TURN_IMAGE_90_RIGHT));
        menuEdit.addSeparator();

        menuEdit.add(new CustomMenuItem(getWord("items.edit.takeScreenshot"), actionHandler, SHORTCUT_TAKE_SCREENSHOT));
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
        menuWindow.add(itemFontDecrease = new CustomMenuItem(getWord("items.window.fontSizeDecrease"), actionHandler, SHORTCUT_FONT_SIZE_DECREASE));
        menuWindow.add(itemFontIncrease = new CustomMenuItem(getWord("items.window.fontSizeIncrease"), actionHandler, SHORTCUT_FONT_SIZE_INCREASE));
        menuWindow.add(itemFontRestore = new CustomMenuItem(getWord("items.window.fontSizeRestore"), actionHandler, SHORTCUT_FONT_SIZE_RESTORE));
        updateFontItems();

        Desktop desktop = GuiUtils.getDesktopSecure();
        if (!(desktop != null && desktop.isSupported(Desktop.Action.APP_PREFERENCES))) {
            menuWindow.addSeparator();
            menuWindow.add(new CustomMenuItem(getWord("items.window.openSettings"), actionHandler, SHORTCUT_OPEN_SETTINGS));
        }
        return menuWindow;
    }

    private CustomMenu getMenuHelp(ActionHandler actionHandler) {
        CustomMenu menuHelp = new CustomMenu(getWord("items.help"));
        menuHelp.add(new CustomMenuItem(getWord("items.help.showDisclaimer"), actionHandler, SHORTCUT_SHOW_DISCLAIMER));
        menuHelp.add(new CustomMenuItem(getWord("items.help.showVersions"), actionHandler, SHORTCUT_SHOW_VERSIONS));
        menuHelp.add(new CustomMenuItem(getWord("items.help.showLogViewer"), actionHandler, SHORTCUT_SHOW_LOG_VIEWER));
        return menuHelp;
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ACTION_UPDATE_FONT) updateFontItems();
    }

    private void updateFontItems() {
        int fontSize = settings.getFontSize();
        itemFontDecrease.getComponent().setEnabled(fontSize != Settings.MIN_FONT_SIZE);
        itemFontIncrease.getComponent().setEnabled(fontSize != Settings.MAX_FONT_SIZE);
        itemFontRestore.getComponent().setEnabled(fontSize != Settings.DEFAULT_FONT_SIZE);
    }
}