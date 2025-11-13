package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogColorChooser {
    // Dialoge
    private final JDialog dialog;
    private final JColorChooser colorChooser;

    // Zustand
    private Color initialColor;
    private Color selectedColor;

    public DialogColorChooser(JFrame frame) {
        this.dialog = new JDialog(frame, true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.colorChooser = new JColorChooser();

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public Color chooseColor(Color selectedColor) {
        this.dialog.setTitle(getWord("dialog.colorChooser"));
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Initialfarbe setzen
        this.initialColor = selectedColor;
        this.selectedColor = selectedColor;
        this.colorChooser.setColor(selectedColor);

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBorder(GuiUtils.BORDER_EMPTY_DEFAULT);
        contentPanel.add(this.colorChooser, BorderLayout.CENTER);
        contentPanel.add(createColorChooserButtonsPanel(), BorderLayout.SOUTH);

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
    private JPanel createColorChooserButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Schaltfläche (Zurücksetzen)
        JButton resetButton = new JButton(getWord("button.reset"));
        resetButton.addActionListener(e -> this.colorChooser.setColor(this.initialColor));
        panel.add(resetButton);

        // Schaltfläche (Abbrechen)
        JButton cancelButton = new JButton(getWord("button.cancel"));
        cancelButton.addActionListener(e -> this.dialog.dispose());
        panel.add(cancelButton);

        // Schaltfläche (OK)
        JButton okButton = new JButton(getWord("button.ok"));
        okButton.addActionListener(e -> {
            this.selectedColor = this.colorChooser.getColor();
            this.dialog.dispose();
        });
        panel.add(okButton);

        // Gleicht die Größen aller Buttons an
        ComponentUtils.equalizeComponentSizes(panel, JButton.class);

        // Den OK-Button als Default-Button setzen
        this.dialog.getRootPane().setDefaultButton(okButton);

        return panel;
    }
}