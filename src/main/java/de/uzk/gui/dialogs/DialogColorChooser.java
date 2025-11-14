package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static de.uzk.Main.settings;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogColorChooser {
    // Dialoge
    private final JDialog dialog;
    private JColorChooser colorChooser;

    // Farben
    private final List<FavoriteColorButton> favoriteColors;
    private Color initialColor;
    private Color selectedColor;

    // Favoriten Konstante
    private static final int MAX_FAVORITES = 10;

    public DialogColorChooser(JFrame frame) {
        this.dialog = new JDialog(frame, true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("closed");
                closeDialog(initialColor);
            }
        });

        // Favoriten Lister initialisieren
        this.favoriteColors = new ArrayList<>();

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> closeDialog(this.initialColor),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void closeDialog(Color color) {
        this.selectedColor = color;
        this.colorChooser.setColor(color);
        this.dialog.dispose();
    }

    public Color chooseColor(Color intialColor) {
        this.dialog.setTitle(getWord("dialog.colorChooser"));
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Initialfarbe setzen
        this.initialColor = intialColor;
        this.selectedColor = intialColor;

        // ColorChooser erstellen
        this.colorChooser = new JColorChooser(intialColor);
        this.colorChooser.setPreviewPanel(new JPanel());
        configureColorChooserPanels(this.colorChooser);
        configureColorChooserUI(this.colorChooser);

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBorder(GuiUtils.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createFavoritesPanel(), BorderLayout.NORTH);
        contentPanel.add(createPreviewPanel(), BorderLayout.CENTER);
        contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

        this.dialog.add(contentPanel, BorderLayout.CENTER);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);

        // Farbe zurückgeben
        return this.selectedColor;
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createFavoritesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(getWord("label.favorites")),
            GuiUtils.BORDER_EMPTY_SMALL
        ));
        panel.setOpaque(false);

        // Plus Button
        FavoriteColorButton addButton = new FavoriteColorButton(null, true);
        addButton.addClickedListener(e -> {
            if (!SwingUtilities.isLeftMouseButton(e)) return;

            Color currentColor = this.colorChooser.getColor();
            FavoriteColorButton colorButton = createColorButton(currentColor, panel);

            // Nach dem Plus einfügen
            panel.add(colorButton, 1);
            this.favoriteColors.add(0, colorButton);
            if (this.favoriteColors.size() > MAX_FAVORITES) {
                FavoriteColorButton lastButton = this.favoriteColors.get(this.favoriteColors.size() - 1);
                panel.remove(lastButton);
                this.favoriteColors.remove(lastButton);
            }

            // Revalidierung
            panel.revalidate();
            panel.repaint();
        });
        panel.add(addButton);

        // Favoriten Panel füllen
        this.favoriteColors.forEach(button -> panel.add(createColorButton(button.color, panel)));

        return panel;
    }

    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(getWord("label.preview")),
            GuiUtils.BORDER_EMPTY_SMALL
        ));

        // Layout Manager
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;

        // ---- Linker Bereich: 3 Labels ----
        JLabel label1 = createSampleTextLabel(null, this.initialColor);
        JLabel label2 = createSampleTextLabel(this.initialColor, Color.BLACK);
        JLabel label3 = createSampleTextLabel(Color.WHITE, this.initialColor);

        ComponentUtils.addRow(panel, gbc, label1, 0);
        ComponentUtils.addRow(panel, gbc, label2, 5);
        ComponentUtils.addRow(panel, gbc, label3, 5);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.insets = new Insets(0, 10, 0, 0);

        // ---- Linker Bereich (oben): 2 Farbfelder ----
        JLabel currentColorField = createColorField(this.initialColor, this.colorChooser::setColor);
        JLabel previousColorField = createColorField(this.initialColor, this.colorChooser::setColor);

        panel.add(currentColorField, gbc);
        gbc.gridx++;
        panel.add(previousColorField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridheight = 1;
        gbc.insets.top = 5;

        // ---- Linker Bereich (unten): 2 Farbfelder Labels ----
        panel.add(createCenteredLabel(getWord("label.current")), gbc);
        gbc.gridx++;
        panel.add(createCenteredLabel(getWord("label.previous")), gbc);

        // ---- Listener für dynamisches Update ----
        this.colorChooser.getSelectionModel().addChangeListener(e -> {
            Color color = colorChooser.getColor();

            // Labels aktualisieren
            label1.setForeground(color);
            label2.setBackground(color);
            label3.setForeground(color);

            // Aktuelles Farbfeld aktualisieren
            boolean lightBackground = ColorUtils.calculatePerceivedBrightness(color) > 0.5;

            currentColorField.setForeground(lightBackground ? Color.BLACK : Color.WHITE);
            currentColorField.setBackground(color);
            currentColorField.setText(ColorUtils.colorToHex(color, true));

            // Revalidierung
            panel.revalidate();
            panel.repaint();
        });

        // ---- Vorschau Panel erstellen ----
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(this.colorChooser, BorderLayout.CENTER);
        previewPanel.add(panel, BorderLayout.SOUTH);
        return previewPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Schaltfläche (Zurücksetzen)
        JButton resetButton = new JButton(getWord("button.reset"));
        resetButton.addActionListener(e -> this.colorChooser.setColor(this.initialColor));
        panel.add(resetButton);

        // Schaltfläche (Abbrechen)
        JButton cancelButton = new JButton(getWord("button.cancel"));
        cancelButton.addActionListener(e -> closeDialog(this.initialColor));
        panel.add(cancelButton);

        // Schaltfläche (OK)
        JButton okButton = new JButton(getWord("button.ok"));
        okButton.addActionListener(e -> closeDialog(this.colorChooser.getColor()));
        panel.add(okButton);

        // Gleicht die Größen aller Buttons an
        ComponentUtils.equalizeComponentSizes(panel, JButton.class);

        // Den OK-Button als Default-Button setzen
        this.dialog.getRootPane().setDefaultButton(okButton);

        return panel;
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private static void configureColorChooserPanels(JColorChooser colorChooser) {
        Stream<AbstractColorChooserPanel> panels = Arrays.stream(colorChooser.getChooserPanels())
            .filter(panel -> !"swatches rgb".contains(panel.getDisplayName().toLowerCase()));

        // Wenn Swatches oder RGB-Panel vorhanden ist, alle anderen Panels entfernen
        panels.iterator().forEachRemaining(colorChooser::removeChooserPanel);
    }

    private static void configureColorChooserUI(JColorChooser root) {
        // Finde alle Komponenten (rekursiv) innerhalb des ColorChoosers
        List<JComponent> components = ComponentUtils.findComponentsRecursively(JComponent.class, root);

        // Durchlaufe alle gefundenen Komponenten
        for (int i = 0; i < components.size(); i++) {
            JComponent component = components.get(i);
            JComponent nextComponent = i == components.size() - 1 ? null : components.get(i + 1);

            // Typname der Diagramm-Komponente aus der ColorChooser-API
            String diagramComponentName = "javax.swing.colorchooser.DiagramComponent";
            String typeName = component.getClass().getTypeName();

            // Prüfe, ob es sich um eine DiagramComponent handelt
            if (diagramComponentName.equalsIgnoreCase(typeName)) {
                // Deaktiviere Rundungen der Diagramm-Komponente (sieht sonst komisch aus)
                component.putClientProperty("JComponent.roundRect", false);
            } else if (component instanceof JLabel colorCodeLabel &&
                nextComponent instanceof JFormattedTextField hexColorTextField) {
                // Wenn ein JLabel ("Farbcode") direkt vor einem Textfeld steht, konfiguriere es neu
                colorCodeLabel.setText(getWord("label.colorCode"));
                colorCodeLabel.setDisplayedMnemonicIndex(0);

                // Stelle sicher, dass Label und Textfeld sichtbar sind (sind sonst nicht sichtbar)
                colorCodeLabel.setVisible(true);
                hexColorTextField.setVisible(true);
            }
        }
    }

    private static JLabel createSampleTextLabel(Color background, Color foreground) {
        JLabel label = new JLabel(getWord("label.sampleText"));
        label.setBorder(GuiUtils.BORDER_EMPTY_TINY);
        if (background != null) {
            label.setOpaque(true);
            label.setBackground(background);
        }
        if (foreground != null) {
            label.setForeground(foreground);
        }
        return label;
    }

    private static JLabel createCenteredLabel(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private static JLabel createColorField(Color background, Consumer<Color> clicked) {
        JLabel colorField = new JLabel();
        colorField.setHorizontalAlignment(SwingConstants.CENTER);
        colorField.setBorder(GuiUtils.BORDER_EMPTY_DEFAULT);
        colorField.setPreferredSize(new Dimension(100, 0));
        colorField.setOpaque(true);
        colorField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                clicked.accept(colorField.getBackground());
            }
        });

        // Aktuelles Farbfeld aktualisieren
        boolean lightBackground = ColorUtils.calculatePerceivedBrightness(background) > 0.5;
        colorField.setForeground(lightBackground ? Color.BLACK : Color.WHITE);
        colorField.setBackground(background);
        colorField.setText(ColorUtils.colorToHex(background, true));

        return colorField;
    }

    private FavoriteColorButton createColorButton(Color color, JPanel favoritesPanel) {
        FavoriteColorButton button = new FavoriteColorButton(color, false);
        button.addClickedListener(e -> {
            if (SwingUtilities.isLeftMouseButton(e)) {
                this.colorChooser.setColor(color);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                button.showPopupMenu(e, () -> {
                    favoritesPanel.remove(button);
                    this.favoriteColors.remove(button);

                    // Revalidierung
                    favoritesPanel.revalidate();
                    favoritesPanel.repaint();
                });
            }
        });

        return button;
    }

    // ========================================
    // Innere Klasse: Renderer für runde Farbbuttons
    // ========================================
    private static class FavoriteColorButton extends JComponent {
        private static final int DIAMETER = 20;

        // Gui Elemente
        private final Color color;
        private final boolean isAddButton;
        private boolean hover;

        public FavoriteColorButton(Color color, boolean isAddButton) {
            this.color = color;
            this.isAddButton = isAddButton;
            setPreferredSize(new Dimension(DIAMETER, DIAMETER));

            // Mausverhalten
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    GuiUtils.setToolTipText(FavoriteColorButton.this, isAddButton ? getWord("label.favorite.add") : null);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    GuiUtils.setToolTipText(FavoriteColorButton.this, null);
                    repaint();
                }
            });
        }

        public void addClickedListener(Consumer<MouseEvent> clicked) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    clicked.accept(e);
                }
            });
        }

        public void showPopupMenu(MouseEvent e, Runnable removeListener) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem delete = new JMenuItem(getWord("label.favorite.remove"));
            delete.addActionListener(ev -> removeListener.run());
            menu.add(delete);
            menu.show(this, e.getX(), e.getY());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = GuiUtils.createHighQualityGraphics2D(g);
            int size = Math.min(getWidth(), getHeight());
            int borderThickness = 1; // 1px Padding für dickeren Rand
            int diameter = size - borderThickness * 2;
            int x = 1;
            int y = 1;

            // Statusfarben
            boolean light = settings.getTheme().isLight();
            Color border = GuiUtils.adjustColor((light ? Color.BLACK : Color.WHITE), this.hover ? 0.5f : 0, light);
            Color background = isAddButton ? (light ? Color.WHITE : Color.BLACK) : color;

            // Kreis füllen
            g2.setColor(background);
            g2.fillOval(x, y, diameter, diameter);

            // Rahmen zeichnen
            g2.setStroke(new BasicStroke((float) borderThickness));
            g2.setColor(border);
            g2.drawOval(x, y, diameter, diameter);

            // Plus-Zeichen zeichnen
            if (this.isAddButton) {
                int center = size / 2;
                int addRadius = (int) (diameter / 4.0);

                g2.setStroke(new BasicStroke(1f));
                g2.setColor(border);
                g2.drawLine(center, center - addRadius, center, center + addRadius);
                g2.drawLine(center - addRadius, center, center + addRadius, center);
            }

            g2.dispose();
        }
    }
}