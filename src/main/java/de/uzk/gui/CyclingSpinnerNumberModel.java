package de.uzk.gui;

import javax.swing.*;

public class CyclingSpinnerNumberModel extends SpinnerNumberModel {
    public CyclingSpinnerNumberModel(int value, int minimum, int maximum, int stepSize) {
        super(value, minimum, maximum, stepSize);
    }

    public CyclingSpinnerNumberModel(double value, double minimum, double maximum, double stepSize) {
        super(value, minimum, maximum, stepSize);
    }

    @Override
    public Object getNextValue() {
        // Return minimum when maximum is reached
        Comparable<?> next = (Comparable<?>) super.getNextValue();
        return (next != null) ? next : getMinimum();
    }

    @Override
    public Object getPreviousValue() {
        // Return maximum when minimum is reached
        Comparable<?> previous = (Comparable<?>) super.getPreviousValue();
        return (previous != null) ? previous : getMaximum();
    }
}
