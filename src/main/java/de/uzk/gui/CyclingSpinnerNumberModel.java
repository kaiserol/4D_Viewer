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
        // Gibt das Minimum zurück, wenn das Maximum überschritten wird
        Comparable<?> next = (Comparable<?>) super.getNextValue();
        return (next != null) ? next : getMinimum();
    }

    @Override
    public Object getPreviousValue() {
        // Gibt das Maximum zurück, wenn das Minimum überschritten wird
        Comparable<?> previous = (Comparable<?>) super.getPreviousValue();
        return (previous != null) ? previous : getMaximum();
    }
}
