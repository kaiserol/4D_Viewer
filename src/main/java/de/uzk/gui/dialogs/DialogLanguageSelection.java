package de.uzk.gui.dialogs;

import de.uzk.config.Language;
import de.uzk.gui.Gui;

import javax.swing.*;

import static de.uzk.Main.logger;
import static de.uzk.Main.settings;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogLanguageSelection {
    private Language oldLanguage;
    private JComboBox<Language> selectBox;
    private JOptionPane pane;
    private JDialog dialog;

    public DialogLanguageSelection() {
    }

    public void show(Gui gui) {
        // Dialog anzeigen
        this.oldLanguage = settings.getLanguage();
        this.initDialogPane(gui.getContainer());
        this.dialog.setVisible(true);

        // Ergebnis auswerten
        Object selectedValue = this.pane.getValue();
        if (selectedValue == null || !selectedValue.equals(JOptionPane.OK_OPTION)) return;

        Language newLanguage = (Language) this.selectBox.getSelectedItem();
        if (newLanguage == null || this.oldLanguage == newLanguage) return;

        // Sprache setzen
        logger.info("Changing Language from '" + this.oldLanguage + "' to '" + newLanguage + "'.");
        settings.setLanguage(newLanguage);

        // UI aktualisieren
        gui.rebuild();
    }

    private void initDialogPane(JFrame frame) {
        this.selectBox = new JComboBox<>(Language.sortedValues());
        this.selectBox.setSelectedItem(this.oldLanguage);

        // Benutzerdefinierte Buttons
        JButton okButton = new JButton(getWord("button.ok"));
        JButton cancelButton = new JButton(getWord("button.cancel"));
        okButton.setEnabled(false);

        // Wenn sich die Auswahl ändert → Button aktivieren/deaktivieren
        this.selectBox.addActionListener(a -> {
            Language selected = (Language) this.selectBox.getSelectedItem();
            okButton.setEnabled(selected != null && selected != this.oldLanguage);
        });

        // Inhalte & Optionen des Dialogs
        Object[] message = {selectBox};
        Object[] options = {okButton, cancelButton};

        // JOptionPane erstellen
        this.pane = new JOptionPane(
                message, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, options, okButton
        );
        this.dialog = this.pane.createDialog(frame, getWord("dialog.languageSelection.title"));
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Aktionen der Buttons
        okButton.addActionListener(a -> {
            this.pane.setValue(JOptionPane.OK_OPTION);
            this.dialog.dispose();
        });
        cancelButton.addActionListener(a -> {
            this.pane.setValue(JOptionPane.CANCEL_OPTION);
            this.dialog.dispose();
        });
    }
}
