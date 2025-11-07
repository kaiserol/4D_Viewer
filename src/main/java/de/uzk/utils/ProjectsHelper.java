package de.uzk.utils;

import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.image.ImageFileType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.utils.PathManager.USER_DIRECTORY;

public class ProjectsHelper {

    private static final Insets buttonMargin = new Insets(5, 5, 5, 5);

    private ProjectsHelper() {}

    public static void openRecents(Gui gui) {
        DefaultListModel<Path> model = new DefaultListModel<>();
        model.addAll(history.getAll());
        JList<Path> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);

        int option = JOptionPane.showConfirmDialog(null,
            list,
            getWord("items.project.openRecent"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            Path selectedPath = list.getSelectedValue();
            if (selectedPath == null) return;
            gui.openImagesDirectory(selectedPath, null, false);

        }
    }

    public static void clearImages(Gui gui) {
        int option = JOptionPane.showConfirmDialog(gui.getContainer(),
            getWord("optionPane.directory.clear"),
            getWord("optionPane.title.confirm"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            logger.info("Clear images...");
            gui.toggleOff();
        }
    }

    // ========================================
    // Dateiauswahl Methoden
    // ========================================
    public static void openFileChooser(Gui gui) {
        JFileChooser fileChooser = getFileChooser();

        // Startverzeichnis wählen
        Path lastUsedDirectory = history.getLastIfExists();
        if (lastUsedDirectory == null) {
            Path userDirectory = USER_DIRECTORY;
            if (Files.isDirectory(userDirectory)) fileChooser.setCurrentDirectory(userDirectory.toFile());
        } else {
            fileChooser.setSelectedFile(lastUsedDirectory.toFile());
        }

        // Dialog öffnen
        int option = fileChooser.showOpenDialog(gui.getContainer());
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile == null) return;

            // Bilder laden
            ImageFileType imageFileType = getSelectedImageFileType((FileNameExtensionFilter) fileChooser.getFileFilter());
            gui.openImagesDirectory(Path.of(selectedFile.getAbsolutePath()), imageFileType, false);
        }
    }

    private static JFileChooser getFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.putClientProperty("FileChooser.readOnly", Boolean.TRUE);
        resetButtonsMarginRecursively(fileChooser);

        // Dialogtitel setzen
        setFileChooserTitel(fileChooser);

        // Filter setzen
        setFileChooserFilter(fileChooser);

        // Inhalt hinzufügen (Zeit-Trenner, Ebenen-Trenner)
        addFileChooserContent(fileChooser);
        return fileChooser;
    }

    private static void resetButtonsMarginRecursively(Component comp) {
        if (comp instanceof AbstractButton button) {
            // Nur Buttons ohne Text (also IconButtons) zurücksetzen
            if (button.getText() == null || button.getText().isEmpty()) button.setMargin(buttonMargin);
        }

        if (comp instanceof Container innerContainer) {
            for (Component child : GuiUtils.getComponents(innerContainer)) {
                resetButtonsMarginRecursively(child);
            }
        }
    }

    private static void setFileChooserTitel(JFileChooser fileChooser) {
        String dialogTitle = String.format("%s", getWord("button.chooseDirectory"));
        fileChooser.setDialogTitle(dialogTitle);

        // Dialogtitel dynamisch setzen, wenn der Filter geändert wird
        fileChooser.addPropertyChangeListener(evt -> {
            if (evt.getNewValue() instanceof FileNameExtensionFilter filter) {
                ImageFileType imageFileType = getSelectedImageFileType(filter);
                String newTitle = String.format("%s (%s)", getWord("button.chooseDirectory"), imageFileType.getDescription());
                fileChooser.setDialogTitle(newTitle);
            }
        });
    }

    private static void setFileChooserFilter(JFileChooser fileChooser) {
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
    }

    private static void addFileChooserContent(JFileChooser fileChooser) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 10, 0, 0),
            BorderFactory.createLineBorder(GuiUtils.getBorderColor())
        ));

        // contentPanel
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(GuiUtils.getBackgroundColor());

        // contentPanel hinzufügen
        panel.add(contentPanel, BorderLayout.CENTER);
        fileChooser.setAccessory(panel);
    }

    private static ImageFileType getSelectedImageFileType(FileNameExtensionFilter filter) {
        if (filter == null) return ImageFileType.getDefault();

        String firstExtension = filter.getExtensions()[0];
        return ImageFileType.fromExtension(firstExtension);
    }
}
