package de.uzk.gui;

import javax.swing.*;

public class OSpinnerNumberModel extends SpinnerNumberModel {
    private final boolean cycling;

    public OSpinnerNumberModel(int value, int minimum, int maximum, int stepSize, boolean cycling) {
        super(value, minimum, maximum, stepSize);
        this.cycling = cycling;
    }

    public OSpinnerNumberModel(double value, double minimum, double maximum, double stepSize, boolean cycling) {
        super(value, minimum, maximum, stepSize);
        this.cycling = cycling;
    }

    @Override
    public Object getNextValue() {
        if (!cycling) return super.getNextValue();

        Comparable<?> next = (Comparable<?>) super.getNextValue();
        return (next != null) ? next : getMinimum(); // Return minimum when maximum is reached
    }

    @Override
    public Object getPreviousValue() {
        if (!cycling) return super.getPreviousValue();

        Comparable<?> previous = (Comparable<?>) super.getPreviousValue();
        return (previous != null) ? previous : getMaximum(); // Return maximum when minimum is reached
    }
}
