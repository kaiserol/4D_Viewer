package de.uzk.gui.areas;

import de.uzk.gui.Gui;
import de.uzk.image.Axis;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class AreaImageDirectoryPath extends AreaContainerInteractive<JPanel> {
    // GUI-Elemente
    private JTextField txtFieldDirectory;

    public AreaImageDirectoryPath(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout());

        // Bilder-Verzeichnis Pfad hinzufügen
        this.txtFieldDirectory = new JTextField();
        this.txtFieldDirectory.setEditable(false);
        this.txtFieldDirectory.putClientProperty("JTextField.placeholderText", getWord("placeholder.imageDirectory"));
        this.container.add(this.txtFieldDirectory, BorderLayout.CENTER);
    }

    // ========================================
    // Observer Methoden
    // ========================================
    @Override
    public void toggleOn() {
        updateDirectoryText();
    }

    @Override
    public void toggleOff() {
        updateDirectoryText();
    }

    @Override
    public void update(Axis axis) {
        updateDirectoryText();
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private void updateDirectoryText() {
        if (workspace.isOpen()) {
            Path imagePath = workspace.getImageFile().getFilePath();
            this.txtFieldDirectory.setText(imagePath.toAbsolutePath().toString());
        } else {
            this.txtFieldDirectory.setText(null);
        }
        this.txtFieldDirectory.setCaretPosition(0);
    }
}
