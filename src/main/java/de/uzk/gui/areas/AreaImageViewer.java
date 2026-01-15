package de.uzk.gui.areas;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.SensitiveImagePanel;
import de.uzk.gui.UIEnvironment;
import de.uzk.gui.observer.ObserverContainer;
import de.uzk.image.Axis;
import de.uzk.image.ImageEditor;
import de.uzk.io.SnapshotHelper;
import de.uzk.markers.MarkerInteractionHandler;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static de.uzk.Main.workspace;

public class AreaImageViewer extends ObserverContainer<JPanel> {
    // Gui Elemente
    private JPanel panelView;
    private SensitiveImagePanel panelImage;
    private JScrollBar scrollBarTime, scrollBarLevel;

    private ImageEditor imageEditor;
    private MarkerInteractionHandler markerEditor;

    public AreaImageViewer(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setFocusable(true);
        this.container.setLayout(new BorderLayout());
        this.container.addFocusListener(new FocusBorderListener());
        this.container.addMouseListener(new FocusMouseListener());
        this.container.addMouseWheelListener(gui.getActionHandler());
        this.container.addKeyListener(gui.getActionHandler());

        // 1. Kopfbereich mit Statusinformationen hinzufügen
        JPanel statsBarPanel = new AreaStatsBar(this.gui).getContainer();
        this.container.add(statsBarPanel, BorderLayout.NORTH);

        // 2. Bildbereich mit Scrollbars hinzufügen
        this.panelView = new JPanel(new BorderLayout());
        this.panelImage = new SensitiveImagePanel();
        this.scrollBarTime = ComponentUtils.createScrollBar(Adjustable.HORIZONTAL, newValue -> handleScrollAction(newValue, Axis.TIME, this.scrollBarTime));
        this.scrollBarLevel = ComponentUtils.createScrollBar(Adjustable.VERTICAL, newValue -> handleScrollAction(newValue, Axis.LEVEL, this.scrollBarLevel));

        int scrollBarWidth = UIManager.getInt("ScrollBar.width");
        this.panelView.add(this.panelImage, BorderLayout.CENTER);
        this.panelView.add(createRightSpace(this.scrollBarTime, scrollBarWidth), BorderLayout.SOUTH);
        this.panelView.add(this.scrollBarLevel, BorderLayout.EAST);

        this.container.add(this.panelView, BorderLayout.CENTER);
        this.container.setMinimumSize(new Dimension(scrollBarWidth * 3, scrollBarWidth * 3));

        this.imageEditor = new ImageEditor();
        this.imageEditor.onNewImageAvailable(this.panelImage::updateImage);

        this.markerEditor = new MarkerInteractionHandler(this.imageEditor);
        this.panelImage.addMouseListener(this.markerEditor);
        this.panelImage.addMouseMotionListener(this.markerEditor);
    }

    //region Komponenten-Erzeugung
    private JPanel createRightSpace(JComponent component, int size) {
        // labelEmpty
        JLabel labelEmpty = new JLabel();
        labelEmpty.setOpaque(true);
        labelEmpty.setPreferredSize(new Dimension(size, size));

        // panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(component, BorderLayout.CENTER);
        panel.add(labelEmpty, BorderLayout.EAST);
        return panel;
    }

    //region Observer Methoden
    @Override
    public void handleAction(ActionType actionType) {
        switch (actionType) {
            case ACTION_EDIT_IMAGE -> imageEditor.updateImage(true);
            case ACTION_ADD_MARKER, ACTION_EDIT_MARKER, ACTION_REMOVE_MARKER -> imageEditor.updateImage(false);
            case SHORTCUT_TAKE_SNAPSHOT -> {
                if (SnapshotHelper.saveSnapshot(imageEditor.getCurrentImage())) {
                    gui.handleAction(ActionType.ACTION_UPDATE_SNAPSHOT_COUNTER);
                }
            }
            default -> {/* ignorieren */}
        }
    }
    //endregion

    @Override
    public void toggleOn() {
        // Komponenten aktivieren
        ComponentUtils.setEnabled(this.container, true);

        this.imageEditor.updateImage(true);
        updateScrollBarValuesSecurely(this.scrollBarTime, workspace.getTime(), workspace.getMaxTime());
        updateScrollBarValuesSecurely(this.scrollBarLevel, workspace.getLevel(), workspace.getMaxLevel());
    }
    //endregion

    @Override
    public void toggleOff() {
        // Komponenten deaktivieren
        ComponentUtils.setEnabled(this.container, false);

        this.imageEditor.updateImage(true);
        updateScrollBarValuesSecurely(this.scrollBarTime, 0, 0);
        updateScrollBarValuesSecurely(this.scrollBarLevel, 0, 0);
    }

    @Override
    public void update(Axis axis) {
        this.imageEditor.updateImage(true);
        switch (axis) {
            case TIME -> ComponentUtils.setValueSecurely(this.scrollBarTime, workspace.getTime());
            case LEVEL -> ComponentUtils.setValueSecurely(this.scrollBarLevel, workspace.getLevel());
        }
    }

    @Override
    public void updateTheme() {
        setBorder(this.container.hasFocus());
    }

    //region Aktualisierungen
    private void handleScrollAction(int newValue, Axis axis, JScrollBar scrollBar) {
        // Wenn sich der Wert nicht ändert, abbrechen
        int oldValue = (axis == Axis.TIME) ? workspace.getTime() : workspace.getLevel();
        if (oldValue == newValue) return;

        // Richtung berechnen: > 0: vorwärts, < 0: rückwärts
        int rotation = newValue - oldValue;
        gui.getActionHandler().scroll(axis, rotation, scrollBar.getValueIsAdjusting());
    }

    private void updateScrollBarValuesSecurely(JScrollBar scrollBar, int value, int max) {
        if (scrollBar.getValueIsAdjusting()) return;
        if (scrollBar.getValue() == value && scrollBar.getMaximum() == max) return;
        ComponentUtils.runWithoutListeners(scrollBar, sb -> sb.setValues(value, 0, 0, max));
    }

    //endregion

    //region Hilfsmethoden
    private void setBorder(boolean focusedPanel) {
        Color borderColor = focusedPanel ? ColorUtils.COLOR_BLUE : UIEnvironment.getBorderColor();
        this.container.setBorder(BorderFactory.createLineBorder(borderColor));
        this.panelView.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
    }

    //region Innere Klassen
    private class FocusBorderListener implements FocusListener {
        // Listener für Fokusänderungen
        @Override
        public void focusGained(FocusEvent e) {
            setBorder(true);
        }

        @Override
        public void focusLost(FocusEvent e) {
            setBorder(false);
        }
    }

    //endregion

    private class FocusMouseListener extends MouseAdapter {
        // Listener für Fokusaktivierung
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!container.isFocusOwner()) container.requestFocusInWindow();
        }
    }
    //endregion
}
