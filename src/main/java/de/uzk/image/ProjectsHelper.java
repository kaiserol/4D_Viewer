package de.uzk.image;

import de.uzk.gui.Gui;
import de.uzk.gui.dialogs.DialogDirectoryChooser;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;

import static de.uzk.Main.history;
import static de.uzk.Main.logger;
import static de.uzk.config.LanguageHandler.getWord;

public class ProjectsHelper {
    private ProjectsHelper() {
    }

    public static void openRecents(Gui gui) {
        DefaultListModel<Path> model = new DefaultListModel<>();
        model.addAll(history.getAll());
        JList<Path> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);

        int option = JOptionPane.showConfirmDialog(null,
            list,
            getWord("menu.project.openRecent"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            Path selectedPath = list.getSelectedValue();
            if (selectedPath == null) return;

            // TODO: Warum rausgenommen (für mich)
//            gui.openImagesDirectory(selectedPath, workspace.getConfig().getImageFileType(), false);
            gui.loadImagesDirectory(selectedPath, null, false);
        }
    }

    public static void clearImages(Gui gui) {
        int option = JOptionPane.showConfirmDialog(gui.getContainer(),
            getWord("dialog.loadingImages.closeProject"),
            getWord("optionPane.title.confirm"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            logger.info("Clear images...");
            gui.toggleOff();
        }
    }

    public static void openProject(Gui gui) {
        // Dialog öffnen
        DialogDirectoryChooser directoryChooser = new DialogDirectoryChooser();
        int option = directoryChooser.showOpenDialog(gui.getContainer());

        if (option == JFileChooser.APPROVE_OPTION) {
            File directory = directoryChooser.getDirectory();
            if (directory == null) return;

            // Bilder laden
            ImageFileType imageFileType = directoryChooser.getSelectedImageFileType();
            gui.loadImagesDirectory(directory.toPath(), imageFileType, false);
        }
    }
}
