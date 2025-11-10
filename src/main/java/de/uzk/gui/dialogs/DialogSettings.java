package de.uzk.gui.dialogs;

import de.uzk.config.Language;
import de.uzk.config.Settings;
import de.uzk.config.Theme;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;

import static de.uzk.Main.settings;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogSettings {
    // GUI-Elemente
    private final JDialog dialog;
    private final Gui gui;
    private JComboBox<Language> selectLanguage;
    private JComboBox<Theme> selectTheme;
    private JSpinner fontSizeSpinner;
    private JCheckBox checkConfirmExit;

    // Alte Werte
    private Language oldLanguage;
    private Theme oldTheme;
    private int oldFontSize;
    private boolean oldConfirmExit;

    public DialogSettings(Gui gui) {
        this.dialog = new JDialog(gui.getContainer(), getWord("dialog.settings"), true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.gui = gui;

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        // Wenn Dialog bereits offen ist → in den Vordergrund bringen
        if (this.dialog.isVisible()) {
            this.dialog.toFront();
            this.dialog.requestFocus();
            return;
        }
        dialog.getContentPane().removeAll();
        dialog.setLayout(new BorderLayout());

        // Alte Werte speichern
        this.oldLanguage = settings.getLanguage();
        this.oldTheme = settings.getTheme();
        this.oldFontSize = settings.getFontSize();
        this.oldConfirmExit = settings.isConfirmExit();

        // Button-Leiste erstellen
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton = new JButton(getWord("button.cancel"));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton okButton = new JButton(getWord("button.ok"));
        okButton.addActionListener(e -> {
            applySettings();
            this.dialog.dispose();
        });
        buttonPanel.add(okButton);
        GuiUtils.makeComponentsSameSize(buttonPanel, JButton.class);

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(GuiUtils.BORDER_PADDING_LARGE);
        contentPanel.add(createSettingsPanel(), BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        this.dialog.add(contentPanel, BorderLayout.CENTER);

        // Den OK-Button als Default-Button setzen
        this.dialog.getRootPane().setDefaultButton(okButton);

        // Listener einrichten, damit OK nur aktiv ist, wenn sich etwas geändert hat
        setupChangeListeners(okButton);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 1. Abschnitt: Erscheinungsbild hinzufügen
        gbc.insets.right = 100;
        panel.add(getBoldSectionLabel("dialog.settings.appearance"), gbc);

        // Sprache
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets.top = 10;
        gbc.insets.right = 10;
        panel.add(new JLabel(getWord("dialog.settings.appearance.language") + ":"), gbc);

        gbc.gridx = 1;
        gbc.insets.right = 0;
        this.selectLanguage = new JComboBox<>(Language.sortedValues());
        this.selectLanguage.setSelectedItem(this.oldLanguage);
        panel.add(this.selectLanguage, gbc);

        // Farbschema
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.right = 10;
        panel.add(new JLabel(getWord("dialog.settings.appearance.theme") + ":"), gbc);

        gbc.gridx = 1;
        gbc.insets.right = 0;
        this.selectTheme = new JComboBox<>(Theme.sortedValues());
        this.selectTheme.setSelectedItem(this.oldTheme);
        panel.add(this.selectTheme, gbc);

        // Schriftgröße
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.right = 10;
        panel.add(new JLabel(getWord("dialog.settings.appearance.fontSize") + ":"), gbc);

        gbc.gridx = 1;
        gbc.insets.right = 0;
        this.fontSizeSpinner = new JSpinner(new SpinnerNumberModel(this.oldFontSize, Settings.MIN_FONT_SIZE, Settings.MAX_FONT_SIZE, 1));
        panel.add(this.fontSizeSpinner, gbc);

        // 2. Abschnitt: Fenster-Verhalten hinzufügen
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets.top = 20;
        gbc.insets.right = 100;
        panel.add(getBoldSectionLabel("dialog.settings.windowBehavior"), gbc);

        gbc.gridy++;
        gbc.insets.right = 0;
        this.checkConfirmExit = new JCheckBox(getWord("dialog.settings.windowBehavior.confirmExit"));
        this.checkConfirmExit.setSelected(this.oldConfirmExit);
        panel.add(this.checkConfirmExit, gbc);

        return panel;
    }

    private JLabel getBoldSectionLabel(String key) {
        return new JLabel(StringUtils.wrapHtml(StringUtils.wrapBold(getWord(key))));
    }

    private void setupChangeListeners(JButton okButton) {
        // Initial: deaktiviert
        okButton.setEnabled(false);

        // Prüft, ob ein Wert geändert wurde
        Runnable checkChanges = () -> {
            boolean changed = !Objects.equals(this.selectLanguage.getSelectedItem(), this.oldLanguage)
                    || !Objects.equals(this.selectTheme.getSelectedItem(), this.oldTheme)
                    || (int) this.fontSizeSpinner.getValue() != this.oldFontSize
                    || this.checkConfirmExit.isSelected() != this.oldConfirmExit;
            okButton.setEnabled(changed);
        };

        // Listener für JComboBox (Language & Theme)
        this.selectLanguage.addActionListener(e -> checkChanges.run());
        this.selectTheme.addActionListener(e -> checkChanges.run());

        // Listener für JSpinner (FontSize)
        this.fontSizeSpinner.addChangeListener(e -> checkChanges.run());

        // Listener für JCheckBox (ConfirmExit)
        this.checkConfirmExit.addActionListener(e -> checkChanges.run());
    }

    private void applySettings() {
        GuiUtils.updateLanguage(this.gui, (Language) this.selectLanguage.getSelectedItem());
        GuiUtils.updateTheme(this.gui, (Theme) this.selectTheme.getSelectedItem());
        GuiUtils.updateFontSize(this.gui, (int) this.fontSizeSpinner.getValue());
        settings.setConfirmExit(this.checkConfirmExit.isSelected());
    }
}