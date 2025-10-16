package de.uzk.action;

import de.uzk.gui.*;
import de.uzk.gui.dialogs.DialogDisclaimer;
import de.uzk.gui.dialogs.DialogSettings;
import de.uzk.gui.dialogs.DialogLanguageSelection;
import de.uzk.gui.dialogs.DialogLogViewer;
import de.uzk.image.Axis;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static de.uzk.Main.imageFileHandler;
import static de.uzk.action.ActionType.*;

public class ActionHandler extends KeyAdapter implements MouseWheelListener {
    private static final long HIGH_LOAD_DELAY = 40;
    private static final long LOAD_DELAY = 25;

    private final Gui gui;
    private final DialogSettings dialogSettings;
    private final DialogLanguageSelection dialogLanguageSelection;
    private final DialogDisclaimer dialogDisclaimer;
    private final DialogLogViewer dialogLogViewer;
    private long lastImageChangedTime = 0;

    public ActionHandler(Gui gui) {
        this.gui = gui;
        this.dialogSettings = new DialogSettings();
        this.dialogLanguageSelection = new DialogLanguageSelection();
        this.dialogDisclaimer = new DialogDisclaimer(gui.getContainer());
        this.dialogLogViewer = new DialogLogViewer(gui.getContainer());
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
        Axis axis = shift ? Axis.TIME : Axis.LEVEL;
        scrollToNextImage(axis, rotation, false);
    }

    public void executeAction(ActionType actionType) {
        if (actionType == null) return; // Der Fall sollte eigentlich nie eintreten
        switch (actionType) {
            // edit actions
            case SHORTCUT_TOGGLE_PIN_TIME -> gui.handleAction(SHORTCUT_TOGGLE_PIN_TIME);
            case SHORTCUT_TURN_IMAGE_90_LEFT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_LEFT);
            case SHORTCUT_TURN_IMAGE_90_RIGHT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_RIGHT);
            case SHORTCUT_TAKE_SCREENSHOT -> gui.handleAction(SHORTCUT_TAKE_SCREENSHOT);

            // navigate actions
            case SHORTCUT_GO_TO_FIRST_IMAGE -> scrollToNextImage(Axis.TIME, -1, true);
            case SHORTCUT__GO_TO_PREV_IMAGE -> scrollToNextImage(Axis.TIME, -1, false);
            case SHORTCUT_GO_TO_NEXT_IMAGE -> scrollToNextImage(Axis.TIME, 1, false);
            case SHORTCUT_GO_TO_LAST_IMAGE -> scrollToNextImage(Axis.TIME, 1, true);

            case SHORTCUT_GO_TO_FIRST_LEVEL -> scrollToNextImage(Axis.LEVEL, -1, true);
            case SHORTCUT_GO_TO_PREV_LEVEL -> scrollToNextImage(Axis.LEVEL, -1, false);
            case SHORTCUT_GO_TO_NEXT_LEVEL -> scrollToNextImage(Axis.LEVEL, 1, false);
            case SHORTCUT_GO_TO_LAST_LEVEL -> scrollToNextImage(Axis.LEVEL, 1, true);

            // window actions
            case SHORTCUT_FONT_SIZE_DECREASE -> GuiUtils.decreaseFont(gui);
            case SHORTCUT_FONT_SIZE_RESTORE -> GuiUtils.restoreFont(gui);
            case SHORTCUT_FONT_SIZE_INCREASE -> GuiUtils.increaseFont(gui);

            case SHORTCUT_SHOW_DISCLAIMER -> dialogDisclaimer.show();
            case SHORTCUT_SHOW_LOG_VIEWER -> dialogLogViewer.show();

            // settings actions
            case SHORTCUT_SELECT_LANGUAGE -> dialogLanguageSelection.show(gui);
            case SHORTCUT_TOGGLE_THEME -> GuiUtils.toggleTheme(gui);
            case SHORTCUT_OPEN_SETTINGS -> dialogSettings.show(gui);
            default -> {
            }
        }
    }

    private void scrollToNextImage(Axis axis, int rotation, boolean goToFirstOrLast) {
        if (rotation == 0 || !allowNextImageChange()) return;

        if (rotation < 0) {
            if (goToFirstOrLast) imageFileHandler.toFirst(axis);
            else imageFileHandler.prev(axis);
        } else {
            if (goToFirstOrLast) imageFileHandler.toLast(axis);
            else imageFileHandler.next(axis);
        }
        gui.update(axis);
    }

    private boolean allowNextImageChange() {
        long delay = imageFileHandler.getImageRotation() != 0 ? HIGH_LOAD_DELAY : LOAD_DELAY;
        long now = System.currentTimeMillis();
        if (now - lastImageChangedTime < delay) return false;
        lastImageChangedTime = now;
        return true;
    }
}