package de.uzk.gui.dialogs;

import de.uzk.config.InitialDirectory;
import de.uzk.config.Language;
import de.uzk.config.Settings;
import de.uzk.config.Theme;
import de.uzk.gui.ScreenshotDirectorySelector;
import de.uzk.gui.Gui;
import de.uzk.gui.UIEnvironment;
import de.uzk.utils.ComponentUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Objects;

import static de.uzk.Main.settings;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogSettings {
    private final Gui gui;

    // Dialoge
    private final JDialog dialog;

    // Gui Elemente
    private JComboBox<Language> selectLanguage;
    private JComboBox<Theme> selectTheme;
    private JComboBox<InitialDirectory> selectInitialDirectory;
    private ScreenshotDirectorySelector selectScreenshotDirectory;
    private JSpinner fontSizeSpinner;
    private JCheckBox checkConfirmExit;
    private JButton okButton;

    // Alte Werte
    private Language oldLanguage;
    private Theme oldTheme;
    private InitialDirectory oldInitialDirectory;
    private Path oldScreenshotPath;
    private int oldFontSize;
    private boolean oldConfirmExit;

    public DialogSettings(Gui gui) {
        this.gui = gui;
        this.dialog = ComponentUtils.createDialog(gui.getContainer(), () -> confirmAndDispose(false));
    }

    private void confirmAndDispose(boolean applySettings) {
        if (applySettings) applySettings();
        this.dialog.dispose();
    }

    public void show() {
        // Wenn Dialog bereits offen ist → in den Vordergrund bringen
        if (this.dialog.isVisible()) {
            this.dialog.toFront();
            this.dialog.requestFocus();
            return;
        }
        this.dialog.setTitle(getWord("dialog.settings"));
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Alte Werte speichern
        this.oldLanguage = settings.getLanguage();
        this.oldTheme = settings.getTheme();
        this.oldFontSize = settings.getFontSize();
        this.oldConfirmExit = settings.isConfirmExit();
        this.oldInitialDirectory = settings.getInitialDirectory();
        this.oldScreenshotPath = settings.getScreenshotDirectory();

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(UIEnvironment.getDefaultBorderLayout());
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createSettingsPanel(), BorderLayout.CENTER);
        contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

        this.dialog.add(contentPanel, BorderLayout.CENTER);

        // Listener einrichten, damit OK nur aktiv ist, wenn sich etwas geändert hat
        setupChangeListeners();

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets.right = 100;

        // 1. Abschnitt: Erscheinungsbild hinzufügen
        ComponentUtils.addRow(settingsPanel, gbc, createBoldLabel(getWord("dialog.settings.section.appearance")), 0);

        gbc.gridwidth = 1;

        // Auswahlfeld (Sprache) hinzufügen
        this.selectLanguage = ComponentUtils.createComboBox(Language.sortedValues(), null);
        this.selectLanguage.setSelectedItem(this.oldLanguage);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.language"), this.selectLanguage, 10);

        // Auswahlfeld (Farbschema) hinzufügen
        this.selectTheme = ComponentUtils.createComboBox(Theme.sortedValues(), null);
        this.selectTheme.setSelectedItem(this.oldTheme);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.theme"), this.selectTheme, 10);

        this.selectInitialDirectory = ComponentUtils.createComboBox(InitialDirectory.values(), null);
        this.selectInitialDirectory.setSelectedItem(this.oldInitialDirectory);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.initialDirectory"), this.selectInitialDirectory, 10);

        selectScreenshotDirectory = new ScreenshotDirectorySelector(oldScreenshotPath);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.screenshotDirectory"), selectScreenshotDirectory, 10);

        // Drehfeld (Schriftgröße) hinzufügen
        this.fontSizeSpinner = ComponentUtils.createSpinner(Settings.MIN_FONT_SIZE, Settings.MAX_FONT_SIZE, false, null);
        this.fontSizeSpinner.setValue(this.oldFontSize);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.fontSize"), this.fontSizeSpinner, 10);

        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets.right = 100;

        // 2. Abschnitt: Fenster-Verhalten hinzufügen
        ComponentUtils.addRow(settingsPanel, gbc, createBoldLabel(getWord("dialog.settings.section.windowBehavior")), 20);

        gbc.weightx = 0;

        // Kontrollkästchen (Beenden bestätigen) hinzufügen
        this.checkConfirmExit = ComponentUtils.createCheckBox(getWord("dialog.settings.checkBox.confirmExit"), null);
        this.checkConfirmExit.setSelected(this.oldConfirmExit);
        ComponentUtils.addRow(settingsPanel, gbc, this.checkConfirmExit, 10);

        return settingsPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Schaltfläche (Abbrechen)
        JButton cancelButton = new JButton(getWord("button.cancel"));
        cancelButton.addActionListener(e -> confirmAndDispose(false));
        buttonsPanel.add(cancelButton);

        // Schaltfläche (OK)
        this.okButton = new JButton(getWord("button.ok"));
        this.okButton.addActionListener(e -> confirmAndDispose(true));
        buttonsPanel.add(this.okButton);

        // Gleicht die Größen aller Buttons an
        ComponentUtils.equalizeComponentSizes(buttonsPanel, JButton.class);

        // Den OK-Button als Default-Button setzen
        this.dialog.getRootPane().setDefaultButton(this.okButton);

        return buttonsPanel;
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private JLabel createBoldLabel(String text) {
        return new JLabel(StringUtils.wrapHtml(StringUtils.wrapBold(text)));
    }

    private void setupChangeListeners() {
        // Initial: deaktiviert
        this.okButton.setEnabled(false);

        // Prüft, ob ein Wert geändert wurde
        Runnable checkChanges = () -> {
            boolean changed = !Objects.equals(this.selectLanguage.getSelectedItem(), this.oldLanguage)
                || !Objects.equals(this.selectTheme.getSelectedItem(), this.oldTheme)
                || !Objects.equals(this.selectInitialDirectory.getSelectedItem(), this.oldInitialDirectory)
                || !Objects.equals(this.selectScreenshotDirectory.getScreenshotDirectory(), this.oldScreenshotPath)
                || (int) this.fontSizeSpinner.getValue() != this.oldFontSize
                || this.checkConfirmExit.isSelected() != this.oldConfirmExit;
            this.okButton.setEnabled(changed);
        };

        // Listener für JComboBox (Language, Theme & InitialDirectory) hinzufügen
        this.selectLanguage.addActionListener(e -> checkChanges.run());
        this.selectTheme.addActionListener(e -> checkChanges.run());
        this.selectInitialDirectory.addActionListener(e -> checkChanges.run());
        this.selectScreenshotDirectory.addChangeListener(p -> checkChanges.run());

        // Listener für JSpinner (FontSize) hinzufügen
        this.fontSizeSpinner.addChangeListener(e -> checkChanges.run());

        // Listener für JCheckBox (ConfirmExit) hinzufügen
        this.checkConfirmExit.addActionListener(e -> checkChanges.run());

    }

    private void applySettings() {
        UIEnvironment.updateLanguage(this.gui, (Language) this.selectLanguage.getSelectedItem());
        UIEnvironment.updateTheme(this.gui, (Theme) this.selectTheme.getSelectedItem());
        UIEnvironment.updateInitialDirectory((InitialDirectory) this.selectInitialDirectory.getSelectedItem());
        UIEnvironment.updateFontSize(this.gui, (int) this.fontSizeSpinner.getValue());
        UIEnvironment.updateConfirmExit(this.checkConfirmExit.isSelected());
        UIEnvironment.updateScreenshotDirectory(this.selectScreenshotDirectory.getScreenshotDirectory());
    }
}