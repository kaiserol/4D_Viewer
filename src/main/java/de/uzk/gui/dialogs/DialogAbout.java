package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogAbout {
    // GUI-Elemente
    private final JDialog dialog;

    public DialogAbout(JFrame frame) {
        this.dialog = new JDialog(frame, true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.setTitle(getWord("dialog.about") + " " + getWord("app.name"));
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(GuiUtils.BORDER_PADDING_LARGE);
        panel.add(createAboutPanel(), BorderLayout.CENTER);
        this.dialog.add(panel, BorderLayout.CENTER);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();

        JLabel label = new JLabel();
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, gbc);
        return panel;
    }
}
