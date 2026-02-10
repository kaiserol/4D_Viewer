package de.uzk.action;

import de.uzk.config.Settings;
import de.uzk.gui.Gui;
import de.uzk.gui.UIEnvironment;
import de.uzk.gui.dialogs.*;
import de.uzk.image.Axis;
import de.uzk.utils.ProjectUtils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static de.uzk.Main.settings;
import static de.uzk.Main.workspace;
import static de.uzk.action.ActionType.*;

// TODO: navigateImage Methoden (aus der executeAction eventuell in seperate Methode auslagern -> wie vorher). Dadurch
// dass navigateImage in executeAction enthalten ist, kann es sein, dass die navigateImages
// einmal durch den Accerelerator und durch den ImageViewer ActionListener aufgerufen wird.
// Will ich selbst testen, ob es nötig ist. (war früher nämlich ein Bug, dass es man es so gelassen hat...)
// Zumal soll beim Pressed, mousewheellistener eventuell dann auch auslagern (in AreaImageViewer), damit es
// eindeutiger ist...
public class ActionHandler extends KeyAdapter implements MouseWheelListener {
    // Es werden maximal 20 FPS / 13 FPS (bei gedrehten Bildern) erreicht
    private static final long UPDATE_INTERVAL_MS = 50;
    private static final long LONG_UPLOAD_INTERVAL_MS = 75;
    private final Gui gui;
    // Dialoge
    private final DialogAbout dialogAbout;
    private final DialogLegal dialogLegal;
    private final DialogHistory dialogHistory;
    private final DialogLogViewer dialogLogViewer;
    private final DialogSettings dialogSettings;
    // Zeitmessung, um Bildwechsel zu takten
    private long lastUpdateTime = 0;

    public ActionHandler(Gui gui) {
        this.gui = gui;
        this.dialogAbout = new DialogAbout(gui.getContainer());
        this.dialogLegal = new DialogLegal(gui.getContainer());
        this.dialogHistory = new DialogHistory(gui.getContainer());
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
            // Projekte Shortcuts
            case SHORTCUT_OPEN_FOLDER -> ProjectUtils.openProject(gui);
            case SHORTCUT_OPEN_RECENT -> ProjectUtils.openRecents(gui);
            case SHORTCUT_CLOSE_PROJECT -> ProjectUtils.closeProject(gui);
            case SHORTCUT_SAVE_PROJECT -> {
                workspace.saveConfigs();
                gui.registerConfigSaved();
            }

            // Bearbeiten Shortcuts
            case SHORTCUT_PIN_TIME -> gui.handleAction(SHORTCUT_PIN_TIME);
            case SHORTCUT_TURN_IMAGE_90_LEFT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_LEFT);
            case SHORTCUT_TURN_IMAGE_90_RIGHT -> gui.handleAction(SHORTCUT_TURN_IMAGE_90_RIGHT);
            case SHORTCUT_TAKE_SNAPSHOT -> gui.handleAction(SHORTCUT_TAKE_SNAPSHOT);
            case SHORTCUT_UNDO -> {
                ActionType undone = workspace.getEditManager().undoLastEdit();
                gui.handleAction(undone);
            }
            case SHORTCUT_REDO -> {
                ActionType redone = workspace.getEditManager().redoLastEdit();
                gui.handleAction(redone);
            }

            // Navigieren Shortcuts
            case SHORTCUT_GO_TO_FIRST_IMAGE -> scrollToBoundary(Axis.TIME, true);
            case SHORTCUT_GO_TO_PREV_IMAGE -> scroll(Axis.TIME, -1, false);
            case SHORTCUT_GO_TO_NEXT_IMAGE -> scroll(Axis.TIME, 1, false);
            case SHORTCUT_GO_TO_LAST_IMAGE -> scrollToBoundary(Axis.TIME, false);

            case SHORTCUT_GO_TO_FIRST_LEVEL -> scrollToBoundary(Axis.LEVEL, true);
            case SHORTCUT_GO_TO_PREV_LEVEL -> scroll(Axis.LEVEL, -1, false);
            case SHORTCUT_GO_TO_NEXT_LEVEL -> scroll(Axis.LEVEL, 1, false);
            case SHORTCUT_GO_TO_LAST_LEVEL -> scrollToBoundary(Axis.LEVEL, false);

            // Fenster Shortcuts
            case SHORTCUT_FONT_SIZE_DECREASE -> UIEnvironment.updateFontSize(gui, settings.getFontSize() - 1);
            case SHORTCUT_FONT_SIZE_INCREASE -> UIEnvironment.updateFontSize(gui, settings.getFontSize() + 1);
            case SHORTCUT_FONT_SIZE_RESTORE -> UIEnvironment.updateFontSize(gui, Settings.DEFAULT_FONT_SIZE);
            case SHORTCUT_OPEN_SETTINGS -> dialogSettings.show();

            // Hilfe Shortcuts
            case SHORTCUT_SHOW_ABOUT -> dialogAbout.show();
            case SHORTCUT_SHOW_LEGAL -> dialogLegal.show();
            case SHORTCUT_SHOW_HISTORY -> dialogHistory.show();
            case SHORTCUT_SHOW_LOG_VIEWER -> dialogLogViewer.show();
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