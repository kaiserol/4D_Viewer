package de.uzk.gui.areas;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.Icons;
import de.uzk.image.Axis;
import de.uzk.markers.Marker;
import de.uzk.utils.SnapshotHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class AreaImageViewer extends AreaContainerInteractive<JPanel> implements MouseMotionListener {
    // GUI-Elemente
    private JPanel panelView;
    private JPanel panelImage;
    private BufferedImage currentImage;
    private JScrollBar scrollBarTime;
    private JScrollBar scrollBarLevel;
    private int insetX;
    private int insetY;

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
        this.scrollBarTime = initScrollBar(Adjustable.HORIZONTAL, Axis.TIME);
        this.scrollBarLevel = initScrollBar(Adjustable.VERTICAL, Axis.LEVEL);

        int scrollBarWidth = UIManager.getInt("ScrollBar.width");
        this.panelView.add(this.panelImage, BorderLayout.CENTER);
        this.panelView.add(insertRightSpace(this.scrollBarTime, scrollBarWidth), BorderLayout.SOUTH);
        this.panelView.add(this.scrollBarLevel, BorderLayout.EAST);

        this.container.add(this.panelView, BorderLayout.CENTER);
        this.container.setMinimumSize(new Dimension(scrollBarWidth * 3, scrollBarWidth * 3));
    }

    private JScrollBar initScrollBar(int orientation, Axis axis) {
        @SuppressWarnings("MagicConstant")
        JScrollBar scrollBar = new JScrollBar(orientation);
        scrollBar.addAdjustmentListener(e -> {
            // Abbrechen, wenn der Wert sich nicht geändert hat
            int oldValue = (axis == Axis.TIME) ? workspace.getTime() : workspace.getLevel();
            int newValue = e.getValue();
            if (oldValue == newValue) return;

            // Richtung berechnen: > 0: vorwärts, < 0: rückwärts
            int rotation = newValue - oldValue;
            gui.getActionHandler().scroll(axis, rotation, e.getValueIsAdjusting());
        });
        scrollBar.setBlockIncrement(1);
        scrollBar.setUnitIncrement(1);
        return scrollBar;
    }

    private JPanel insertRightSpace(JComponent component, int size) {
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

    @Override
    public void mouseDragged(MouseEvent e) {
        this.insetX = e.getX();
        this.insetY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    // ========================================
    // Innere Klassen
    // ========================================

    // Listener für Fokusänderungen
    private class FocusBorderListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            setFocusBorder(true);
        }

        @Override
        public void focusLost(FocusEvent e) {
            setFocusBorder(false);
        }
    }

    // Listener für Fokusaktivierung
    private class FocusMouseListener extends MouseAdapter {
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
    // Bild zeichnen
    // ========================================

    // Erstellt das Panel mit Bildanzeige
    private JPanel initImagePanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintImage(g);
            }
        };
    }

    // Zeichnet das aktuelle Bild und Marker
    private void paintImage(Graphics g) {
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
            String text = workspace.getImagesDirectory() != null ? getWord("placeholder.imageCouldNotLoad") : "";
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
            case ACTION_TAKE_SNAPSHOT -> {
                if (SnapshotHelper.saveSnapshot(this.currentImage)) {
                    gui.handleAction(ActionType.ACTION_UPDATE_SNAPSHOT_COUNTER);
                }
            }
        }
    }

    @Override
    public void toggleOn() {
        // Komponenten aktivieren
        GuiUtils.setEnabled(this.container, true);

        updateCurrentImage();
        setScrollBarValues(this.scrollBarTime, workspace.getTime(), workspace.getMaxTime());
        setScrollBarValues(this.scrollBarLevel, workspace.getLevel(), workspace.getMaxLevel());
    }


    @Override
    public void toggleOff() {
        // Komponenten deaktivieren
        GuiUtils.setEnabled(this.container, false);

        updateCurrentImage();
        setScrollBarValues(this.scrollBarTime, 0, 0);
        setScrollBarValues(this.scrollBarLevel, 0, 0);
    }

    @Override
    public void update(Axis axis) {
        updateCurrentImage();
        switch (axis) {
            case TIME -> setScrollBarValue(this.scrollBarTime, workspace.getTime());
            case LEVEL -> setScrollBarValue(this.scrollBarLevel, workspace.getLevel());
        }
    }

    @Override
    public void updateTheme() {
        setFocusBorder(this.container.hasFocus());
    }


    // ========================================
    // Hilfsmethoden
    // ========================================

    // Färbt den Rahmen bei Fokus
    private void setFocusBorder(boolean focus) {
        Color borderColor = focus ? GuiUtils.COLOR_BLUE : GuiUtils.getBorderColor();
        this.container.setBorder(BorderFactory.createLineBorder(borderColor));
        this.panelView.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
    }

    private void updateCurrentImage() {
        // Bild neu laden
        this.currentImage = null;
        if (workspace.isOpen()) {
            Path imagePath = workspace.getImageFile().getFilePath();
            BufferedImage originalImage = Icons.loadImage(imagePath, false);
            List<Marker> markers = workspace.getMarkers().getMarkersForImage(workspace.getTime());
            if (originalImage != null) {
                long t = System.nanoTime();
                this.currentImage = GuiUtils.getEditedImage(originalImage, true, markers);
                long dt = System.nanoTime() - t;
                logger.debug(String.format("Edited image in %,d ns", dt));
            }
        }

        // Bild zeichnen
        this.container.repaint();
    }

    private void setScrollBarValues(JScrollBar scrollBar, int value, int max) {
        if (scrollBar.getValueIsAdjusting()) return;
        if (scrollBar.getValue() == value && scrollBar.getMaximum() == max) return;
        GuiUtils.runWithoutAdjustmentEvents(scrollBar, () -> scrollBar.setValues(value, 0, 0, max));
    }

    private void setScrollBarValue(JScrollBar scrollBar, int value) {
        if (scrollBar.getValueIsAdjusting()) return;
        if (scrollBar.getValue() == value) return;
        GuiUtils.runWithoutAdjustmentEvents(scrollBar, () -> scrollBar.setValue(value));
    }
}
