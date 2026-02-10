package de.uzk.gui;

import javax.swing.*;

public class CyclingSpinnerNumberModel extends SpinnerNumberModel {
    private final boolean cycling;

    public CyclingSpinnerNumberModel(int value, int minimum, int maximum, int stepSize, boolean cycling) {
        super(value, minimum, maximum, stepSize);
        this.cycling = cycling;
    }

    public CyclingSpinnerNumberModel(double value, double minimum, double maximum, double stepSize, boolean cycling) {
        super(value, minimum, maximum, stepSize);
        this.cycling = cycling;
    }

    @Override
    public Object getNextValue() {
        if (!cycling) return super.getNextValue();

        // Gibt das Minimum zurück, wenn das Maximum überschritten wird
        Comparable<?> next = (Comparable<?>) super.getNextValue();
        return (next != null) ? next : getMinimum();
    }

    @Override
    public Object getPreviousValue() {
        if (!cycling) return super.getPreviousValue();

        // Gibt das Maximum zurück, wenn das Minimum überschritten wird
        Comparable<?> previous = (Comparable<?>) super.getPreviousValue();
        return (previous != null) ? previous : getMaximum();
    }
}
