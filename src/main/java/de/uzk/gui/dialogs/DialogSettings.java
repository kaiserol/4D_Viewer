package de.uzk.gui.dialogs;

import de.uzk.gui.Gui;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.config;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogSettings {
    private boolean oldConfirmExit;
    private JCheckBox checkBox;

    public DialogSettings() {
    }

    public void show(Gui gui) {
        this.oldConfirmExit = config.isConfirmExit();

        int option = JOptionPane.showConfirmDialog(
                gui.getFrame(),
                createSettingsPanel(),
                getWord("dialog.settings.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) return;

        // Einstellungen aktualisieren
        config.setConfirmExit(checkBox.isSelected());
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.bottom = 5;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        // Abschnittstitel
        panel.add(new JLabel(StringUtils.wrapHtml(StringUtils.applyFontStyle(
                getWord("dialog.settings.section.windowBehavior"), Font.BOLD))), gbc);

        // Checkbox für „Bestätigung beim Schließen“
        this.checkBox = new JCheckBox();
        this.checkBox.setText(getWord("dialog.settings.confirmExit"));
        this.checkBox.setSelected(this.oldConfirmExit);

        gbc.gridy++;
        gbc.insets.bottom = 0;
        panel.add(this.checkBox, gbc);

        return panel;
    }
}