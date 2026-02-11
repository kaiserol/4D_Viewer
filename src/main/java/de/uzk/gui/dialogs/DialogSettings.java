package de.uzk.gui.dialogs;

import de.uzk.config.InitialDirectory;
import de.uzk.config.Language;
import de.uzk.config.Settings;
import de.uzk.config.Theme;
import de.uzk.gui.Gui;
import de.uzk.gui.ScreenshotDirectorySelector;
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
        dialog = ComponentUtils.createDialog(gui.getContainer(), () -> confirmAndDispose(false));
    }

    private void confirmAndDispose(boolean applySettings) {
        if (applySettings) applySettings();
        dialog.dispose();
    }

    public void show() {
        // Wenn Dialog bereits offen ist → in den Vordergrund bringen
        if (dialog.isVisible()) {
            dialog.toFront();
            dialog.requestFocus();
            return;
        }
        dialog.setTitle(getWord("dialog.settings"));
        dialog.getContentPane().removeAll();
        dialog.setLayout(new BorderLayout());

        // Alte Werte speichern
        oldLanguage = settings.getLanguage();
        oldTheme = settings.getTheme();
        oldFontSize = settings.getFontSize();
        oldConfirmExit = settings.isConfirmExit();
        oldInitialDirectory = settings.getInitialDirectory();
        oldScreenshotPath = settings.getScreenshotDirectory();

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(UIEnvironment.getDefaultBorderLayout());
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createSettingsPanel(), BorderLayout.CENTER);
        contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Listener einrichten, damit OK nur aktiv ist, wenn sich etwas geändert hat
        setupChangeListeners();

        // Dialog anzeigen
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
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
        selectLanguage = ComponentUtils.createComboBox(Language.sortedValues(), null);
        selectLanguage.setSelectedItem(oldLanguage);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.language"), selectLanguage, 10);

        // Auswahlfeld (Farbschema) hinzufügen
        selectTheme = ComponentUtils.createComboBox(Theme.sortedValues(), null);
        selectTheme.setSelectedItem(oldTheme);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.theme"), selectTheme, 10);

        // Drehfeld (Schriftgröße) hinzufügen
        fontSizeSpinner = ComponentUtils.createSpinner(Settings.MIN_FONT_SIZE, Settings.MAX_FONT_SIZE, false, null);
        fontSizeSpinner.setValue(oldFontSize);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.fontSize"), fontSizeSpinner, 10);

        //2. Abschnitt (Ordnereinstellungen) hinzufügen
        ComponentUtils.addRow(settingsPanel, gbc, createBoldLabel(getWord("dialog.settings.directories")), 20);

        selectInitialDirectory = ComponentUtils.createComboBox(InitialDirectory.values(), null);
        selectInitialDirectory.setSelectedItem(oldInitialDirectory);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.initialDirectory"), selectInitialDirectory, 10);

        selectScreenshotDirectory = new ScreenshotDirectorySelector(oldScreenshotPath);
        ComponentUtils.addLabeledRow(settingsPanel, gbc, getWord("dialog.settings.screenshotDirectory"), selectScreenshotDirectory, 10);


        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets.right = 100;

        // 3. Abschnitt: Fenster-Verhalten hinzufügen
        ComponentUtils.addRow(settingsPanel, gbc, createBoldLabel(getWord("dialog.settings.section.windowBehavior")), 20);

        gbc.weightx = 0;

        // Kontrollkästchen (Beenden bestätigen) hinzufügen
        checkConfirmExit = ComponentUtils.createCheckBox(getWord("dialog.settings.checkBox.confirmExit"), null);
        checkConfirmExit.setSelected(oldConfirmExit);
        ComponentUtils.addRow(settingsPanel, gbc, checkConfirmExit, 10);

        return settingsPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Schaltfläche (Abbrechen)
        JButton cancelButton = new JButton(getWord("button.cancel"));
        cancelButton.addActionListener(e -> confirmAndDispose(false));
        buttonsPanel.add(cancelButton);

        // Schaltfläche (OK)
        okButton = new JButton(getWord("button.ok"));
        okButton.addActionListener(e -> confirmAndDispose(true));
        buttonsPanel.add(okButton);

        // Gleicht die Größen aller Buttons an
        ComponentUtils.equalizeComponentSizes(buttonsPanel, JButton.class);

        // Den OK-Button als Default-Button setzen
        dialog.getRootPane().setDefaultButton(okButton);

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
        okButton.setEnabled(false);

        // Prüft, ob ein Wert geändert wurde
        Runnable checkChanges = () -> {
            boolean changed = !Objects.equals(selectLanguage.getSelectedItem(), oldLanguage)
                || !Objects.equals(selectTheme.getSelectedItem(), oldTheme)
                || !Objects.equals(selectInitialDirectory.getSelectedItem(), oldInitialDirectory)
                || !Objects.equals(selectScreenshotDirectory.getScreenshotDirectory(), oldScreenshotPath)
                || (int) fontSizeSpinner.getValue() != oldFontSize
                || checkConfirmExit.isSelected() != oldConfirmExit;
            okButton.setEnabled(changed);
        };

        // Listener für JComboBox (Language, Theme & InitialDirectory) hinzufügen
        selectLanguage.addActionListener(e -> checkChanges.run());
        selectTheme.addActionListener(e -> checkChanges.run());
        selectInitialDirectory.addActionListener(e -> checkChanges.run());
        selectScreenshotDirectory.addChangeListener(p -> checkChanges.run());

        // Listener für JSpinner (FontSize) hinzufügen
        fontSizeSpinner.addChangeListener(e -> checkChanges.run());

        // Listener für JCheckBox (ConfirmExit) hinzufügen
        checkConfirmExit.addActionListener(e -> checkChanges.run());

    }

    private void applySettings() {
        UIEnvironment.updateLanguage(gui, (Language) selectLanguage.getSelectedItem());
        UIEnvironment.updateTheme(gui, (Theme) selectTheme.getSelectedItem());
        UIEnvironment.updateInitialDirectory((InitialDirectory) selectInitialDirectory.getSelectedItem());
        UIEnvironment.updateFontSize(gui, (int) fontSizeSpinner.getValue());
        UIEnvironment.updateConfirmExit(checkConfirmExit.isSelected());
        UIEnvironment.updateScreenshotDirectory(selectScreenshotDirectory.getScreenshotDirectory());
    }
}