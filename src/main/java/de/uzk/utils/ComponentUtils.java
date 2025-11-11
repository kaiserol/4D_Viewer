package de.uzk.utils;

import de.uzk.gui.CyclingSpinnerNumberModel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.util.EventListener;
import java.util.Objects;
import java.util.function.Consumer;

public class ComponentUtils {
    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    public static JCheckBox createCheckBox(String text, Consumer<Boolean> listener) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.addActionListener(e -> listener.accept(checkBox.isSelected()));
        checkBox.setFocusPainted(true);
        return checkBox;
    }

    public static JScrollBar createScrollBar(int orientation, Consumer<Integer> listener) {
        @SuppressWarnings("MagicConstant")
        JScrollBar scrollBar = new JScrollBar(orientation);
        scrollBar.addAdjustmentListener(e -> listener.accept(scrollBar.getValue()));
        scrollBar.setBlockIncrement(1);
        scrollBar.setUnitIncrement(1);
        return scrollBar;
    }

    public static JSlider createSlider(int min, int max, Consumer<Integer> listener) {
        JSlider slider = new JSlider(min, max, min);
        slider.addChangeListener(e -> listener.accept(slider.getValue()));
        slider.setSnapToTicks(true);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        return slider;
    }

    public static JSpinner createSpinner(int min, int max, boolean cycling, Consumer<Integer> listener) {
        JSpinner spinner = new JSpinner(new CyclingSpinnerNumberModel(min, min, max, 1, cycling));
        spinner.addChangeListener(e -> listener.accept((int) spinner.getValue()));
        return spinner;
    }

    // ========================================
    // RunWithoutListeners – Standardvarianten
    // ========================================
    public static void runWithoutListeners(JCheckBox component, Consumer<JCheckBox> action) {
        runWithoutListeners(component, action, ActionListener.class,
            component::addActionListener,
            component::removeActionListener
        );
    }

    public static void runWithoutListeners(JScrollBar component, Consumer<JScrollBar> action) {
        runWithoutListeners(component, action, AdjustmentListener.class,
            component::removeAdjustmentListener,
            component::addAdjustmentListener
        );
    }

    public static void runWithoutListeners(JSlider component, Consumer<JSlider> action) {
        runWithoutListeners(component, action, ChangeListener.class,
            component::removeChangeListener,
            component::addChangeListener
        );
    }

    public static void runWithoutListeners(JSpinner component, Consumer<JSpinner> action) {
        runWithoutListeners(component, action, ChangeListener.class,
            component::removeChangeListener,
            component::addChangeListener
        );
    }

    // ========================================
    // Erweiterte Varianten: Nur bei Änderungen
    // ========================================
    public static void setValueSecurely(JCheckBox checkBox, boolean newValue) {
        if (checkBox.isSelected() == newValue) return;
        runWithoutListeners(checkBox, c -> c.setSelected(newValue));
    }

    public static void setValueSecurely(JScrollBar scrollBar, int newValue) {
        if (scrollBar.getValueIsAdjusting()) return;
        if (scrollBar.getValue() == newValue) return;
        runWithoutListeners(scrollBar, sb -> sb.setValue(newValue));
    }

    public static void setValueSecurely(JSlider slider, int newValue) {
        if (slider.getValueIsAdjusting()) return;
        if (slider.getValue() == newValue) return;
        runWithoutListeners(slider, s -> s.setValue(newValue));
    }

    public static void setValueSecurely(JSpinner spinner, int newValue) {
        if (spinner.getValue() instanceof Number value && value.intValue() == newValue) return;
        runWithoutListeners(spinner, s -> {
            s.setValue(newValue);
            if (s.getEditor() instanceof JSpinner.DefaultEditor defaultEditor) {
                defaultEditor.getTextField().setValue(newValue);
            }
        });
    }

    // ========================================
    // Generische Basismethode
    // ========================================
    private static <C extends JComponent, T extends EventListener> void runWithoutListeners(
        C component, Consumer<C> action, Class<T> listenerType,
        Consumer<T> removeListener, Consumer<T> addListener) {

        T[] listeners = component.getListeners(listenerType);
        for (T listener : listeners) removeListener.accept(listener);
        try {
            action.accept(component);
        } finally {
            for (T listener : listeners) addListener.accept(listener);
        }
    }

    // ------------------------------------------------------
    // Komponenten-Aktivierung / -Deaktivierung
    // ------------------------------------------------------
    public static void setEnabled(Container container, boolean enabled) {
        if (container == null) return;

        for (Component component : getComponents(container)) {
            component.setEnabled(enabled);
            if (component instanceof Container newContainer) {
                setEnabled(newContainer, enabled);
            }
        }
    }

    // ------------------------------------------------------
    // Komponenten-Ermittlung
    // ------------------------------------------------------
    public static Component[] getComponents(Container container) {
        if (container == null) return new Component[0];
        else if (container instanceof JWindow window) return window.getOwnedWindows();
        else if (container instanceof JFrame frame) return frame.getContentPane().getComponents();
        else if (container instanceof JDialog dialog) return dialog.getContentPane().getComponents();
        else if (container instanceof JMenu menu) return menu.getMenuComponents();
        else if (container instanceof JScrollPane scrollPane) return scrollPane.getViewport().getComponents();
        else return container.getComponents();
    }

    // ------------------------------------------------------
    // Layout-Helfer
    // ------------------------------------------------------
    public static void makeComponentsSameSize(JPanel panel, Class<? extends Component> clazz) {
        int maxWidth = 0;

        // Maximale Breite bestimmen
        for (Component comp : panel.getComponents()) {
            if (Objects.equals(comp.getClass(), clazz)) {
                Dimension pref = comp.getPreferredSize();
                maxWidth = Math.max(maxWidth, pref.width);
            }
        }

        // Einheitliche Größe setzen
        for (Component comp : panel.getComponents()) {
            if (Objects.equals(comp.getClass(), clazz)) {
                Dimension size = comp.getPreferredSize();
                comp.setPreferredSize(new Dimension(maxWidth, size.height));
            }
        }
    }

    public static GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    public static void addLabeledRow(Container container, GridBagConstraints gbc, String labelText, JComponent component, int topInset) {
        addLabeledRow(container, gbc, new JLabel(labelText + ":"), component, topInset);
    }

    public static void addLabeledRow(Container container, GridBagConstraints gbc, JLabel label, JComponent component,  int topInset) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets.top = topInset;
        gbc.insets.right = 10;
        if (label != null) container.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets.right = 0;
        if (component != null) container.add(component, gbc);

        gbc.gridy++;
    }

    public static void addRow(Container container, GridBagConstraints gbc, JComponent component, int topInset) {
        gbc.gridx = 0;
        gbc.insets.top = topInset;
        if (component != null) container.add(component, gbc);

        gbc.gridy++;
    }
}
