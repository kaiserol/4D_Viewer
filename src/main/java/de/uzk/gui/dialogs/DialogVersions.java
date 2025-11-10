package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogVersions {
    // GUI-Elemente
    private final JDialog dialog;

    public DialogVersions(JFrame frame) {
        this.dialog = new JDialog(frame, getWord("dialog.versions"), true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBorder(GuiUtils.BORDER_PADDING_LARGE);
        panel.add(createContributorsPanel(), BorderLayout.CENTER);
        this.dialog.add(panel, BorderLayout.CENTER);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    private JPanel createContributorsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        // Version hinzufügen
        addInfoRow(panel, gbc, row++, getWord("app.v1"), String.format(getWord("app.v1.date"), getWord("date.unknown")));
        gbc.insets.top = 5;
        addInfoRow(panel, gbc, row++, getWord("developers"), getWord("dialog.versions.v1.developer"));
        addInfoRow(panel, gbc, row++, getWord("contributors"), getWord("dialog.versions.v1.contributor"));

        // Version hinzufügen
        gbc.insets.top = 20;
        addInfoRow(panel, gbc, row++, getWord("app.v2_0"), getWord("app.v2_0.date"));
        gbc.insets.top = 5;
        addInfoRow(panel, gbc, row++, getWord("developers"), getWord("dialog.versions.v2_0.developer"));
        addInfoRow(panel, gbc, row++, getWord("contributors"), getWord("dialog.versions.v2_0.contributor-1"));
        addInfoRow(panel, gbc, row++, null, getWord("dialog.versions.v2_0.contributor-2"));

        // Version hinzufügen
        gbc.insets.top = 20;
        addInfoRow(panel, gbc, row++, getWord("app.v2_1"), String.format(getWord("app.v2_1.date"), getWord("date.today")));
        gbc.insets.top = 5;
        addInfoRow(panel, gbc, row++, getWord("developers"), getWord("dialog.versions.v2_1.developer-1"));
        addInfoRow(panel, gbc, row++, null, getWord("dialog.versions.v2_1.developer-2"));
        addInfoRow(panel, gbc, row++, getWord("contributors"), getWord("dialog.versions.v2_1.contributor-1"));
        addInfoRow(panel, gbc, row, null, getWord("dialog.versions.v2_1.contributor-2"));

        return panel;
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, String valueText) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets.left = 0;
        gbc.insets.right = 5;
        gbc.weightx = 0;

        JLabel label = new JLabel(StringUtils.wrapHtml(StringUtils.wrapBold(labelText) + ":"));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        if (labelText != null && !labelText.isEmpty()) panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.insets.left = 5;
        gbc.insets.right = 0;
        gbc.weightx = 1;

        // Formatierungslogik für valueText
        String formattedValue;
        if (valueText == null || valueText.isEmpty()) {
            formattedValue = "";
        } else {
            // Pattern für runde Klammern
            Pattern p = Pattern.compile("\\((.*)\\)\\s*$");
            Matcher m = p.matcher(valueText);

            if (m.find()) {
                int start = m.start();
                String normalPart = valueText.substring(0, start).trim();
                String specialPart = valueText.substring(start).trim();

                Color lighterColor = GuiUtils.adjustColor(GuiUtils.COLOR_BLUE, 0.2f, true);
                formattedValue = normalPart + " " + StringUtils.applyColor(specialPart, lighterColor);
            } else {
                formattedValue = valueText;
            }
        }
        panel.add(new JLabel(StringUtils.wrapHtml(formattedValue)), gbc);
    }
}
