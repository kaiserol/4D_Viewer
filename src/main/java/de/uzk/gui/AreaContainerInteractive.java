package de.uzk.gui;

import de.uzk.action.ActionType;
import de.uzk.action.HandleActionListener;
import de.uzk.image.Axis;

import java.awt.*;

public abstract class AreaContainerInteractive<T extends Container> implements HandleActionListener, ToggleListener, UpdateImageListener, UpdateThemeListener, AppFocusListener {
    protected T container;
    protected final Gui gui;

    protected AreaContainerInteractive(T container, Gui gui) {
        this.container = container;
        this.gui = gui;
        if (gui != null) {
            this.gui.addHandleActionListener(this);
            this.gui.addToggleListener(this);
            this.gui.addUpdateImageListener(this);
            this.gui.addUpdateThemeListener(this);
            this.gui.addAppFocusListener(this);
        }
    }

    public final void setContainer(T container) {
        if (container == null) throw new NullPointerException("Container is null.");
        this.container = container;
    }

    public final T getContainer() {
        return container;
    }

    @Override
    public void handleAction(ActionType actionType) {
    }

    @Override
    public void toggleOn() {
    }

    @Override
    public void toggleOff() {
    }

    @Override
    public void update(Axis axis) {
    }

    @Override
    public void updateTheme() {
    }

    @Override
    public void appGainedFocus() {
    }

    @Override
    public void appLostFocus() {
    }
}
