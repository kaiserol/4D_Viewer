package de.uzk.gui;

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
        Object current = spinner.getValue();
        if (current instanceof Number && ((Number) current).intValue() == newValue) return;
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
}
