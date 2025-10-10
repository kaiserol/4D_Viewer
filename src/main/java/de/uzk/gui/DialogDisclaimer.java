package de.uzk.gui;

import de.uzk.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogDisclaimer {
    private final JDialog dialog;

    public DialogDisclaimer(JFrame frame) {
        this.dialog = new JDialog(frame, getWord("dialog.disclaimer"), true);
    }

    public void show() {
        this.dialog.setLayout(new BorderLayout(0, 0));
        this.dialog.setResizable(false);

        JPanel infoPanel = createContributorsPanel();
        this.dialog.add(infoPanel, BorderLayout.CENTER);

        JPanel copyrightPanel = createCopyRightPanel();
        this.dialog.add(copyrightPanel, BorderLayout.SOUTH);
        this.dialog.pack();
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    private JPanel createContributorsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        int row = 0;

        // Version 2.1
        row = addLabelRow(panel, gbc, row, getWord("app.meta.version") + "-2.1:", getWord("app-v2.1.date"));
        gbc.insets.top = 0;
        row = addLabelRow(panel, gbc, row, getWord("app.meta.author") + ":", getWord("app-v2.1.author"));
        row = addLabelRow(panel, gbc, row, "", getWord("app-v2.1.author-2"));
        row = addLabelRow(panel, gbc, row, getWord("app.meta.co-producer") + ":", getWord("app-v2.1.co-producer"));

        // Version 2.0
        gbc.insets.top = 20;
        row = addLabelRow(panel, gbc, row, getWord("app.meta.version") + "-2.0:", getWord("app-v2.0.date"));
        gbc.insets.top = 0;
        row = addLabelRow(panel, gbc, row, getWord("app.meta.author") + ":", getWord("app-v2.0.author"));
        row = addLabelRow(panel, gbc, row, getWord("app.meta.co-producer") + ":", getWord("app-v2.0.co-producer"));

        // Version 1.0
        gbc.insets.top = 20;
        row = addLabelRow(panel, gbc, row, getWord("app.meta.version") + "-1.0:", getWord("app-v1.0.date"));
        gbc.insets.top = 0;
        addLabelRow(panel, gbc, row, getWord("app.meta.author") + ":", getWord("app-v1.0.author"));

        return panel;
    }

    private int addLabelRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets.right = 20;
        panel.add(new JLabel(StringUtils.wrapHtml(StringUtils.applyFontStyle(label, Font.BOLD))), gbc);

        gbc.gridx = 1;
        gbc.insets.right = 0;
        panel.add(new JLabel(value), gbc);
        return row + 1;
    }

    private JPanel createCopyRightPanel() {
        JPanel copyrightPanel = new JPanel();
        copyrightPanel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 0, 10),
                new CompoundBorder(new MatteBorder(1, 0, 0, 0, GuiUtils.getBorderColor()),
                        new EmptyBorder(10, 0, 10, 0))));
        copyrightPanel.setLayout(new BorderLayout());

        String copyRightText = StringUtils.wrapHtml(StringUtils.wrapCenter(StringUtils.applyFontStyle(
                getWord("dialog.disclaimer.copyRight1") + "<br>" +
                        getWord("dialog.disclaimer.copyRight2"), Font.PLAIN)));
        JLabel copyRightLabel = new JLabel(copyRightText);
        copyRightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        copyrightPanel.add(copyRightLabel, BorderLayout.CENTER);

        return copyrightPanel;
    }
}