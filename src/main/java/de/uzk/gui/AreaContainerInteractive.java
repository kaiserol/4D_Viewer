package de.uzk.gui;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.image.ImageLayer;

import java.awt.*;

public abstract class AreaContainerInteractive<T extends Container> implements ActionTypeListener, ToggleListener, UpdateImageListener, UpdateUIListener, AppFocusListener {
    protected T container;
    protected final Gui gui;

    protected AreaContainerInteractive(T container, Gui gui) {
        this.container = container;
        this.gui = gui;
        if (gui != null) {
            this.gui.addActionTypeListener(this);
            this.gui.addToggleListener(this);
            this.gui.addUpdateImageListener(this);
            this.gui.addUpdateUIListener(this);
            this.gui.addAppFocusListener(this);
        }
    }

    public final void setContainer(T container) {
        if (container == null) throw new NullPointerException();
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
    public void update(ImageLayer layer) {
    }

    @Override
    public void updateUI() {
    }

    @Override
    public void appGainedFocus() {
    }

    @Override
    public void appLostFocus() {
    }
}
