package de.uzk.gui;

import de.uzk.handler.ImageLayer;

import java.awt.*;

public abstract class InteractiveContainer<T extends Container> implements ToggleListener, UpdateImageListener, UpdateUIListener {
    protected T container;
    protected final Gui gui;

    protected InteractiveContainer(T container, Gui gui) {
        this.container = container;
        this.gui = gui;
        if (gui != null) {
            this.gui.addToggleListener(this);
            this.gui.addUpdateImageListener(this);
            this.gui.addUpdateUIListener(this);
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
}
