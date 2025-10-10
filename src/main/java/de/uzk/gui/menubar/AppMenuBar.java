package de.uzk.gui.menubar;

import de.uzk.actions.ActionHandler;
import de.uzk.config.ConfigHandler;
import de.uzk.config.Language;
import de.uzk.gui.*;
import de.uzk.gui.DialogLogViewer;
import de.uzk.gui.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionListener;

import static de.uzk.Main.config;
import static de.uzk.Main.logger;
import static de.uzk.actions.Actions.*;
import static de.uzk.config.LanguageHandler.getWord;

public class AppMenuBar extends AreaContainerInteractive<JMenuBar> {
    private CustomMenuBar menuBar;
    private final DialogDisclaimer dialogDisclaimer;
    private final DialogLogViewer dialogLogViewer;

    public AppMenuBar(Gui gui, ActionHandler actionHandler) {
        super(new JMenuBar(), gui);
        this.dialogDisclaimer = new DialogDisclaimer(gui.getFrame());
        this.dialogLogViewer = new DialogLogViewer(gui.getFrame());
        init(actionHandler);
    }

    private void init(ActionHandler actionHandler) {
        this.menuBar = new CustomMenuBar(this.container);
        this.menuBar.add(getMenuEdit(actionHandler));
        this.menuBar.add(getMenuNavigate(actionHandler));
        this.menuBar.add(getMenuWindow());
    }

    private CustomMenu getMenuEdit(ActionHandler actionHandler) {
        CustomMenu menuEdit = new CustomMenu(getWord("items.edit"), true);

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

        // language, theme
        menuWindow.add(new CustomMenuItem(getWord("items.window.selectLanguage"), a -> selectLanguage()));
        menuWindow.add(new CustomMenuItem(getWord("items.window.toggleTheme"), a -> GuiUtils.switchThemes(gui)));
        menuWindow.addSeparator();

        // font: decrease, restore, increase
        CustomMenuItem decrFontItem = new CustomMenuItem(getWord("items.window.fontSizeDecr"));
        CustomMenuItem restoreFontItem = new CustomMenuItem(getWord("items.window.fontSizeRestore"));
        CustomMenuItem incrFontItem = new CustomMenuItem(getWord("items.window.fontSizeIncr"));

        decrFontItem.setAction(updateFontItems(GuiUtils::decrFont,
                decrFontItem.getComponent(), restoreFontItem.getComponent(), incrFontItem.getComponent()), ACTION_DECREASE_FONT, ACTION_DECREASE_FONT_2);
        restoreFontItem.setAction(updateFontItems(GuiUtils::restoreFont,
                decrFontItem.getComponent(), restoreFontItem.getComponent(), incrFontItem.getComponent()), ACTION_RESTORE_FONT);
        incrFontItem.setAction(updateFontItems(GuiUtils::incrFont,
                decrFontItem.getComponent(), restoreFontItem.getComponent(), incrFontItem.getComponent()), ACTION_INCREASE_FONT, ACTION_INCREASE_FONT_2);

        menuWindow.add(decrFontItem, restoreFontItem, incrFontItem);
        updateFontItems(null,
                decrFontItem.getComponent(), restoreFontItem.getComponent(), incrFontItem.getComponent()).actionPerformed(null);
        menuWindow.addSeparator();

        // disclaimer, logViewer
        menuWindow.add(new CustomMenuItem(getWord("items.window.showDisclaimer"), a -> dialogDisclaimer.show(), ACTION_SHOW_DISCLAIMER));
        menuWindow.add(new CustomMenuItem(getWord("items.window.showLogViewer"), a -> dialogLogViewer.show(), ACTION_SHOW_LOG_VIEWER));

        return menuWindow;
    }

    private ActionListener updateFontItems(Runnable runnable, JComponent decrFontItem, JComponent restoreFontItem, JComponent incrFontItem) {
        return a -> {
            if (runnable != null) runnable.run();
            int fontSize = config.getFontSize();
            decrFontItem.setEnabled(fontSize != ConfigHandler.MIN_FONT_SIZE);
            restoreFontItem.setEnabled(fontSize != ConfigHandler.DEFAULT_FONT_SIZE);
            incrFontItem.setEnabled(fontSize != ConfigHandler.MAX_FONT_SIZE);
        };
    }

    private void selectLanguage() {
        Language oldLanguage = config.getLanguage();
        JComboBox<Language> selectBox = new JComboBox<>(Language.values());
        selectBox.setSelectedItem(oldLanguage);

        // Benutzerdefinierte Buttons
        JButton okButton = new JButton(getWord("optionPane.button.ok"));
        JButton cancelButton = new JButton(getWord("optionPane.button.cancel"));
        okButton.setEnabled(false);

        // Wenn sich die Auswahl ändert → Button aktivieren/deaktivieren
        selectBox.addActionListener(a -> {
            Language selected = (Language) selectBox.getSelectedItem();
            okButton.setEnabled(selected != null && selected != oldLanguage);
        });

        // Inhalte & Optionen des Dialogs
        Object[] message = {selectBox};
        Object[] options = {okButton, cancelButton};

        // JOptionPane erstellen
        JOptionPane pane = new JOptionPane(
                message, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, options, okButton
        );
        JDialog dialog = pane.createDialog(gui.getFrame(), getWord("items.window.selectLanguage"));

        // Aktionen der Buttons
        okButton.addActionListener(a -> {
            pane.setValue(okButton);
            dialog.dispose();
        });
        cancelButton.addActionListener(a -> {
            pane.setValue(cancelButton);
            dialog.dispose();
        });

        // Dialog anzeigen
        dialog.setVisible(true);

        // Ergebnis auswerten
        Object selectedValue = pane.getValue();
        Language newLanguage = (Language) selectBox.getSelectedItem();
        if (selectedValue != okButton || newLanguage == null || oldLanguage == newLanguage) return;

        // Sprache setzen und speichern
        logger.info("Changing language from '" + oldLanguage + "' to '" + newLanguage + "'");
        config.setLanguage(newLanguage);
        config.saveConfig();
        gui.rebuild();
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