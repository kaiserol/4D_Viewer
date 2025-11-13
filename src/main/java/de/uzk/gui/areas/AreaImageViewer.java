package de.uzk.gui.areas;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.Icons;
import de.uzk.image.Axis;
import de.uzk.markers.Marker;
import de.uzk.utils.ComponentUtils;
import de.uzk.utils.SnapshotHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class AreaImageViewer extends AreaContainerInteractive<JPanel> {
    // Gui Elemente
    private JPanel panelView, panelImage;
    private JScrollBar scrollBarTime, scrollBarLevel;
    private BufferedImage currentImage;
    private int insetX, insetY;

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

        DragListener dl = new DragListener();
        this.container.addMouseListener(dl);
        this.container.addMouseMotionListener(dl);

        // 1. Kopfbereich mit Statusinformationen hinzufügen
        JPanel statsBarPanel = new AreaStatsBar(this.gui).getContainer();
        this.container.add(statsBarPanel, BorderLayout.NORTH);

        // 2. Bildbereich mit Scrollbars hinzufügen
        this.panelView = new JPanel(new BorderLayout());
        this.panelImage = initImagePanel();
        this.scrollBarTime = ComponentUtils.createScrollBar(Adjustable.HORIZONTAL, newValue ->
            handleScrollAction(newValue, Axis.TIME, this.scrollBarTime));
        this.scrollBarLevel = ComponentUtils.createScrollBar(Adjustable.VERTICAL, newValue ->
            handleScrollAction(newValue, Axis.LEVEL, this.scrollBarLevel));

        int scrollBarWidth = UIManager.getInt("ScrollBar.width");
        this.panelView.add(this.panelImage, BorderLayout.CENTER);
        this.panelView.add(createRightSpace(this.scrollBarTime, scrollBarWidth), BorderLayout.SOUTH);
        this.panelView.add(this.scrollBarLevel, BorderLayout.EAST);

        this.container.add(this.panelView, BorderLayout.CENTER);
        this.container.setMinimumSize(new Dimension(scrollBarWidth * 3, scrollBarWidth * 3));
    }

    // ========================================
    // Innere Klassen
    // ========================================
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

    private class FocusMouseListener extends MouseAdapter {
        // Listener für Fokusaktivierung
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!container.isFocusOwner()) container.requestFocusInWindow();
        }
    }

    private class DragListener extends MouseAdapter {
        private Point start;

        @Override
        public void mousePressed(MouseEvent e) {
            this.start = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (this.start != null) {
                insetX = this.start.x - e.getX();
                insetY = this.start.y - e.getY();
                container.repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            this.start = null;
        }
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
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

    // ========================================
    // Bild zeichnen
    // ========================================
    private JPanel initImagePanel() {
        // Erstellt das Panel mit Bildanzeige
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintImage(g);
            }
        };
    }

    private void paintImage(Graphics g) {
        // Zeichnet das aktuelle Bild und Marker
        Graphics2D g2D = GuiUtils.createHighQualityGraphics2D(g);
        if (this.currentImage != null) {
            double zoomPercentage = workspace.getConfig().getZoom() / 100.0;
            double scale = GuiUtils.getImageScaleFactor(this.currentImage, this.panelImage) * zoomPercentage;
            int width = (int) (this.currentImage.getWidth() * scale);
            int height = (int) (this.currentImage.getHeight() * scale);
            int x = (this.panelImage.getWidth() - width) / 2;
            int y = (this.panelImage.getHeight() - height) / 2;

            // Bild zeichnen
            g2D.drawImage(this.currentImage, x - this.insetX, y - this.insetY, width, height, null);
        } else {
            // Eine Fehlermeldung wird angezeigt, wenn das aktuelle Bild nicht geladen werden kann (weil es nicht existiert)
            String text = workspace.getImagesDirectory() != null ? getWord("dialog.loadingImages.imageCouldNotLoad") : "";
            GuiUtils.drawCenteredText(g2D, text, this.panelImage);
        }
    }

    // ========================================
    // Observer Methoden
    // ========================================
    @Override
    public void handleAction(ActionType actionType) {
        switch (actionType) {
            case ACTION_EDIT_IMAGE, ACTION_ADD_MARKER, ACTION_REMOVE_MARKER -> updateCurrentImage();
            case SHORTCUT_TAKE_SNAPSHOT -> {
                if (SnapshotHelper.saveSnapshot(this.currentImage)) {
                    gui.handleAction(ActionType.ACTION_UPDATE_SNAPSHOT_COUNTER);
                }
            }
        }
    }

    @Override
    public void toggleOn() {
        // Komponenten aktivieren
        ComponentUtils.setEnabled(this.container, true);

        updateCurrentImage();
        updateScrollBarValuesSecurely(this.scrollBarTime, workspace.getTime(), workspace.getMaxTime());
        updateScrollBarValuesSecurely(this.scrollBarLevel, workspace.getLevel(), workspace.getMaxLevel());
    }


    @Override
    public void toggleOff() {
        // Komponenten deaktivieren
        ComponentUtils.setEnabled(this.container, false);

        updateCurrentImage();
        updateScrollBarValuesSecurely(this.scrollBarTime, 0, 0);
        updateScrollBarValuesSecurely(this.scrollBarLevel, 0, 0);
    }

    @Override
    public void update(Axis axis) {
        updateCurrentImage();
        switch (axis) {
            case TIME -> ComponentUtils.setValueSecurely(this.scrollBarTime, workspace.getTime());
            case LEVEL -> ComponentUtils.setValueSecurely(this.scrollBarLevel, workspace.getLevel());
        }
    }

    @Override
    public void updateTheme() {
        setBorder(this.container.hasFocus());
    }

    // ========================================
    // Aktualisierungen
    // ========================================
    private void handleScrollAction(int newValue, Axis axis, JScrollBar scrollBar) {
        // Wenn sich der Wert nicht ändert, abbrechen
        int oldValue = (axis == Axis.TIME) ? workspace.getTime() : workspace.getLevel();
        if (oldValue == newValue) return;

        // Richtung berechnen: > 0: vorwärts, < 0: rückwärts
        int rotation = newValue - oldValue;
        gui.getActionHandler().scroll(axis, rotation, scrollBar.getValueIsAdjusting());
    }

    private void updateCurrentImage() {
        // Bild neu laden
        this.currentImage = null;
        if (workspace.isLoaded()) {
            Path imagePath = workspace.getCurrentImageFile().getFilePath();
            BufferedImage originalImage = Icons.loadImage(imagePath, false);
            List<Marker> markers = workspace.getMarkers().getMarkersForImage(workspace.getTime());
            if (originalImage != null) {
//                long t = System.nanoTime();
                this.currentImage = GuiUtils.getEditedImage(originalImage, true, markers);
//                long dt = System.nanoTime() - t;
                // TODO: debug auskommentieren wenn du es wieder brauchst
//                logger.debug(String.format("Edited image in %,d ns", dt));
            }
        }

        // Bild zeichnen
        this.container.repaint();
    }

    private void updateScrollBarValuesSecurely(JScrollBar scrollBar, int value, int max) {
        if (scrollBar.getValueIsAdjusting()) return;
        if (scrollBar.getValue() == value && scrollBar.getMaximum() == max) return;
        ComponentUtils.runWithoutListeners(scrollBar, sb -> sb.setValues(value, 0, 0, max));
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private void setBorder(boolean focusedPanel) {
        Color borderColor = focusedPanel ? GuiUtils.COLOR_BLUE : GuiUtils.getBorderColor();
        this.container.setBorder(BorderFactory.createLineBorder(borderColor));
        this.panelView.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
    }
}
