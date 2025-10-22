package de.uzk.action;

import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.dialogs.DialogDisclaimer;
import de.uzk.gui.dialogs.DialogLanguageSelection;
import de.uzk.gui.dialogs.DialogLogViewer;
import de.uzk.gui.dialogs.DialogSettings;
import de.uzk.image.Axis;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static de.uzk.Main.workspace;
import static de.uzk.action.ActionType.*;

public class ActionHandler extends KeyAdapter implements MouseWheelListener {
    // ca. 20/13 FPS, gleicht Slider & Keys Geschwindigkeit an
    private static final long UPDATE_INTERVAL_MS = 50;
    private static final long LONG_UPLOAD_INTERVAL_MS = 75;

    // GUI-Elemente
    private final Gui gui;
    private final DialogSettings dialogSettings;
    private final DialogLanguageSelection dialogLanguageSelection;
    private final DialogDisclaimer dialogDisclaimer;
    private final DialogLogViewer dialogLogViewer;

    // Zeitmessung, um Bildwechsel zu takten
    private long lastUpdateTime = 0;

    public ActionHandler(Gui gui) {
        this.gui = gui;
        this.dialogSettings = new DialogSettings();
        this.dialogLanguageSelection = new DialogLanguageSelection();
        this.dialogDisclaimer = new DialogDisclaimer(gui.getContainer());
        this.dialogLogViewer = new DialogLogViewer(gui.getContainer());
    }

    // ======================================
    // Key Events
    // ======================================
    @Override
    public void keyPressed(KeyEvent e) {
        ActionType actionType = ActionType.getAction(e);
        if (actionType == null) return;
        navigateImage(actionType);
    }

    // ======================================
    // MouseWheel Events
    // ======================================
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        boolean shift = e.isShiftDown();
        int rotation = e.getWheelRotation();
        Axis axis = shift ? Axis.TIME : Axis.LEVEL;
        scroll(axis, rotation, false);
    }

    public void scroll(Axis axis, int rotation, boolean isAdjusting) {
        // Gleiche Logik für Tastatur, Maus und ScrollBar
        if (axis == null || rotation == 0) return;
        if (!isAdjusting && preventNextUpdate()) return;

        int abs = Math.abs(rotation);
        if (abs > 1) {
            if (axis == Axis.TIME) {
                int newTime = (rotation < 0) ?
                        Math.max(0, workspace.getTime() - abs) :
                        Math.min(workspace.getMaxTime(), workspace.getTime() + rotation);
                workspace.setTime(newTime);
            } else {
                int newLevel = (rotation < 0) ?
                        Math.max(0, workspace.getLevel() - abs) :
                        Math.min(workspace.getMaxLevel(), workspace.getLevel() + rotation);
                workspace.setLevel(newLevel);
            }
        } else {
            if (rotation < 0) workspace.prev(axis);
            else workspace.next(axis);
        }
        gui.update(axis);
    }

    private void scrollToBoundary(Axis axis, boolean toFirst) {
        if (preventNextUpdate()) return;

        if (toFirst) workspace.toFirst(axis);
        else workspace.toLast(axis);
        gui.update(axis);
    }

    // ======================================
    // Aktionen aus Tastaturkürzeln
    // ======================================
    public void executeAction(ActionType actionType) {
        if (actionType == null) return;
        switch (actionType) {
            // edit actions
            case SHORTCUT_TOGGLE_PIN_TIME -> gui.handleAction(SHORTCUT_TOGGLE_PIN_TIME);
            case SHORTCUT_TURN_IMAGE_90_LEFT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_LEFT);
            case SHORTCUT_TURN_IMAGE_90_RIGHT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_RIGHT);
            case SHORTCUT_TAKE_SCREENSHOT -> gui.handleAction(SHORTCUT_TAKE_SCREENSHOT);

            // window actions
            case SHORTCUT_FONT_SIZE_DECREASE -> GuiUtils.decreaseFont(gui);
            case SHORTCUT_FONT_SIZE_INCREASE -> GuiUtils.increaseFont(gui);
            case SHORTCUT_FONT_SIZE_RESTORE -> GuiUtils.restoreFont(gui);

            case SHORTCUT_SHOW_DISCLAIMER -> dialogDisclaimer.show();
            case SHORTCUT_SHOW_LOG_VIEWER -> dialogLogViewer.show();

            // settings actions
            case SHORTCUT_SELECT_LANGUAGE -> dialogLanguageSelection.show(gui);
            case SHORTCUT_TOGGLE_THEME -> GuiUtils.toggleTheme(gui);
            case SHORTCUT_OPEN_SETTINGS -> dialogSettings.show(gui);
        }
    }

    private void navigateImage(ActionType actionType) {
        switch (actionType) {
            // navigate actions
            case SHORTCUT_GO_TO_FIRST_IMAGE -> scrollToBoundary(Axis.TIME, true);
            case SHORTCUT_GO_TO_PREV_IMAGE -> scroll(Axis.TIME, -1, false);
            case SHORTCUT_GO_TO_NEXT_IMAGE -> scroll(Axis.TIME, 1, false);
            case SHORTCUT_GO_TO_LAST_IMAGE -> scrollToBoundary(Axis.TIME, false);

            case SHORTCUT_GO_TO_FIRST_LEVEL -> scrollToBoundary(Axis.LEVEL, true);
            case SHORTCUT_GO_TO_PREV_LEVEL -> scroll(Axis.LEVEL, -1, false);
            case SHORTCUT_GO_TO_NEXT_LEVEL -> scroll(Axis.LEVEL, 1, false);
            case SHORTCUT_GO_TO_LAST_LEVEL -> scrollToBoundary(Axis.LEVEL, false);
        }
    }

    private boolean preventNextUpdate() {
        long now = System.currentTimeMillis();
        long interval = workspace.getConfig().getRotation() != 0 ? LONG_UPLOAD_INTERVAL_MS : UPDATE_INTERVAL_MS;
        if (now - lastUpdateTime < interval) return true;
        lastUpdateTime = now;
        return false;
    }
}