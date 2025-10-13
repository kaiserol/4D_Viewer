package de.uzk.action;

import de.uzk.config.Language;
import de.uzk.gui.DialogDisclaimer;
import de.uzk.gui.DialogLogViewer;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.image.ImageLayer;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static de.uzk.Main.*;
import static de.uzk.Main.config;
import static de.uzk.action.ActionType.*;
import static de.uzk.config.LanguageHandler.getWord;

public class ActionHandler extends KeyAdapter implements MouseWheelListener {
    // delays in ms
    private static final long HIGH_LOAD_DELAY = 75;
    private static final long LOAD_DELAY = 50;

    private final Gui gui;
    private final DialogDisclaimer dialogDisclaimer;
    private final DialogLogViewer dialogLogViewer;
    private long lastImageChangedTime = 0;

    public ActionHandler(Gui gui) {
        this.gui = gui;
        this.dialogDisclaimer = new DialogDisclaimer(gui.getFrame());
        this.dialogLogViewer = new DialogLogViewer(gui.getFrame());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        executeAction(ActionType.getAction(e));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        executeAction(ActionType.getAction(e));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseWheelMoved(e.isShiftDown(), e.getWheelRotation());
    }

    public void mouseWheelMoved(boolean shift, int rotation) {
        ImageLayer layer = shift ? ImageLayer.TIME : ImageLayer.LEVEL;
        scrollToNextImage(layer, rotation, false);
    }

    public void executeAction(ActionType actionType) {
        if (actionType == null) return; // Sollte eigentlich nie eintreten
        switch (actionType) {
            // edit actions
            case SHORTCUT_TOGGLE_PIN_TIME -> gui.handleAction(SHORTCUT_TOGGLE_PIN_TIME);
            case SHORTCUT_TURN_IMAGE_90_LEFT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_LEFT);
            case SHORTCUT_TURN_IMAGE_90_RIGHT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_RIGHT);
            case SHORTCUT_TAKE_SCREENSHOT -> gui.handleAction(SHORTCUT_TAKE_SCREENSHOT);

            // nav actions
            case SHORTCUT_GO_TO_FIRST_IMAGE -> scrollToNextImage(ImageLayer.TIME, -1, true);
            case SHORTCUT__GO_TO_PREV_IMAGE -> scrollToNextImage(ImageLayer.TIME, -1, false);
            case SHORTCUT_GO_TO_NEXT_IMAGE -> scrollToNextImage(ImageLayer.TIME, 1, false);
            case SHORTCUT_GO_TO_LAST_IMAGE -> scrollToNextImage(ImageLayer.TIME, 1, true);

            case SHORTCUT_GO_TO_FIRST_LEVEL -> scrollToNextImage(ImageLayer.LEVEL, -1, true);
            case SHORTCUT_GO_TO_PREV_LEVEL -> scrollToNextImage(ImageLayer.LEVEL, -1, false);
            case SHORTCUT_GO_TO_NEXT_LEVEL -> scrollToNextImage(ImageLayer.LEVEL, 1, false);
            case SHORTCUT_GO_TO_LAST_LEVEL -> scrollToNextImage(ImageLayer.LEVEL, 1, true);

            // window actions
            case ACTION_SELECT_LANGUAGE -> selectLanguage();
            case ACTION_TOGGLE_THEME -> GuiUtils.toggleTheme(gui);

            case SHORTCUT_FONT_SIZE_DECREASE -> GuiUtils.decreaseFont();
            case SHORTCUT_FONT_SIZE_RESTORE -> GuiUtils.restoreFont();
            case SHORTCUT_FONT_SIZE_INCREASE -> GuiUtils.increaseFont();

            case SHORTCUT_SHOW_DISCLAIMER -> dialogDisclaimer.show();
            case SHORTCUT_SHOW_LOG_VIEWER -> dialogLogViewer.show();
            default -> {
            }
        }
    }

    private void scrollToNextImage(ImageLayer layer, int rotation, boolean goToFirstOrLast) {
        if (rotation == 0 || !allowNextImageChange()) return;

        if (rotation < 0) {
            if (goToFirstOrLast) imageHandler.toFirst(layer);
            else imageHandler.prev(layer);
        } else {
            if (goToFirstOrLast) imageHandler.toLast(layer);
            else imageHandler.next(layer);
        }
        gui.update(layer);
    }

    private boolean allowNextImageChange() {
        long delay = imageHandler.getImageDetails().getRotation() != 0 ? HIGH_LOAD_DELAY : LOAD_DELAY;
        long now = System.currentTimeMillis();
        if (now - lastImageChangedTime < delay) return false;
        lastImageChangedTime = now;
        return true;
    }

    // TODO: in Dialog Klasse auslagern
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
}