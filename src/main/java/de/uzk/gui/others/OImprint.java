package de.uzk.gui.others;

import de.uzk.gui.Gui;
import de.uzk.gui.InteractiveContainer;
import de.uzk.gui.GuiUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static de.uzk.config.LanguageHandler.getWord;

public class OImprint extends InteractiveContainer<JPanel> {
    public OImprint(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new GridBagLayout());

        // GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // useRightLabel
        JLabel useRightLabel = new JLabel(getWord("imprint.useRight"));
        useRightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.container.add(useRightLabel, gbc);

        // gbc
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 20, 0, 0);

        JButton infoButton = new JButton();
        infoButton.setText(getWord("help"));
        infoButton.putClientProperty("JButton.buttonType", "help");
        infoButton.addActionListener(e -> new OInnerImprint(gui.getFrame()));
        infoButton.setToolTipText(getWord("tooltips.showImprint"));
        this.container.add(infoButton, gbc);
    }

    private static class OInnerImprint extends JDialog {

        public OInnerImprint(Frame parent) {
            super(parent, getWord("imprint"), true);
            setLayout(new BorderLayout());

            // infoPanel
            JPanel infoPanel = new JPanel(new GridBagLayout());
            infoPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

            // GridBagConstraints
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // curVersionLabel
            JLabel curVersionLabel = new JLabel(getWord("app.version.currentLabel") + ":");
            infoPanel.add(curVersionLabel, gbc);

            // gbc
            gbc.gridx++;
            gbc.weightx = 1;
            gbc.insets = new Insets(0, 10, 0, 0);

            // curVersionValueLabel
            JLabel curVersion = new JLabel(getWord("app.version.current"));
            curVersion.setHorizontalAlignment(SwingConstants.RIGHT);
            curVersion.setFont(curVersion.getFont().deriveFont(Font.BOLD));
            infoPanel.add(curVersion, gbc);

            // gbc
            gbc.weightx = 0;
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 0, 0);

            // curAuthorLabel
            JLabel curAuthorLabel = new JLabel(getWord("app.author.label") + ":");
            infoPanel.add(curAuthorLabel, gbc);

            // gbc
            gbc.gridx++;
            gbc.insets = new Insets(0, 10, 0, 0);
            gbc.weightx = 1;

            // curAuthorValueLabel
            JLabel curAuthor = new JLabel(getWord("app.author.current"));
            curAuthor.setHorizontalAlignment(SwingConstants.RIGHT);
            infoPanel.add(curAuthor, gbc);

            // gbc
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 0, 0);

            // curAuthorValueLabel
            JLabel cooperationLabel = new JLabel(getWord("app.author.currentCooperation"));
            GuiUtils.updateFontSize(cooperationLabel, -3, Font.ITALIC);
            infoPanel.add(cooperationLabel, gbc);

            // gbc
            gbc.gridy++;
            gbc.weightx = 0;
            gbc.gridwidth = 1;
            gbc.insets = new Insets(15, 0, 0, 0);

            // oldVersionLabel
            JLabel oldVersionLabel = new JLabel(getWord("app.version.oldLabel") + ":");
            infoPanel.add(oldVersionLabel, gbc);

            // gbc
            gbc.gridx++;
            gbc.weightx = 1;
            gbc.insets = new Insets(15, 0, 0, 0);

            // oldVersionValueLabel
            JLabel oldVersion = new JLabel(getWord("app.version.old"));
            oldVersion.setHorizontalAlignment(SwingConstants.RIGHT);
            infoPanel.add(oldVersion, gbc);

            // gbc
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.weightx = 0;

            // oldAuthorLabel
            JLabel oldAuthorLabel = new JLabel(getWord("app.author.label") + ":");
            infoPanel.add(oldAuthorLabel, gbc);

            // gbc
            gbc.gridx++;
            gbc.weightx = 1;

            // oldAuthorValueLabel
            JLabel oldAuthor = new JLabel(getWord("app.author.old"));
            oldAuthor.setHorizontalAlignment(SwingConstants.RIGHT);
            infoPanel.add(oldAuthor, gbc);

            add(infoPanel);

            // copyrightPanel
            JPanel copyrightImagesPanel = getCopyrightImagesPanel();
            add(copyrightImagesPanel, BorderLayout.SOUTH);

            pack();
            setResizable(false);
            setLocationRelativeTo(parent);
            setVisible(true);
        }

        private JPanel getCopyrightImagesPanel() {
            JPanel copyrightPanel = new JPanel();
            copyrightPanel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 10, 10),
                    new CompoundBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY),
                            new EmptyBorder(10, 0, 0, 0))));
            copyrightPanel.setLayout(new BorderLayout());

            String copyrightString = "<html><center>" +
                    getWord("imprint.copyRight1") +
                    getWord("imprint.copyRight2") +
                    getWord("imprint.copyRight3") +
                    "</center></html>";
            JLabel copyrightLabel = new JLabel(copyrightString);
            GuiUtils.updateFontSize(copyrightLabel, -3, Font.ITALIC);
            copyrightPanel.add(copyrightLabel, BorderLayout.CENTER);
            return copyrightPanel;
        }
    }
}
