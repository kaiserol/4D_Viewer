package de.uzk.gui.areas;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.observer.*;
import de.uzk.image.Axis;

import java.awt.*;

public abstract class AreaContainerInteractive<T extends Container> implements HandleActionListener, ToggleListener, UpdateImageListener, UpdateThemeListener, AppFocusListener {
    protected T container;
    protected final Gui gui;

    public AreaContainerInteractive(T container, Gui gui) {
        setContainer(container);
        this.gui = gui;
        if (gui != null) {
            this.gui.registerHandleActionListener(this);
            this.gui.registerToggleListener(this);
            this.gui.registerUpdateImageListener(this);
            this.gui.registerUpdateThemeListener(this);
            this.gui.registerAppFocusListener(this);
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
