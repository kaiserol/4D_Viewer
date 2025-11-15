package de.uzk.gui.dialogs;

import de.uzk.gui.UIEnvironment;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.ComponentUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.uzk.Main.settings;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogHistory {
    // Dialoge
    private final JDialog dialog;

    public DialogHistory(JFrame frame) {
        this.dialog = new JDialog(frame, true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.setTitle(getWord("dialog.history"));
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createHistoryPanel(), BorderLayout.CENTER);

        this.dialog.add(contentPanel, BorderLayout.CENTER);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createHistoryPanel() {
        JPanel historyPanel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();

        // Version 1.* hinzufügen
        addLabeledRow(historyPanel, gbc, getWord("app.v1"), String.format(getWord("app.v1.date"), getWord("date.unknown")), 0);
        addLabeledRow(historyPanel, gbc, getWord("people.developers"), getWord("dialog.history.v1.developer"), 5);
        addLabeledRow(historyPanel, gbc, getWord("people.contributors"), getWord("dialog.history.v1.contributor"), 5);

        // Version 2.0 hinzufügen
        addLabeledRow(historyPanel, gbc, getWord("app.v2_0"), getWord("app.v2_0.date"), 20);
        addLabeledRow(historyPanel, gbc, getWord("people.developers"), getWord("dialog.history.v2_0.developer"), 5);
        addLabeledRow(historyPanel, gbc, getWord("people.contributors"), getWord("dialog.history.v2_0.contributor-1"), 5);
        addLabeledRow(historyPanel, gbc, null, getWord("dialog.history.v2_0.contributor-2"), 5);

        // Version 2.1 hinzufügen
        addLabeledRow(historyPanel, gbc, getWord("app.v2_1"), String.format(getWord("app.v2_1.date"), getWord("date.today")), 20);
        addLabeledRow(historyPanel, gbc, getWord("people.developers"), getWord("dialog.history.v2_1.developer-1"), 5);
        addLabeledRow(historyPanel, gbc, null, getWord("dialog.history.v2_1.developer-2"), 5);
        addLabeledRow(historyPanel, gbc, getWord("people.contributors"), getWord("dialog.history.v2_1.contributor-1"), 5);
        addLabeledRow(historyPanel, gbc, null, getWord("dialog.history.v2_1.contributor-2"), 5);

        return historyPanel;
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private void addLabeledRow(Container container, GridBagConstraints gbc, String labelText, String value, int topInset) {
        // 1. Linkes Label erzeugen
        JLabel titleLabel = null;
        if (labelText != null && !labelText.isEmpty()) {
            titleLabel = new JLabel(StringUtils.wrapHtml(StringUtils.wrapBold(labelText) + ":"));
            titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        }

        // 2. Textinhalt (rechts) vorbereiten
        String formattedText = "";

        if (value != null && !value.isEmpty()) {
            // Pattern sucht nach "(...)" am Ende des Textes — typischerweise Zusatzinfos
            Pattern bracketPattern = Pattern.compile("\\((.*)\\)\\s*$");
            Matcher matcher = bracketPattern.matcher(value);

            if (matcher.find()) {
                // Text in "normalen" und "hervorgehobenen" Teil zerlegen
                String mainText = value.substring(0, matcher.start()).trim();
                String bracketPart = matcher.group(0).trim();

                // Zusatzinfo farblich leicht absetzen
                Color highlightColor = ColorUtils.adjustColor(UIEnvironment.getTextColor(), 0.3f, settings.getTheme().isLightMode());
                formattedText = StringUtils.wrapHtml(mainText + " " +
                    StringUtils.applyColor(bracketPart, highlightColor));
            } else {
                // Kein Klammertext – einfach übernehmen
                formattedText = StringUtils.wrapHtml(value);
            }
        }
        JLabel valueLabel = new JLabel(formattedText);

        // 3. Zeile mit ComponentUtils hinzufügen
        ComponentUtils.addLabeledRow(container, gbc, titleLabel, valueLabel, topInset);
    }
}
