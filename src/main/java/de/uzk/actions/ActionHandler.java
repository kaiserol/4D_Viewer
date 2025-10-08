package de.uzk.actions;

import de.uzk.gui.Gui;
import de.uzk.image.ImageLayer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static de.uzk.Main.imageHandler;
import static de.uzk.actions.ActionUtils.*;

public class ActionHandler extends KeyAdapter implements MouseWheelListener {
    private final Gui gui;
    private long lastKeyPressTime = 0;
    private long lastMouseWheelTime = 0;

    public ActionHandler(Gui gui) {
        this.gui = gui;
    }

    private boolean equals(KeyEvent curKeyEvent, KeyEvent keyEvent) {
        return curKeyEvent.getKeyCode() == keyEvent.getKeyCode() && curKeyEvent.getModifiersEx() == keyEvent.getModifiersEx();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        executeMove(e);
    }

    public void executeMove(KeyEvent e) {
        if (allowMove(true)) {
            int oldTime = imageHandler.getTime();

            // checks whether the event equals a move in the time layer
            if (equals(e, ACTION_FIRST_IMAGE)) imageHandler.toFirst(ImageLayer.TIME);
            else if (equals(e, ACTION_PREV_IMAGE)) imageHandler.prev(ImageLayer.TIME);
            else if (equals(e, ACTION_NEXT_IMAGE)) imageHandler.next(ImageLayer.TIME);
            else if (equals(e, ACTION_LAST_IMAGE)) imageHandler.toLast(ImageLayer.TIME);

            // checks whether the event equals a move in the level layer
            int oldLevel = imageHandler.getLevel();
            if (equals(e, ACTION_FIRST_LEVEL)) imageHandler.toFirst(ImageLayer.LEVEL);
            else if (equals(e, ACTION_PREV_LEVEL)) imageHandler.prev(ImageLayer.LEVEL);
            else if (equals(e, ACTION_NEXT_LEVEL)) imageHandler.next(ImageLayer.LEVEL);
            else if (equals(e, ACTION_LAST_LEVEL)) imageHandler.toLast(ImageLayer.LEVEL);

            // check if time or level changed
            if (imageHandler.getTime() != oldTime) gui.update(ImageLayer.TIME);
            if (imageHandler.getLevel() != oldLevel) gui.update(ImageLayer.LEVEL);
        }
    }

    private boolean allowMove(boolean isKeyPressed) {
        if (imageHandler.isEmpty()) return false;

        boolean muchComputingTime = imageHandler.getImageDetails().getRotation() != 0;
        long currentTime = System.currentTimeMillis();

        // Ignore the keystroke if the delay has not expired yet.
        if (isKeyPressed) {
            if (currentTime - lastKeyPressTime < (muchComputingTime ? MUCH_COMPUTING_TIME_DELAY : KEY_PRESS_DELAY))
                return false;
            lastKeyPressTime = currentTime;
        } else {
            if (currentTime - lastMouseWheelTime < (muchComputingTime ? MUCH_COMPUTING_TIME_DELAY : MOUSE_WHEEL_DELAY))
                return false;
            lastMouseWheelTime = currentTime;
        }
        return true;
    }

    public void executeEdit(KeyEvent e) {
        if (equals(e, ACTION_PIN_TIME)) gui.handleAction(ActionType.TOGGLE_PIN_TIME);
        else if (equals(e, ACTION_TURN_IMAGE_RIGHT)) gui.handleAction(ActionType.TURN_IMAGE_90_RIGHT);
        else if (equals(e, ACTION_SCREENSHOT)) gui.handleAction(ActionType.TAKE_SCREENSHOT);
        else if (equals(e, ACTION_TURN_IMAGE_LEFT)) gui.handleAction(ActionType.TURN_IMAGE_90_LEFT);
    }

    // ----------------------- movement with mouse wheeler --------------------

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseWheelMoved(e.isShiftDown(), e.getWheelRotation(), false);
    }

    public void mouseWheelMoved(boolean shift, int rotation, boolean isKeyPressed) {
        if (allowMove(isKeyPressed)) {
            executeMove(shift, rotation);
        }
    }

    private void executeMove(boolean shift, int rotation) {
        if (shift) {
            int oldTime = imageHandler.getTime();
            if (rotation > 0) imageHandler.next(ImageLayer.TIME);
            else if (rotation < 0) imageHandler.prev(ImageLayer.TIME);

            // update time
            if (oldTime != imageHandler.getTime()) gui.update(ImageLayer.TIME);
        } else {
            int oldLevel = imageHandler.getLevel();
            if (rotation > 0) imageHandler.next(ImageLayer.LEVEL);
            else if (rotation < 0) imageHandler.prev(ImageLayer.LEVEL);

            // update level
            if (oldLevel != imageHandler.getLevel()) gui.update(ImageLayer.LEVEL);
        }
    }
}
