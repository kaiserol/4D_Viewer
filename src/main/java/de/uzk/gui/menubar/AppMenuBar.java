package de.uzk.gui.menubar;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.config.Settings;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.areas.AreaContainerInteractive;

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
        this.menuBar.add(getMenuProjects(actionHandler));
        this.menuBar.add(getMenuEdit(actionHandler));
        this.menuBar.add(getMenuNavigate(actionHandler));
        this.menuBar.add(getMenuWindow(actionHandler));
        this.menuBar.add(getMenuHelp(actionHandler));
    }

    private CustomMenu getMenuProjects(ActionHandler actionHandler) {
        CustomMenu menuProjects = new CustomMenu(getWord("menu.project"));

        menuProjects.add(new CustomMenuItem(getWord("menu.project.open"), null, actionHandler, SHORTCUT_OPEN_FOLDER));
        menuProjects.add(new CustomMenuItem(getWord("menu.project.openRecent"), null, actionHandler, SHORTCUT_OPEN_RECENT));
        menuProjects.add(new CustomMenuItem(getWord("menu.project.save"), null, actionHandler, SHORTCUT_SAVE_PROJECT));
        menuProjects.addSeparator();

        menuProjects.add(new CustomMenuItem(getWord("menu.project.close"), null, actionHandler, SHORTCUT_CLOSE_PROJECT));

        return menuProjects;
    }

    private CustomMenu getMenuEdit(ActionHandler actionHandler) {
        CustomMenu menuEdit = new CustomMenu(getWord("menu.edit"));

        menuEdit.add(new CustomMenuItem(getWord("menu.edit.pinTime"), ICON_PIN, actionHandler, SHORTCUT_PIN_TIME));
        menuEdit.add(new CustomMenuItem(getWord("menu.edit.turnImageLeft"), ICON_ARROW_LEFT_TURN, actionHandler, SHORTCUT_TURN_IMAGE_90_LEFT));
        menuEdit.add(new CustomMenuItem(getWord("menu.edit.turnImageRight"), ICON_ARROW_RIGHT_TURN, actionHandler, SHORTCUT_TURN_IMAGE_90_RIGHT));
        menuEdit.addSeparator();

        menuEdit.add(new CustomMenuItem(getWord("menu.edit.takeSnapshot"), actionHandler, SHORTCUT_TAKE_SNAPSHOT));
        return menuEdit;
    }

    private CustomMenu getMenuNavigate(ActionHandler actionHandler) {
        CustomMenu menuNavigate = new CustomMenu(getWord("menu.nav"));

        menuNavigate.add(new CustomMenuItem(getWord("menu.nav.time.first"), ICON_ARROW_LEFT_START, actionHandler, SHORTCUT_GO_TO_FIRST_IMAGE));
        menuNavigate.add(new CustomMenuItem(getWord("menu.nav.time.prev"), ICON_ARROW_LEFT, actionHandler, SHORTCUT_GO_TO_PREV_IMAGE));
        menuNavigate.add(new CustomMenuItem(getWord("menu.nav.time.next"), ICON_ARROW_RIGHT, actionHandler, SHORTCUT_GO_TO_NEXT_IMAGE));
        menuNavigate.add(new CustomMenuItem(getWord("menu.nav.time.last"), ICON_ARROW_RIGHT_END, actionHandler, SHORTCUT_GO_TO_LAST_IMAGE));
        menuNavigate.addSeparator();

        menuNavigate.add(new CustomMenuItem(getWord("menu.nav.level.first"), ICON_ARROW_UP_START, actionHandler, SHORTCUT_GO_TO_FIRST_LEVEL));
        menuNavigate.add(new CustomMenuItem(getWord("menu.nav.level.prev"), ICON_ARROW_UP, actionHandler, SHORTCUT_GO_TO_PREV_LEVEL));
        menuNavigate.add(new CustomMenuItem(getWord("menu.nav.level.next"), ICON_ARROW_DOWN, actionHandler, SHORTCUT_GO_TO_NEXT_LEVEL));
        menuNavigate.add(new CustomMenuItem(getWord("menu.nav.level.last"), ICON_ARROW_DOWN_END, actionHandler, SHORTCUT_GO_TO_LAST_LEVEL));
        return menuNavigate;
    }

    private CustomMenu getMenuWindow(ActionHandler actionHandler) {
        CustomMenu menuWindow = new CustomMenu(getWord("menu.window"));

        menuWindow.add(itemFontDecrease = new CustomMenuItem(getWord("menu.window.fontSizeDecrease"), actionHandler, SHORTCUT_FONT_SIZE_DECREASE));
        menuWindow.add(itemFontIncrease = new CustomMenuItem(getWord("menu.window.fontSizeIncrease"), actionHandler, SHORTCUT_FONT_SIZE_INCREASE));
        menuWindow.add(itemFontRestore = new CustomMenuItem(getWord("menu.window.fontSizeRestore"), actionHandler, SHORTCUT_FONT_SIZE_RESTORE));
        updateFontItems();

        Desktop desktop = GuiUtils.getDesktopSecurely();
        if (desktop == null || !desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            menuWindow.addSeparator();
            menuWindow.add(new CustomMenuItem(getWord("menu.window.openSettings"), actionHandler, SHORTCUT_OPEN_SETTINGS));
        }
        return menuWindow;
    }

    private CustomMenu getMenuHelp(ActionHandler actionHandler) {
        CustomMenu menuHelp = new CustomMenu(getWord("menu.help"));

        Desktop desktop = GuiUtils.getDesktopSecurely();
        if (desktop == null || !desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            String name = getWord("menu.help.showAbout") + " " + getWord("app.name");
            menuHelp.add(new CustomMenuItem(name, actionHandler, SHORTCUT_SHOW_ABOUT));
        }
        menuHelp.add(new CustomMenuItem(getWord("menu.help.showLegal"), actionHandler, SHORTCUT_SHOW_LEGAL));
        menuHelp.add(new CustomMenuItem(getWord("menu.help.showHistoryAndCredits"), actionHandler, SHORTCUT_SHOW_HISTORY));
        menuHelp.addSeparator();

        menuHelp.add(new CustomMenuItem(getWord("menu.help.showLogViewer"), actionHandler, SHORTCUT_SHOW_LOG_VIEWER));
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