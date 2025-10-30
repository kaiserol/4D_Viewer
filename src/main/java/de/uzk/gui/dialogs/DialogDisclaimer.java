package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.gui.SelectableText;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogDisclaimer {
    private final JDialog dialog;

    // Maximale Dialogbreite
    private static final int MAX_WIDTH = 500;

    public DialogDisclaimer(JFrame frame) {
        this.dialog = new JDialog(frame, getWord("dialog.disclaimer"), true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.dialog.setResizable(false);

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(createContributorsPanel(), BorderLayout.CENTER); // Mitwirkende und Versionen
        panel.add(createDisclaimerPanel(), BorderLayout.SOUTH); // Disclaimer Text
        this.dialog.add(panel, BorderLayout.EAST);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    private JPanel createContributorsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        int row = 0;

        // Untertitel hinzufügen
        panel.add(getSubtitelLabel(getWord("dialog.disclaimer.subtitle-1")), gbc);

        row++;
        gbc.gridwidth = 1;

        // Version 1.0 hinzufügen
        gbc.insets.top = 10;
        addLabelRow(panel, gbc, row++, getWord("app.version") + "-1.0:", getWord("app-v1.0.date"));
        gbc.insets.top = 5;
        addLabelRow(panel, gbc, row++, getWord("app.developer") + ":", getWord("app-v1.0.developer"));

        // Version 2.0 hinzufügen
        gbc.insets.top = 20;
        addLabelRow(panel, gbc, row++, getWord("app.version") + "-2.0:", getWord("app-v2.0.date"));
        gbc.insets.top = 5;
        addLabelRow(panel, gbc, row++, getWord("app.developer") + ":", getWord("app-v2.0.developer"));
        addLabelRow(panel, gbc, row++, getWord("app.co-producer") + ":", getWord("app-v2.0.co-producer"));

        // Version 2.1 hinzufügen
        gbc.insets.top = 20;
        addLabelRow(panel, gbc, row++, getWord("app.version") + "-2.1:", getWord("app-v2.1.date"));
        gbc.insets.top = 5;
        addLabelRow(panel, gbc, row++, getWord("app.developer") + ":", getWord("app-v2.1.developer"));
        addLabelRow(panel, gbc, row++, "", getWord("app-v2.1.developer-2"));
        addLabelRow(panel, gbc, row, getWord("app.co-producer") + ":", getWord("app-v2.1.co-producer"));

        return panel;
    }

    private JPanel createDisclaimerPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        // Untertitel hinzufügen
        panel.add(getSubtitelLabel(getWord("dialog.disclaimer.subtitle-2")), BorderLayout.NORTH);

        // Disclaimer Text hinzufügen
        SelectableText disclaimerText = new SelectableText(StringUtils.formatInputToHTML(getWord("dialog.disclaimer.text"), "justify", MAX_WIDTH));
        panel.add(disclaimerText, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================
    // Hilfsfunktionen
    // ==========================================================
    private void addLabelRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, String labelValueText) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets.left = 0;
        gbc.insets.right = 5;
        gbc.weightx = 1;
        JLabel label = new JLabel(StringUtils.wrapHtml(StringUtils.wrapBold(labelText)));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.insets.left = 5;
        gbc.insets.right = 0;
        gbc.weightx = 0.65;
        panel.add(new JLabel(labelValueText), gbc);
    }

    private JLabel getSubtitelLabel(String text) {
        JLabel label = new JLabel(StringUtils.wrapHtml(StringUtils.applyAlignment(StringUtils.applyFontSize(
                text, 125), "center", MAX_WIDTH)));
        label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        label.setOpaque(true);
        label.setBackground(GuiUtils.COLOR_BLUE);
        label.setForeground(Color.WHITE);
        return label;
    }
}