package de.uzk.utils;

import de.uzk.edit.Edit;
import de.uzk.gui.CyclingSpinnerNumberModel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility-Klasse für häufig verwendete Swing-Komponenten und deren Handhabung.
 *
 * <br><br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class ComponentUtils {
    /**
     * Standard-Cursor
     */
    public static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
    /**
     * Hand-Cursor (z. B. für klickbare Elemente)
     */
    public static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    // ========================================
    // Komponenten-Erzeugung
    // ========================================

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private ComponentUtils() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Gibt ein unsichtbares, nicht blinkendes Caret zurück.
     * Nützlich z. B. für JTextFields, die keine blinkende Eingabemarke haben sollen.
     *
     * @return Caret ohne Sichtbarkeit und ohne Blinken
     */
    public static Caret getNoBlinkCaret() {
        Caret caret = new DefaultCaret() {
            @Override
            public void paint(Graphics g) {
                // Caret bleibt unsichtbar
            }
        };
        caret.setBlinkRate(0);
        return caret;
    }

    /**
     * Erstellt einen konfigurierten {@link JDialog}, der ein einheitliches Schließverhalten besitzt.
     *
     * <p>
     * Der erzeugte Dialog:
     * <ul>
     *     <li>reagiert auf das Schließen über den Fenster-Schließen-Button (X)</li>
     *     <li>reagiert auf das Drücken der ESC-Taste</li>
     *     <li>führt vor dem Schließen optional eine benutzerdefinierte Aktion aus</li>
     *     <li>wird erst nach Ausführung dieser Aktion geschlossen</li>
     *     <li>verwendet die Standard-Modalität {@link Dialog.ModalityType#APPLICATION_MODAL}</li>
     * </ul>
     * </p>
     *
     * <p>
     * Die übergebene Aktion {@code beforeClosing} wird genau einmal ausgeführt,
     * unabhängig davon, ob der Dialog per ESC, Klick auf das X oder programmatisch
     * geschlossen wird.
     *
     * @param parentWindow  Das übergeordnete Fenster, zu dem der Dialog modal ist.
     *                      Darf {@code null} sein, falls kein Parent existiert.
     * @param beforeClosing Eine Aktion, die direkt vor dem Schließen des Dialogs
     *                      ausgeführt wird. Darf {@code null} sein, wenn keine
     *                      zusätzliche Logik benötigt wird.
     * @return Ein vollständig vorbereiteter {@link JDialog}, der bereits Listener und ESC-Handling
     * registriert hat, jedoch noch nicht sichtbar ist.
     */
    public static JDialog createDialog(Window parentWindow, Runnable beforeClosing) {
        JDialog dialog = new JDialog(parentWindow, "", Dialog.ModalityType.APPLICATION_MODAL);

        // Ausführung der Aktion kapseln
        Consumer<AWTEvent> safeRun = e -> {
            if (beforeClosing != null) beforeClosing.run();
            if (dialog.isVisible()) dialog.dispose();
        };

        // Listener reagiert auf das manuelle Schließen (X-Button)
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                safeRun.accept(e);
            }
        });

        // ESC-Taste schließt den Dialog
        dialog.getRootPane().registerKeyboardAction(
            safeRun::accept,
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        return dialog;
    }

    /**
     * Erstellt ein JCheckBox mit einem Listener.
     *
     * @param text     Text der Checkbox
     * @param listener Consumer, der beim Ändern des Auswahlstatus aufgerufen wird; kann null sein
     * @return Die erstellte JCheckBox
     */
    public static JCheckBox createCheckBox(String text, Consumer<Boolean> listener) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFocusPainted(true);

        if (listener != null) {
            checkBox.addActionListener(e -> listener.accept(checkBox.isSelected()));
        }
        return checkBox;
    }

    /**
     * Erstellt ein JComboBox mit Listener.
     *
     * @param items    Elemente des Drop-Downs
     * @param listener Consumer, der beim Auswählen eines Elements aufgerufen wird; kann null sein
     * @param <E>      Typ der Elemente
     * @return Die erstellte JComboBox
     */
    public static <E> JComboBox<E> createComboBox(E[] items, Consumer<E> listener) {
        JComboBox<E> comboBox = new JComboBox<>(items);

        if (listener != null) {
            comboBox.addActionListener(e -> {
                int index = comboBox.getSelectedIndex();
                if (index < 0) return;
                listener.accept(comboBox.getItemAt(index));
            });
        }
        return comboBox;
    }

    /**
     * Erstellt eine JScrollBar mit Listener.
     *
     * @param orientation JScrollBar.HORIZONTAL oder JScrollBar.VERTICAL
     * @param listener    Consumer, der beim Ändern der Position aufgerufen wird; kann null sein
     * @return Die erstellte JScrollBar
     */
    public static JScrollBar createScrollBar(int orientation, Consumer<Integer> listener) {
        @SuppressWarnings("MagicConstant")
        JScrollBar scrollBar = new JScrollBar(orientation);

        scrollBar.setBlockIncrement(1);
        scrollBar.setUnitIncrement(1);

        if (listener != null) {
            scrollBar.addAdjustmentListener(e -> listener.accept(scrollBar.getValue()));
        }
        return scrollBar;
    }

    /**
     * Erstellt einen JSlider mit Listener.
     *
     * @param min      Minimalwert
     * @param max      Maximalwert
     * @param listener Consumer, der beim Ändern des Werts aufgerufen wird; kann null sein
     * @return Der erstellte JSlider
     */
    public static JSlider createSlider(int min, int max, Consumer<Integer> listener) {
        JSlider slider = new JSlider(min, max, min);
        slider.setSnapToTicks(true);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);

        if (listener != null) {
            slider.addChangeListener(e -> listener.accept(slider.getValue()));
        }
        return slider;
    }

    /**
     * Erstellt einen JSpinner mit optionalem Cycling und Listener.
     *
     * @param min      Minimalwert
     * @param max      Maximalwert
     * @param cycling  {@code true}, wenn der Spinner am Ende wieder von vorn beginnt
     * @param listener Consumer, der beim Ändern des Werts aufgerufen wird; kann null sein
     * @return Der erstellte JSpinner
     */
    public static JSpinner createSpinner(int min, int max, boolean cycling, Consumer<Integer> listener) {
        JSpinner spinner = new JSpinner(new CyclingSpinnerNumberModel(min, min, max, 1, cycling));

        if (listener != null) {
            spinner.addChangeListener(e -> listener.accept((int) spinner.getValue()));
        }
        return spinner;
    }



    // ========================================
    // RunWithoutListeners: Standardvarianten
    // ========================================

    /**
     * Setzt den Wert einer JCheckBox, ohne Auslösen von Listenern.
     *
     * @param checkBox Zu ändernde Checkbox
     * @param newValue Neuer Wert
     */
    public static void setValueSecurely(JCheckBox checkBox, boolean newValue) {
        if (checkBox.isSelected() == newValue) return;
        runWithoutListeners(checkBox, c -> c.setSelected(newValue));
    }

    /**
     * Setzt den Wert einer JScrollBar, ohne Auslösen von Listenern.
     *
     * @param scrollBar Zu ändernde ScrollBar
     * @param newValue  Neuer Wert
     */
    public static void setValueSecurely(JScrollBar scrollBar, int newValue) {
        if (scrollBar.getValueIsAdjusting()) return;
        if (scrollBar.getValue() == newValue) return;
        runWithoutListeners(scrollBar, sb -> sb.setValue(newValue));
    }

    /**
     * Setzt den Wert eines Sliders, ohne Auslösen von Listenern.
     *
     * @param slider   Zu ändernder Slider
     * @param newValue Neuer Wert
     */
    public static void setValueSecurely(JSlider slider, int newValue) {
        if (slider.getValueIsAdjusting()) return;
        if (slider.getValue() == newValue) return;
        runWithoutListeners(slider, s -> s.setValue(newValue));
    }

    /**
     * Setzt den Wert eines Spinners, ohne Auslösen von Listenern.
     *
     * @param spinner  Zu ändernder Spinner
     * @param newValue Neuer Wert
     */
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
    // RunWithoutListeners generisch
    // ========================================

    /**
     * Führt eine Aktion an einer JCheckBox aus, ohne die registrierten ActionListener auszulösen.
     */
    public static void runWithoutListeners(JCheckBox component, Consumer<JCheckBox> action) {
        runWithoutListeners(component, action, ActionListener.class,
            component::addActionListener,
            component::removeActionListener
        );
    }

    /**
     * Führt eine Aktion an einer JScrollBar aus, ohne die registrierten AdjustmentListener auszulösen.
     */
    public static void runWithoutListeners(JScrollBar component, Consumer<JScrollBar> action) {
        runWithoutListeners(component, action, AdjustmentListener.class,
            component::removeAdjustmentListener,
            component::addAdjustmentListener
        );
    }

    /**
     * Führt eine Aktion an einem JSlider aus, ohne die registrierten ChangeListener auszulösen.
     */
    public static void runWithoutListeners(JSlider component, Consumer<JSlider> action) {
        runWithoutListeners(component, action, ChangeListener.class,
            component::removeChangeListener,
            component::addChangeListener
        );
    }

    /**
     * Führt eine Aktion an einem JSpinner aus, ohne die registrierten ChangeListener auszulösen.
     */
    public static void runWithoutListeners(JSpinner component, Consumer<JSpinner> action) {
        runWithoutListeners(component, action, ChangeListener.class,
            component::removeChangeListener,
            component::addChangeListener
        );
    }

    /**
     * Generische Implementierung zum temporären Entfernen und Wiederherstellen von Listenern.
     *
     * @param component      Komponente, auf der die Aktion ausgeführt wird
     * @param action         Aktion, die ausgeführt werden soll
     * @param listenerType   Klasse des Listeners
     * @param removeListener Funktion zum Entfernen eines Listeners
     * @param addListener    Funktion zum Hinzufügen eines Listeners
     * @param <E>            Typ der Komponente
     * @param <L>            Typ des Listeners
     */
    private static <E extends Component, L extends EventListener> void runWithoutListeners(
        E component, Consumer<E> action, Class<L> listenerType,
        Consumer<L> removeListener, Consumer<L> addListener) {

        L[] listeners = component.getListeners(listenerType);
        for (L listener : listeners) removeListener.accept(listener);
        try {
            action.accept(component);
        } finally {
            for (L listener : listeners) addListener.accept(listener);
        }
    }

    // ========================================
    // Komponenten Ermittlung
    // ========================================

    /**
     * Gibt die direkten Kind-Komponenten eines Containers zurück.
     *
     * @param container Container, aus dem Komponenten geholt werden sollen
     * @return Array der Kind-Komponenten; leer, falls null
     */
    public static Component[] getComponents(Container container) {
        if (container == null) return new Component[0];
        else if (container instanceof JFrame frame) return frame.getContentPane().getComponents();
        else if (container instanceof JDialog dialog) return dialog.getContentPane().getComponents();
        else if (container instanceof JWindow window) return window.getOwnedWindows();
        else if (container instanceof JMenu menu) return menu.getMenuComponents();
        else if (container instanceof JScrollPane scrollPane) return scrollPane.getViewport().getComponents();
        else return container.getComponents();
    }

    // ========================================
    // Komponenten Hilfsmethoden
    // ========================================

    /**
     * Aktiviert oder deaktiviert alle Kind-Komponenten rekursiv.
     *
     * @param root    Container, deren Kind-Komponenten gesetzt werden
     * @param enabled {@code true}, wenn aktiviert, false, wenn deaktiviert
     */
    public static void setEnabled(Container root, boolean enabled) {
        if (root == null) return;

        for (Component child : getComponents(root)) {
            child.setEnabled(enabled);
            if (child instanceof Container container) {
                setEnabled(container, enabled);
            }
        }
    }

    /**
     * Findet alle Komponenten eines bestimmten Typs rekursiv in einem Container.
     *
     * @param type Klasse der gesuchten Komponente
     * @param root Wurzel-Komponente, ab der gesucht wird
     * @param <T>  Typ der Komponente
     * @return Liste der gefundenen Komponenten
     */
    public static <T extends Component> List<T> findComponentsRecursively(Class<T> type, Component root) {
        List<T> result = new ArrayList<>();

        // Prüfe die Wurzel selbst
        if (type.isInstance(root)) {
            result.add(type.cast(root));
        }

        // Wenn die Wurzel ein Container ist, werden alle Kinder durchsucht
        if (root instanceof Container container) {
            for (Component child : ComponentUtils.getComponents(container)) {
                result.addAll(findComponentsRecursively(type, child));
            }
        }

        return result;
    }

    /**
     * Sucht rekursiv die erste Komponente eines bestimmten Typs in einem Container.
     * Die Suche erfolgt in Tiefe (Depth-First) und bricht sofort ab,
     * sobald eine passende Komponente gefunden wurde.
     *
     * @param type Klasse der gesuchten Komponente
     * @param root Wurzel-Komponente, ab der gesucht wird
     * @param <T>  Typ der Komponente
     * @return Erste gefundene Komponente des angegebenen Typs oder {@code null}, falls keine existiert
     */
    public static <T extends Component> T findFirstComponentRecursively(Class<T> type, Component root) {
        // Prüfe die Wurzel selbst
        if (type.isInstance(root)) {
            return type.cast(root);
        }

        // Wenn die Wurzel ein Container ist, werden alle Kinder durchsucht
        if (root instanceof Container container) {
            for (Component child : ComponentUtils.getComponents(container)) {
                T found = findFirstComponentRecursively(type, child);
                if (found != null) return found;
            }
        }

        // Nichts gefunden
        return null;
    }

    /**
     * Gleicht die Breite aller Komponenten eines bestimmten Typs in einem Container an.
     *
     * @param root  Container, dessen Komponenten angepasst werden
     * @param clazz Klasse der Komponenten, die angepasst werden sollen
     * @param <T>   Typ der Komponente
     */
    public static <T extends Component> void equalizeComponentSizes(Container root, Class<T> clazz) {
        int maxWidth = 0;

        // Maximale Breite bestimmen
        for (Component child : ComponentUtils.getComponents(root)) {
            if (Objects.equals(child.getClass(), clazz)) {
                Dimension pref = child.getPreferredSize();
                maxWidth = Math.max(maxWidth, pref.width);
            }
        }

        // Einheitliche Größe setzen
        for (Component child : ComponentUtils.getComponents(root)) {
            if (Objects.equals(child.getClass(), clazz)) {
                Dimension size = child.getPreferredSize();
                child.setPreferredSize(new Dimension(maxWidth, size.height));
            }
        }
    }

    // ========================================
    // GridBag Layout Methoden
    // ========================================

    /**
     * Erstellt Standard-GridBagConstraints für west-gerichtete, horizontal skalierende Komponenten.
     *
     * @return neue GridBagConstraints
     */
    public static GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    /**
     * Fügt eine beschriftete Komponente als neue Zeile in ein GridBagLayout ein.
     *
     * @param container Container, in den eingefügt wird
     * @param gbc       GridBagConstraints
     * @param labelText Text für das JLabel
     * @param component Komponente, die eingefügt wird
     * @param topInset  Oberer Abstand
     */
    public static void addLabeledRow(Container container, GridBagConstraints gbc, String labelText, JComponent component, int topInset) {
        addLabeledRow(container, gbc, new JLabel(labelText + ":"), component, topInset);
    }

    /**
     * Fügt eine beschriftete Komponente als neue Zeile in ein GridBagLayout ein.
     *
     * @param container Container, in den eingefügt wird
     * @param gbc       GridBagConstraints
     * @param label     JLabel für die Zeile
     * @param component Komponente, die eingefügt wird
     * @param topInset  Oberer Abstand
     */
    public static void addLabeledRow(Container container, GridBagConstraints gbc, JLabel label, JComponent component, int topInset) {
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

    /**
     * Fügt eine Komponente als neue Zeile in ein GridBagLayout ein.
     *
     * @param container Container, in den eingefügt wird
     * @param gbc       GridBagConstraints
     * @param component Komponente, die eingefügt wird
     * @param topInset  Oberer Abstand
     */
    public static void addRow(Container container, GridBagConstraints gbc, JComponent component, int topInset) {
        gbc.gridx = 0;
        gbc.insets.top = topInset;
        if (component != null) container.add(component, gbc);

        gbc.gridy++;
    }
}
