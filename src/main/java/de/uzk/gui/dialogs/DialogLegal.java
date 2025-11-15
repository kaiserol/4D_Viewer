package de.uzk.gui.dialogs;

import de.uzk.gui.SelectableText;
import de.uzk.gui.UIEnvironment;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogLegal {
    // Dialoge
    private final JDialog dialog;

    // Maximale Dialogbreite
    private static final int MAX_WIDTH = 500;

    public DialogLegal(JFrame frame) {
        this.dialog = new JDialog(frame, true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.setTitle(getWord("dialog.legal"));
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createUsagePanel(), BorderLayout.CENTER);
        contentPanel.add(createDisclaimerPanel(), BorderLayout.SOUTH);

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
    private JPanel createUsagePanel() {
        JPanel usagePanel = new JPanel(new BorderLayout(0, 10));

        // Untertitel hinzufügen
        usagePanel.add(createSelectableSubTitle(getWord("dialog.legal.subtitle.usage")), BorderLayout.NORTH);

        // Text hinzufügen
        SelectableText text = new SelectableText(
            StringUtils.wrapHtmlWithLinks(
                getWord("dialog.legal.text.usage"),
                "justify",
                MAX_WIDTH
            )
        );
        usagePanel.add(text, BorderLayout.CENTER);
        return usagePanel;
    }

    private JPanel createDisclaimerPanel() {
        JPanel disclaimerPanel = new JPanel(new BorderLayout(0, 10));

        // Untertitel hinzufügen
        disclaimerPanel.add(createSelectableSubTitle(getWord("dialog.legal.subtitle.disclaimer")), BorderLayout.NORTH);

        // Text hinzufügen
        SelectableText text = new SelectableText(
            StringUtils.wrapHtmlWithLinks(
                getWord("dialog.legal.text.disclaimer"),
                "justify",
                MAX_WIDTH
            ));
        disclaimerPanel.add(text, BorderLayout.CENTER);
        return disclaimerPanel;
    }

    private SelectableText createSelectableSubTitle(String title) {
        String htmlContent = StringUtils.applyDivAlignment(StringUtils.applyFontSize(title, 125), "center", MAX_WIDTH);
        SelectableText subTitleText = new SelectableText(StringUtils.wrapHtml(htmlContent));
        subTitleText.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        subTitleText.setOpaque(true);
        subTitleText.setBackground(ColorUtils.COLOR_BLUE);
        subTitleText.setForeground(Color.WHITE);
        return subTitleText;
    }
}