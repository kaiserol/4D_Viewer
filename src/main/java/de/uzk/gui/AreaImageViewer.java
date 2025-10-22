package de.uzk.gui;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.config.ScreenshotHelper;
import de.uzk.image.Axis;
import de.uzk.markers.MarkerMapping;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public class AreaImageViewer extends AreaContainerInteractive<JPanel> {
    private final ActionHandler actionHandler;

    // Bildanzeige
    private JPanel panelView;
    private JPanel panelImage;
    private BufferedImage originalImage;
    private BufferedImage currentImage;

    // GUI-Elemente
    private JScrollBar scrollBarTime;
    private JScrollBar scrollBarLevel;

    public AreaImageViewer(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
        init();
    }

    private void init() {
        this.container.setFocusable(true);
        this.container.setLayout(new BorderLayout());
        this.container.addFocusListener(new FocusBorderListener());
        this.container.addMouseListener(new FocusMouseListener());
        this.container.addMouseWheelListener(this.actionHandler);
        this.container.addKeyListener(this.actionHandler);

        // === 1. Kopfbereich mit Statusinformationen ===
        JPanel statsBarPanel = new AreaStatsBar(this.gui).getContainer();
        this.container.add(statsBarPanel, BorderLayout.NORTH);

        // === 2. Bildbereich mit Scrollbars ===
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

    // Färbt den Rahmen bei Fokus
    private void setFocusBorder(boolean focus) {
        Color color = focus ? GuiUtils.COLOR_BLUE : GuiUtils.getBorderColor();
        this.container.setBorder(new MatteBorder(1, 1, 1, 1, color));
        this.panelView.setBorder(new MatteBorder(1, 0, 0, 0, color));
    }

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
            double scale = GuiUtils.getImageScaleFactor(this.currentImage, this.panelImage);
            int width = (int) (this.currentImage.getWidth() * scale);
            int height = (int) (this.currentImage.getHeight() * scale);
            int x = (this.panelImage.getWidth() - width) / 2;
            int y = (this.panelImage.getHeight() - height) / 2;
            g2D.drawImage(this.currentImage, x, y, width, height, null);

            // Zeichnet Marker
            List<MarkerMapping> marker = markerHandler.getMarkers(imageFileHandler.getTime());
            for (MarkerMapping m : marker) {
                m.getMarker().draw(g2D, new Rectangle(x, y, width, height), scale);
            }
        } else {
            // Wenn das Bild nicht geladen werden konnte, zeigt es eine Fehlermeldung an
            String text = imageFileHandler == null ? "" : getWord("placeholder.imageCouldNotLoad");
            GuiUtils.drawCenteredText(g2D, text, this.panelImage);
        }
    }

    private JScrollBar initScrollBar(int orientation, Axis axis) {
        @SuppressWarnings("MagicConstant")
        JScrollBar scrollBar = new JScrollBar(orientation);
        scrollBar.addAdjustmentListener(e -> {
            // Abbrechen, wenn der Wert sich nicht geändert hat
            int oldValue = (axis == Axis.TIME) ? imageFileHandler.getTime() : imageFileHandler.getLevel();
            int newValue = e.getValue();
            if (oldValue == newValue) return;

            // Richtung berechnen: > 0: vorwärts, < 0: rückwärts
            int rotation = newValue - oldValue;
            actionHandler.scroll(axis, rotation, e.getValueIsAdjusting());
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

    // ==========================================================
    // Überschreibungen
    // ==========================================================
    @Override
    public void handleAction(ActionType actionType) {
        switch (actionType) {
            case ACTION_EDIT_IMAGE -> updateCurrentImage();
            case SHORTCUT_TAKE_SCREENSHOT -> {
                if (originalImage != null && ScreenshotHelper.saveScreenshot(this.originalImage)) {
                    gui.handleAction(ActionType.ACTION_UPDATE_SCREENSHOT_COUNTER);
                }
            }
        }
    }

    @Override
    public void toggleOn() {
        // Komponenten aktivieren
        GuiUtils.setEnabled(this.container, true);

        updateCurrentImage();
        setScrollBarValues(this.scrollBarTime, imageFileHandler.getTime(), imageFileHandler.getMaxTime());
        setScrollBarValues(this.scrollBarLevel, imageFileHandler.getLevel(), imageFileHandler.getMaxLevel());
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
            case TIME -> setScrollBarValue(this.scrollBarTime, imageFileHandler.getTime());
            case LEVEL -> setScrollBarValue(this.scrollBarLevel, imageFileHandler.getLevel());
        }
    }

    @Override
    public void updateTheme() {
        setFocusBorder(this.container.isFocusOwner());
    }

    // ==========================================================
    // Hilfsfunktionen
    // ==========================================================
    private void updateCurrentImage() {
        File file = imageFileHandler != null ? imageFileHandler.getImageFile() != null ? imageFileHandler.getImageFile().getFile() : null: null;
        this.currentImage = this.originalImage = (file != null ? Icons.loadImage(file, false) : null);
        if (this.originalImage != null) this.currentImage = GuiUtils.getEditedImage(this.originalImage, true);
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
