package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.gui.SelectableText;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.uzk.Main.settings;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogDisclaimer {
    // GUI-Elemente
    private final JDialog dialog;

    // Maximale Dialogbreite
    private static final int MAX_WIDTH = 500;

    public DialogDisclaimer(JFrame frame) {
        this.dialog = new JDialog(frame, getWord("dialog.disclaimer"), true);
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
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(createRightOfUsePanel(), BorderLayout.CENTER); // Right of Use
        panel.add(createDisclaimerPanel(), BorderLayout.SOUTH); // Liability Exclusion
        this.dialog.add(panel, BorderLayout.EAST);

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
        gbc.gridwidth = 2;
        int row = 0;

        // Untertitel hinzufügen
        panel.add(getSubTitle(getWord("dialog.disclaimer.subtitle-1")), gbc);
        row++;
        panel.add(getSubTitle(getWord("dialog.disclaimer.text-1")), gbc);

        row++;
        gbc.gridwidth = 1;

        // Version hinzufügen
        gbc.insets.top = 10;
        addInfoRow(panel, gbc, row++, getWord("app.version_1"), String.format(getWord("app.version_1.date"), getWord("date.unknown")));
        gbc.insets.top = 5;
        addInfoRow(panel, gbc, row++, getWord("app.developer"), getWord("app.version_1.developer"));
        addInfoRow(panel, gbc, row++, getWord("app.contributors"), getWord("app.version_1.contributor"));

        // Version hinzufügen
        gbc.insets.top = 20;
        addInfoRow(panel, gbc, row++, getWord("app.version_2_0"), getWord("app.version_2_0.date"));
        gbc.insets.top = 5;
        addInfoRow(panel, gbc, row++, getWord("app.developer"), getWord("app.version_2_0.developer"));
        addInfoRow(panel, gbc, row++, getWord("app.contributors"), getWord("app.version_2_0.contributor-1"));
        addInfoRow(panel, gbc, row++, null, getWord("app.version_2_0.contributor-2"));

        // Version hinzufügen
        gbc.insets.top = 20;
        addInfoRow(panel, gbc, row++, getWord("app.version_2_1"), String.format(getWord("app.version_2_1.date"), getWord("date.today")));
        gbc.insets.top = 5;
        addInfoRow(panel, gbc, row++, getWord("app.developer"), getWord("app.version_2_1.developer-1"));
        addInfoRow(panel, gbc, row++, null, getWord("app.version_2_1.developer-2"));
        addInfoRow(panel, gbc, row++, getWord("app.contributors"), getWord("app.version_2_1.contributor-1"));
        addInfoRow(panel, gbc, row, null, getWord("app.version_2_1.contributor-2"));

        return panel;
    }

    private JPanel createRightOfUsePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        // Untertitel hinzufügen
        panel.add(getSubTitle(getWord("dialog.disclaimer.subtitle-1")), BorderLayout.NORTH);

        // Text hinzufügen (Rechtlicher Hinweis)
        SelectableText rightOfUseText = new SelectableText(
            StringUtils.wrapHtmlWithLinks(
                getWord("dialog.disclaimer.text-1"),
                "justify",
                MAX_WIDTH
            )
        );
        panel.add(rightOfUseText, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDisclaimerPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        // Untertitel hinzufügen
        panel.add(getSubTitle(getWord("dialog.disclaimer.subtitle-2")), BorderLayout.NORTH);

        // Text hinzufügen
        SelectableText disclaimerText = new SelectableText(
            StringUtils.wrapHtmlWithLinks(
                getWord("dialog.disclaimer.text-2"),
                "justify",
                MAX_WIDTH
            ));
        panel.add(disclaimerText, BorderLayout.CENTER);
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

                Color lighterColor = GuiUtils.adjustColor(GuiUtils.getTextColor(), 0.3f, settings.getTheme().isLight());
                formattedValue = normalPart + " " + StringUtils.applyColor(specialPart, lighterColor);
            } else {
                formattedValue = valueText;
            }
        }
        panel.add(new JLabel(StringUtils.wrapHtml(formattedValue)), gbc);
    }

    private SelectableText getSubTitle(String title) {
        String htmlContent = StringUtils.applyDivAlignment(StringUtils.applyFontSize(title, 125), "center", MAX_WIDTH);
        SelectableText subTitleText = new SelectableText(StringUtils.wrapHtml(htmlContent));
        subTitleText.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        subTitleText.setOpaque(true);
        subTitleText.setBackground(GuiUtils.COLOR_BLUE);
        subTitleText.setForeground(Color.WHITE);
        return subTitleText;
    }
}