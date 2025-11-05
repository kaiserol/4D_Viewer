package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.gui.SelectableText;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

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
        panel.add(createRightOfUsePanel(), BorderLayout.CENTER);
        panel.add(createLiabilityExclusionPanel(), BorderLayout.SOUTH);
        this.dialog.add(panel, BorderLayout.CENTER);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
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

    private JPanel createLiabilityExclusionPanel() {
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