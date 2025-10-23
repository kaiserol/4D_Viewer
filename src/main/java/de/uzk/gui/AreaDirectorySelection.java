package de.uzk.gui;

import de.uzk.image.Axis;
import de.uzk.image.ImageFile;
import de.uzk.image.ImageFileNameExtension;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public class AreaDirectorySelection extends AreaContainerInteractive<JPanel> {
    private JTextField txtFieldDirectory;
    private JButton clearImagesButton;

    public AreaDirectorySelection(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout(10, 0));

        // clearImagesButton
        this.clearImagesButton = new JButton(Icons.ICON_DELETE);
        this.clearImagesButton.addActionListener(a -> clearImages());
        this.clearImagesButton.setToolTipText(getWord("tooltips.clearImages"));
        this.container.add(this.clearImagesButton, BorderLayout.WEST);

        // txtFieldDirectory
        this.txtFieldDirectory = new JTextField();
        this.txtFieldDirectory.setEditable(false);
        this.txtFieldDirectory.putClientProperty("JTextField.placeholderText", getWord("placeholder.imageDirectory"));
        this.container.add(this.txtFieldDirectory, BorderLayout.CENTER);

        // btnChooseDirectory
        JButton btnChooseDirectory = new JButton(getWord("button.chooseDirectory"));
        btnChooseDirectory.addActionListener(a -> openFileChooser());
        this.container.add(btnChooseDirectory, BorderLayout.EAST);

        // Fokus setzen, nachdem die UI aufgebaut ist
        SwingUtilities.invokeLater(btnChooseDirectory::requestFocusInWindow);
    }

    private void clearImages() {
        int option = JOptionPane.showConfirmDialog(gui.getContainer(),
                getWord("optionPane.directory.clear"),
                getWord("optionPane.title.confirm"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            logger.info("Clear images...");
            gui.toggleOff();
        } else this.container.requestFocusInWindow();
    }

    private void openFileChooser() {
        JFileChooser fileChooser = getFileChooser();

        // Startverzeichnis
        if (settings.getLastHistory() != null) {
            fileChooser.setSelectedFile(settings.getLastHistory().toFile());
        } else {
            String userDirectory = System.getProperty("user.dir");
            if (userDirectory != null) fileChooser.setCurrentDirectory(new File(userDirectory));
        }

        // Öffne Dialog
        int option = fileChooser.showOpenDialog(gui.getContainer());
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile == null) return;

            // Lade Image-Files
            ImageFileNameExtension extension = getSelectedExtension((FileNameExtensionFilter) fileChooser.getFileFilter());
            gui.loadImageFiles(Path.of(selectedFile.getAbsolutePath()), extension, false);
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
        for (ImageFileNameExtension ext : ImageFileNameExtension.sortedValues()) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    ext.getFullDescription(),
                    ext.getExtensions()
            );
            fileChooser.addChoosableFileFilter(filter);

            // Standardfilter auswählen
            if (ext == settings.getFileNameExt()) {
                fileChooser.setFileFilter(filter);
            }
        }

        // Optional: Nur Ordner zulassen, die Bilder enthalten
        fileChooser.setAcceptAllFileFilterUsed(false);
        return fileChooser;
    }

    private void setFileChooserTitel(JFileChooser fileChooser) {
        String dialogTitle = String.format("%s", getWord("button.chooseDirectory"));
        fileChooser.setDialogTitle(dialogTitle);

        // Dialogtitel dynamisch setzen, wenn der Filter geändert wird
        fileChooser.addPropertyChangeListener(evt -> {
            if (evt.getNewValue() instanceof FileNameExtensionFilter filter) {
                ImageFileNameExtension extension = getSelectedExtension(filter);
                String newTitle = String.format("%s (%s)", getWord("button.chooseDirectory"), extension.getDescription());
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
        this.clearImagesButton.setEnabled(true);
        updateDirectoryText();
    }

    @Override
    public void toggleOff() {
        this.clearImagesButton.setEnabled(false);
        this.txtFieldDirectory.setText(null);
    }

    @Override
    public void update(Axis axis) {
        updateDirectoryText();
    }

    private void updateDirectoryText() {
        ImageFile imageFile = workspace.getImageFile();
        String imageFileString = imageFile == null ? null : StringUtils.FILE_SEP + imageFile.getName();
        this.txtFieldDirectory.setText(workspace.getImageFilesDirectoryPath() + imageFileString);
        this.txtFieldDirectory.setCaretPosition(0);
    }
}
