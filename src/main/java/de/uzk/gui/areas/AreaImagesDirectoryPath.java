package de.uzk.gui.areas;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.UIEnvironment;
import de.uzk.gui.observer.ObserverContainer;
import de.uzk.image.Axis;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class AreaImagesDirectoryPath extends ObserverContainer<JPanel> {
    // Gui Elemente
    private JTextField txtFieldDirectory;

    public AreaImagesDirectoryPath(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        container.setLayout(UIEnvironment.getDefaultBorderLayout());

        // Bilder-Verzeichnis Pfad hinzufügen
        txtFieldDirectory = new JTextField();
        txtFieldDirectory.setEditable(false);
        txtFieldDirectory.putClientProperty("JTextField.placeholderText", getWord("imagesDirectory"));
        container.add(txtFieldDirectory, BorderLayout.CENTER);

        JButton btnChooseDirectory = new JButton(getWord("menu.project.open"));
        btnChooseDirectory.addActionListener(a -> gui.getActionHandler().executeAction(ActionType.SHORTCUT_OPEN_FOLDER));
        container.add(btnChooseDirectory, BorderLayout.EAST);

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
        if (workspace.isLoaded()) {
            Path imagePath = workspace.getCurrentImageFile().getFilePath();
            txtFieldDirectory.setText(imagePath.toAbsolutePath().toString());
        } else {
            txtFieldDirectory.setText(null);
        }
        txtFieldDirectory.setCaretPosition(0);
    }
}
