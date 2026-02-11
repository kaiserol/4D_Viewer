package de.uzk.gui.dialogs;

import de.uzk.gui.SelectableText;
import de.uzk.gui.UIEnvironment;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.ComponentUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogLegal {
    // Maximale Dialogbreite
    private static final int MAX_WIDTH = 500;
    // Dialoge
    private final JDialog dialog;

    public DialogLegal(Window parentWindow) {
        dialog = ComponentUtils.createDialog(parentWindow, null);
    }

    public void show() {
        dialog.setTitle(getWord("dialog.legal"));
        dialog.getContentPane().removeAll();
        dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createSubTitledContentPanel("dialog.legal.subTitle.usage", "dialog.legal.text.usage"));
        contentPanel.add(Box.createVerticalStrut(UIEnvironment.SPACING_DEFAULT));
        contentPanel.add(createSubTitledContentPanel("dialog.legal.subTitle.image-sources", "dialog.legal.text.image-sources"));
        contentPanel.add(Box.createVerticalStrut(UIEnvironment.SPACING_DEFAULT));
        contentPanel.add(createSubTitledContentPanel("dialog.legal.subTitle.disclaimer", "dialog.legal.text.disclaimer"));

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Dialog anzeigen
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createSubTitledContentPanel(String subTitleWord, String textWord) {
        JPanel panel = new JPanel(UIEnvironment.getDefaultBorderLayout());
        panel.add(createSelectableSubTitle(getWord(subTitleWord)), BorderLayout.NORTH); // Untertitel hinzufügen
        panel.add(createSelectableContent(getWord(textWord)), BorderLayout.CENTER);  // Text hinzufügen
        return panel;
    }

    private SelectableText createSelectableSubTitle(String subTitle) {
        String htmlContent = StringUtils.wrapHtml(StringUtils.applyDivAlignment(
            StringUtils.applyFontSize(subTitle, 125),
            "center",
            MAX_WIDTH
        ));
        SelectableText subTitleText = new SelectableText(htmlContent);
        subTitleText.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        subTitleText.setOpaque(true);
        subTitleText.setBackground(ColorUtils.COLOR_BLUE);
        subTitleText.setForeground(Color.WHITE);
        return subTitleText;
    }

    private SelectableText createSelectableContent(String text) {
        String htmlContent = StringUtils.wrapHtmlWithLinks(
            text,
            "justify",
            MAX_WIDTH
        );
        return new SelectableText(htmlContent);
    }
}