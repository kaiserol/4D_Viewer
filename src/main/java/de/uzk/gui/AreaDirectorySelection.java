package de.uzk.gui;

import de.uzk.image.ImageFileNameExtension;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import static de.uzk.Main.imageFileHandler;
import static de.uzk.config.LanguageHandler.getWord;

public class AreaDirectorySelection extends AreaContainerInteractive<JPanel> {
    private JTextField txtFieldDirectory;
    private JFileChooser fileChooser;

    public AreaDirectorySelection(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout(10, 0));

        // txtFieldDirectory
        this.txtFieldDirectory = new JTextField();
        this.txtFieldDirectory.setEditable(false);
        this.txtFieldDirectory.putClientProperty("JTextField.placeholderText", getWord("placeholder.imageDirectory"));
        this.container.add(this.txtFieldDirectory, BorderLayout.CENTER);

        // btnChooseDirectory
        JButton btnChooseDirectory = new JButton(getWord("button.chooseDirectory"));
        btnChooseDirectory.addActionListener(a -> openFileChooser());

        // fileChooser
        this.fileChooser = getFileChooser();
        this.container.add(btnChooseDirectory, BorderLayout.EAST);
    }

    private void openFileChooser() {
        // Startverzeichnis
        if (imageFileHandler.hasImageFilesDirectory()) {
            fileChooser.setSelectedFile(imageFileHandler.getImageFilesDirectory());
        } else {
            String userDirectory = System.getProperty("user.dir");
            if (userDirectory != null) fileChooser.setCurrentDirectory(new File(userDirectory));
        }

        // Öffne Dialog
        int option = this.fileChooser.showOpenDialog(gui.getContainer());
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile == null) return;

            // Lade Image-Files
            ImageFileNameExtension extension = getSelectedExtension((FileNameExtensionFilter) fileChooser.getFileFilter());
            gui.loadImageFiles(selectedFile.getAbsolutePath(), extension, false);
        }
    }

    private JFileChooser getFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.putClientProperty("FileChooser.readOnly", Boolean.TRUE);
        setFileChooserTitel(fileChooser);

        // FileNameExtensionFilter hinzufügen
        for (ImageFileNameExtension ext : ImageFileNameExtension.values()) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    ext.getFullDescription(),
                    ext.getExtensions()
            );
            fileChooser.addChoosableFileFilter(filter);

            // Standardfilter auswählen
            if (ext == imageFileHandler.getImageFileNameExtension()) {
                fileChooser.setFileFilter(filter);
            }
        }

        // Optional: Nur Ordner zulassen, die Bilder enthalten
        fileChooser.setAcceptAllFileFilterUsed(false);
        return fileChooser;
    }

    private void setFileChooserTitel(JFileChooser fileChooser) {
        String dialogTitle = String.format("%s (%s)",
                getWord("button.chooseDirectory"),
                imageFileHandler.getImageFileNameExtension().getDescription());
        fileChooser.setDialogTitle(dialogTitle);

        // Dialogtitel dynamisch setzen, wenn der Filter geändert wird
        fileChooser.addPropertyChangeListener(evt -> {
            if (evt.getNewValue() instanceof FileNameExtensionFilter filter) {
                ImageFileNameExtension extension = getSelectedExtension(filter);
                if (extension == null) return;
                String newTitle = String.format("%s (%s)",
                        getWord("button.chooseDirectory"),
                        extension.getDescription());
                fileChooser.setDialogTitle(newTitle);
            }
        });
    }

    private ImageFileNameExtension getSelectedExtension(FileNameExtensionFilter filter) {
        if (filter == null) return null;

        for (ImageFileNameExtension ext : ImageFileNameExtension.values()) {
            if (ext.getFullDescription().equals(filter.getDescription())) {
                return ext;
            }
        }
        return null;
    }

    @Override
    public void toggleOn() {
        this.txtFieldDirectory.setText(imageFileHandler.getImageFilesDirectoryPath());
    }

    @Override
    public void toggleOff() {
        this.txtFieldDirectory.setText(null);
    }
}
