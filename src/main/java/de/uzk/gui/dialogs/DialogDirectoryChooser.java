package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.image.ImageFileType;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

import static de.uzk.Main.history;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.io.PathManager.USER_DIRECTORY;

public class DialogDirectoryChooser {
    // Dialoge
    private JFileChooser fileChooser;

    public int showOpenDialog(JFrame frame) {
        // Dialog anzeigen
        this.fileChooser = createNewFileChooser();
        return this.fileChooser.showOpenDialog(frame);
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JFileChooser createNewFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.putClientProperty("FileChooser.readOnly", Boolean.TRUE);
        fileChooser.setSelectedFile(getLastDirectory());

        setFileFilter(fileChooser); // Filter setzen
        setTitel(fileChooser); // Dialogtitel setzen
        addContent(fileChooser); // Inhalte hinzufügen

        // Button-Margin zurücksetzen
        resetButtonsMargin(fileChooser);

        return fileChooser;
    }

    private void setFileFilter(JFileChooser fileChooser) {
        fileChooser.setAcceptAllFileFilterUsed(false);

        for (ImageFileType type : ImageFileType.sortedValues()) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                type.getFullDescription(),
                type.getExtensions()
            );
            fileChooser.addChoosableFileFilter(filter);

            // Standardfilter auswählen
            if (type == workspace.getConfig().getImageFileType()) {
                fileChooser.setFileFilter(filter);
            }
        }

        // Nur Verzeichnisse als gültige Auswahl erlauben
        fileChooser.addPropertyChangeListener(evt -> {
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                File selected = (File) evt.getNewValue();
                if (selected != null && selected.isFile()) {
                    fileChooser.setSelectedFile(selected.getParentFile());
                }
            }
        });
    }

    private void setTitel(JFileChooser fileChooser) {
        // TODO: Verändere... (openDirectory soll da nicht stehen)
        String dialogTitle = String.format("%s", getWord("menu.project.openDirectory"));
        fileChooser.setDialogTitle(dialogTitle);

        // Dialogtitel dynamisch setzen, wenn der Filter geändert wird
        fileChooser.addPropertyChangeListener(evt -> {
            if (evt.getNewValue() instanceof FileNameExtensionFilter filter) {
                ImageFileType imageFileType = getSelectedImageFileType(filter);
                String newTitle = String.format("%s (%s)", getWord("menu.project.openDirectory"), imageFileType.getDescription());
                fileChooser.setDialogTitle(newTitle);
            }
        });
    }

    private void addContent(JFileChooser fileChooser) {
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 10, 0, 0),
            BorderFactory.createLineBorder(GuiUtils.getBorderColor())
        ));

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(GuiUtils.BORDER_EMPTY_DEFAULT);
        contentPanel.setBackground(GuiUtils.getBackgroundColor());
        borderPanel.add(contentPanel, BorderLayout.CENTER);

        // Zubehör setzen
        fileChooser.setAccessory(borderPanel);
    }

    // ========================================
    // Getter
    // ========================================
    public File getDirectory() {
        File selected = this.fileChooser.getSelectedFile();
        if (selected != null && selected.isFile()) {
            return selected.getParentFile();
        }
        return selected;
    }

    public ImageFileType getSelectedImageFileType() {
        FileNameExtensionFilter filter = (FileNameExtensionFilter) this.fileChooser.getFileFilter();
        return getSelectedImageFileType(filter);
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private static File getLastDirectory() {
        Path lastDirectory = history.getLastIfExists();
        return lastDirectory == null ? USER_DIRECTORY.toFile() : lastDirectory.toFile();
    }

    private static ImageFileType getSelectedImageFileType(FileNameExtensionFilter filter) {
        if (filter == null) return ImageFileType.getDefault();

        String firstExtension = filter.getExtensions()[0];
        return ImageFileType.fromExtension(firstExtension);
    }

    private static void resetButtonsMargin(JFileChooser root) {
        ComponentUtils.findComponentsRecursively(AbstractButton.class, root).forEach(button -> {
            // Nur Buttons ohne Text (also IconButtons) zurücksetzen
            if (button.getText() == null || button.getText().isEmpty()) {
                button.setMargin(GuiUtils.INSETS_SMALL);
            }
        });
    }
}
