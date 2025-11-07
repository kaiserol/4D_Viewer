package de.uzk.action;

import de.uzk.config.Settings;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.dialogs.DialogDisclaimer;
import de.uzk.gui.dialogs.DialogLogViewer;
import de.uzk.gui.dialogs.DialogSettings;
import de.uzk.gui.dialogs.DialogVersions;
import de.uzk.image.Axis;
import de.uzk.utils.ProjectsHelper;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static de.uzk.Main.settings;
import static de.uzk.Main.workspace;
import static de.uzk.action.ActionType.*;

public class ActionHandler extends KeyAdapter implements MouseWheelListener {
    // GUI-Elemente
    private final Gui gui;
    private final DialogDisclaimer dialogDisclaimer;
    private final DialogVersions dialogVersions;
    private final DialogLogViewer dialogLogViewer;
    private final DialogSettings dialogSettings;

    // Zeitmessung, um Bildwechsel zu takten
    private long lastUpdateTime = 0;

    // Es werden maximal 20 FPS / 13 FPS (bei gedrehten Bildern) erreicht
    private static final long UPDATE_INTERVAL_MS = 50;
    private static final long LONG_UPLOAD_INTERVAL_MS = 75;

    public ActionHandler(Gui gui) {
        this.gui = gui;
        this.dialogDisclaimer = new DialogDisclaimer(gui.getContainer());
        this.dialogVersions = new DialogVersions(gui.getContainer());
        this.dialogLogViewer = new DialogLogViewer(gui.getContainer());
        this.dialogSettings = new DialogSettings(gui);
    }

    // ========================================
    // Key Events
    // ========================================
    @Override
    public void keyPressed(KeyEvent e) {
        ActionType actionType = ActionType.fromKeyEvent(e);
        executeAction(actionType);
    }

    // ========================================
    // MouseWheel Events
    // ========================================
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        boolean shift = e.isShiftDown();
        int rotation = e.getWheelRotation();
        Axis axis = shift ? Axis.TIME : Axis.LEVEL;
        scroll(axis, rotation, false);
    }

    // ========================================
    // Aktionen aus Tastaturkürzeln
    // ========================================
    public void executeAction(ActionType actionType) {
        if (actionType == null) return;
        switch (actionType) {
            // Bearbeiten Shortcuts
            case SHORTCUT_TURN_IMAGE_90_LEFT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_LEFT);
            case SHORTCUT_TURN_IMAGE_90_RIGHT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_RIGHT);
            case ACTION_TAKE_SNAPSHOT -> gui.handleAction(ACTION_TAKE_SNAPSHOT);

            // Sonstige Shortcuts
            case SHORTCUT_TOGGLE_PIN_TIME -> gui.handleAction(SHORTCUT_TOGGLE_PIN_TIME);

            // Fenster Shortcuts
            case SHORTCUT_FONT_SIZE_DECREASE -> GuiUtils.updateFontSize(gui, settings.getFontSize() - 1);
            case SHORTCUT_FONT_SIZE_INCREASE -> GuiUtils.updateFontSize(gui, settings.getFontSize() + 1);
            case SHORTCUT_FONT_SIZE_RESTORE -> GuiUtils.updateFontSize(gui, Settings.DEFAULT_FONT_SIZE);
            case SHORTCUT_OPEN_SETTINGS -> dialogSettings.show();

            // Hilfe Shortcuts
            case SHORTCUT_SHOW_DISCLAIMER -> dialogDisclaimer.show();
            case SHORTCUT_SHOW_VERSIONS -> dialogVersions.show();
            case SHORTCUT_SHOW_LOG_VIEWER -> dialogLogViewer.show();

            // Navigieren Shortcuts
            case SHORTCUT_GO_TO_FIRST_IMAGE -> scrollToBoundary(Axis.TIME, true);
            case SHORTCUT_GO_TO_PREV_IMAGE -> scroll(Axis.TIME, -1, false);
            case SHORTCUT_GO_TO_NEXT_IMAGE -> scroll(Axis.TIME, 1, false);
            case SHORTCUT_GO_TO_LAST_IMAGE -> scrollToBoundary(Axis.TIME, false);

            case SHORTCUT_GO_TO_FIRST_LEVEL -> scrollToBoundary(Axis.LEVEL, true);
            case SHORTCUT_GO_TO_PREV_LEVEL -> scroll(Axis.LEVEL, -1, false);
            case SHORTCUT_GO_TO_NEXT_LEVEL -> scroll(Axis.LEVEL, 1, false);
            case SHORTCUT_GO_TO_LAST_LEVEL -> scrollToBoundary(Axis.LEVEL, false);

            // Projekte Shortcuts
            case SHORTCUT_OPEN_RECENT -> ProjectsHelper.openRecents(gui);
            case SHORTCUT_OPEN_FOLDER ->
                ProjectsHelper.openFileChooser(gui);

            case SHORTCUT_SAVE_CONFIG -> {
                workspace.getConfig().save();
                gui.registerConfigSaved();
            }
            case SHORTCUT_CLOSE_PROJECT -> ProjectsHelper.clearImages(gui);
        }
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
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

    private boolean preventNextUpdate() {
        long now = System.currentTimeMillis();
        long interval = (workspace.getConfig().getRotation() != 0) ? LONG_UPLOAD_INTERVAL_MS : UPDATE_INTERVAL_MS;
        if (now - lastUpdateTime < interval) return true;
        lastUpdateTime = now;
        return false;
    }
}