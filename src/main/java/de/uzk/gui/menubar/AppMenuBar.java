package de.uzk.gui.menubar;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.config.ConfigHandler;
import de.uzk.gui.AreaContainerInteractive;
import de.uzk.gui.Gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.Main.config;
import static de.uzk.action.ActionType.*;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.gui.Icons.*;

public class AppMenuBar extends AreaContainerInteractive<JMenuBar> {
    private final CustomMenuBar menuBar;
    private CustomMenuItem itemFontDecrease;
    private CustomMenuItem itemFontRestore;
    private CustomMenuItem itemFontIncrease;

    public AppMenuBar(Gui gui, ActionHandler actionHandler) {
        super(new JMenuBar(), gui);
        this.menuBar = new CustomMenuBar(this.container, "App MenuBar");
        this.menuBar.add(getMenuEdit(actionHandler));
        this.menuBar.add(getMenuNavigate(actionHandler));
        this.menuBar.add(getMenuWindow(actionHandler));
    }

    private CustomMenu getMenuEdit(ActionHandler actionHandler) {
        CustomMenu menuEdit = new CustomMenu(getWord("items.edit"));

        menuEdit.add(new CustomMenuItem(getWord("items.edit.pinTime"), ICON_PIN, actionHandler, SHORTCUT_TOGGLE_PIN_TIME));
        menuEdit.add(new CustomMenuItem(getWord("items.edit.turnImageLeft"), ICON_TURN_LEFT, actionHandler, SHORTCUT_TURN_IMAGE_90_LEFT));
        menuEdit.add(new CustomMenuItem(getWord("items.edit.turnImageRight"), ICON_TURN_RIGHT, actionHandler, SHORTCUT_TURN_IMAGE_90_RIGHT));
        menuEdit.addSeparator();

        menuEdit.add(new CustomMenuItem(getWord("items.edit.screenshot"), ICON_SCREENSHOT, actionHandler, SHORTCUT_TAKE_SCREENSHOT));
        return menuEdit;
    }

    private CustomMenu getMenuNavigate(ActionHandler actionHandler) {
        CustomMenu menuNavigate = new CustomMenu(getWord("items.nav"));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.image.first"), ICON_ARROW_LEFT_START, actionHandler, SHORTCUT_GO_TO_FIRST_IMAGE, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.image.prev"), ICON_ARROW_LEFT, actionHandler, SHORTCUT__GO_TO_PREV_IMAGE, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.image.next"), ICON_ARROW_RIGHT, actionHandler, SHORTCUT_GO_TO_NEXT_IMAGE, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.image.last"), ICON_ARROW_RIGHT_END, actionHandler, SHORTCUT_GO_TO_LAST_IMAGE, true));
        menuNavigate.addSeparator();
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.level.first"), ICON_ARROW_UP_START, actionHandler, SHORTCUT_GO_TO_FIRST_LEVEL, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.level.prev"), ICON_ARROW_UP, actionHandler, SHORTCUT_GO_TO_PREV_LEVEL, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.level.next"), ICON_ARROW_DOWN, actionHandler, SHORTCUT_GO_TO_NEXT_LEVEL, true));
        menuNavigate.add(new CustomMenuItem(getWord("items.nav.level.last"), ICON_ARROW_DOWN_END, actionHandler, SHORTCUT_GO_TO_LAST_LEVEL, true));
        return menuNavigate;
    }

    private CustomMenu getMenuWindow(ActionHandler actionHandler) {
        CustomMenu menuWindow = new CustomMenu(getWord("items.window"));

        // language, theme
        menuWindow.add(new CustomMenuItem(getWord("items.window.selectLanguage"), actionHandler, ACTION_SELECT_LANGUAGE));
        menuWindow.add(new CustomMenuItem(getWord("items.window.toggleTheme"), actionHandler, ACTION_TOGGLE_THEME));
        menuWindow.addSeparator();

        // font: decrease, restore, increase
        menuWindow.add(itemFontDecrease = new CustomMenuItem(getWord("items.window.fontSizeDecrease"), actionHandler, SHORTCUT_FONT_SIZE_DECREASE));
        menuWindow.add(itemFontRestore = new CustomMenuItem(getWord("items.window.fontSizeRestore"), actionHandler, SHORTCUT_FONT_SIZE_RESTORE));
        menuWindow.add(itemFontIncrease = new CustomMenuItem(getWord("items.window.fontSizeIncrease"), actionHandler, SHORTCUT_FONT_SIZE_INCREASE));
        menuWindow.addSeparator();
        updateFontItems();

        // disclaimer, logViewer
        menuWindow.add(new CustomMenuItem(getWord("items.window.showDisclaimer"), actionHandler, SHORTCUT_SHOW_DISCLAIMER));
        menuWindow.add(new CustomMenuItem(getWord("items.window.showLogViewer"), actionHandler, SHORTCUT_SHOW_LOG_VIEWER));

        return menuWindow;
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
        int fontSize = config.getFontSize();
        itemFontDecrease.getComponent().setEnabled(fontSize != ConfigHandler.MIN_FONT_SIZE);
        itemFontRestore.getComponent().setEnabled(fontSize != ConfigHandler.DEFAULT_FONT_SIZE);
        itemFontIncrease.getComponent().setEnabled(fontSize != ConfigHandler.MAX_FONT_SIZE);
        gui.updateUI();
    }

    private void toggleMenus(boolean enabled) {
        List<CustomMenuBarNode> nodes = new ArrayList<>();
        for (int i = 0; i < Math.min(2, menuBar.getMenus().size()); i++) {
            nodes.add(menuBar.getMenus().get(i));
        }

        while (!nodes.isEmpty()) {
            CustomMenuBarNode node = nodes.remove(0);
            if (node instanceof CustomMenu menu) nodes.addAll(0, menu.getNodes());
            else if (node instanceof CustomMenuItem item) item.getComponent().setEnabled(enabled);
        }
    }
}