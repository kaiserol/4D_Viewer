package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogDisclaimer {
    private final JDialog dialog;

    public DialogDisclaimer(JFrame frame) {
        this.dialog = new JDialog(frame, getWord("dialog.disclaimer.title"), true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.dialog.setLayout(new BorderLayout());
        this.dialog.setResizable(false);

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.getContentPane().removeAll();

        // contributorsPanel
        JPanel contributorsPanel = createContributorsPanel();
        this.dialog.add(contributorsPanel, BorderLayout.CENTER);

        // copyRightPanel
        JPanel copyrightPanel = createCopyRightPanel();
        this.dialog.add(copyrightPanel, BorderLayout.SOUTH);

        // Fenster anzeigen
        this.dialog.pack();
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    private JPanel createContributorsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        int row = 0;

        // Version 2.1
        row = addLabelRow(panel, gbc, row, getWord("app.version") + "-2.1:", getWord("app-v2.1.date"));
        gbc.insets.top = 0;
        row = addLabelRow(panel, gbc, row, getWord("app.developer") + ":", getWord("app-v2.1.developer"));
        row = addLabelRow(panel, gbc, row, "", getWord("app-v2.1.developer-2"));
        row = addLabelRow(panel, gbc, row, getWord("app.co-producer") + ":", getWord("app-v2.1.co-producer"));

        // Version 2.0
        gbc.insets.top = 20;
        row = addLabelRow(panel, gbc, row, getWord("app.version") + "-2.0:", getWord("app-v2.0.date"));
        gbc.insets.top = 0;
        row = addLabelRow(panel, gbc, row, getWord("app.developer") + ":", getWord("app-v2.0.developer"));
        row = addLabelRow(panel, gbc, row, getWord("app.co-producer") + ":", getWord("app-v2.0.co-producer"));

        // Version 1.0
        gbc.insets.top = 20;
        row = addLabelRow(panel, gbc, row, getWord("app.version") + "-1.0:", getWord("app-v1.0.date"));
        gbc.insets.top = 0;
        addLabelRow(panel, gbc, row, getWord("app.developer") + ":", getWord("app-v1.0.developer"));

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