package de.uzk.gui.dialogs;

import de.uzk.config.Language;
import de.uzk.config.Theme;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.settings;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogSettings {
    private boolean oldConfirmExit;
    private JCheckBox checkBox;
    private JComboBox<Language> selectLanguage;
    private JComboBox<Theme> selectTheme;
    private JTextField timeSeparator;
    private JTextField levelSeparator;

    public DialogSettings() {
    }

    public void show(Gui gui) {
        this.oldConfirmExit = settings.isConfirmExit();

        int option = JOptionPane.showConfirmDialog(
                gui.getContainer(),
                createSettingsPanel(),
                getWord("dialog.settings.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) return;

        // Einstellungen aktualisieren
        settings.setConfirmExit(checkBox.isSelected());
        GuiUtils.setLanguage(gui, (Language) selectLanguage.getSelectedItem());
        GuiUtils.setTheme(gui, (Theme) selectTheme.getSelectedItem());
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

        // Abschnitt: Fenster-Verhalten
        panel.add(boldTranslatedLabel("dialog.settings.section.windowBehavior"), gbc);

        // Checkbox für „Bestätigung beim Schließen“
        this.checkBox = new JCheckBox();
        this.checkBox.setText(getWord("dialog.settings.confirmExit"));
        this.checkBox.setSelected(this.oldConfirmExit);

        gbc.gridy++;
        gbc.insets.bottom = 0;
        panel.add(this.checkBox, gbc);

        gbc.gridy++;
        gbc.gridx = 0;

        panel.add(boldTranslatedLabel("dialog.settings.section.appearance"), gbc);

        gbc.gridy++;

        panel.add(new JLabel(getWord("dialog.settings.language")), gbc);

        gbc.gridx +=1;
        this.selectLanguage = new JComboBox<>(Language.values());
        this.selectLanguage.setSelectedItem(settings.getLanguage());
        panel.add(this.selectLanguage, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 1;
        panel.add(new JLabel(getWord("dialog.settings.theme")), gbc);
        gbc.gridx += 1;
        this.selectTheme = new JComboBox<>(Theme.values());

        this.selectTheme.setSelectedItem(settings.getTheme());
        panel.add(this.selectTheme, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(boldTranslatedLabel("dialog.settings.section.files"), gbc);

        gbc.gridy++;
        panel.add(new JLabel(getWord("dialog.settings.timeSeparator")), gbc);
        gbc.gridx += 1;
        this.timeSeparator = new JTextField();
        this.timeSeparator.setText("");
        panel.add(this.timeSeparator, gbc);


        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel(getWord("dialog.settings.levelSeparator")), gbc);
        gbc.gridx += 1;
        this.levelSeparator = new JTextField();
        this.levelSeparator.setText("");
        panel.add(this.levelSeparator, gbc);


        return panel;
    }

    private JLabel boldTranslatedLabel(String word) {
        return new JLabel(StringUtils.wrapHtml(StringUtils.wrapBold(getWord(word))));
    }
}