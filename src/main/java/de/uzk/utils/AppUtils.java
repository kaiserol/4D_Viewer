package de.uzk.utils;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.dialogs.DialogDirectoryChooser;
import de.uzk.gui.dialogs.DialogLoadingImages;
import de.uzk.image.ImageFileType;
import de.uzk.image.LoadingResult;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.*;
import static de.uzk.Main.settings;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class AppUtils {
    private AppUtils() {
    }

    public static void openRecents(Gui gui) {
        DefaultListModel<Path> model = new DefaultListModel<>();
        model.addAll(history.getAll());
        JList<Path> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);

        int option = JOptionPane.showConfirmDialog(gui.getContainer(),
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
            loadImagesDirectory(gui, selectedPath, null, false);
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
            loadImagesDirectory(gui, directory.toPath(), imageFileType, false);
        }
    }

    public static boolean loadImagesDirectory(Gui gui, Path imagesDirectory, ImageFileType imageFileType, boolean isGuiBeingBuilt) {
        // Wenn eine "gültige Datei" übergeben wird, wird ins Elternverzeichnis navigiert
        imagesDirectory = imagesDirectory != null && Files.isRegularFile(imagesDirectory) ? imagesDirectory.getParent() : imagesDirectory;

        // Prüfe, ob das Verzeichnis passende Bilder hat
        LoadingResult result = new DialogLoadingImages(gui.getContainer()).load(imagesDirectory, imageFileType);

        switch (result) {
            case LOADING_SUCCESSFUL -> {
                gui.toggleOn();
                gui.handleAction(ActionType.ACTION_ADD_MARKER);
                return true;
            }
            case DIRECTORY_ALREADY_LOADED -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("result.directoryAlreadyLoaded")
                    .formatted(imageFileType.getType(), imagesDirectory);
                JOptionPane.showMessageDialog(
                    gui.getContainer(),
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
            case DIRECTORY_DOES_NOT_EXIST -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("result.directoryDoesNotExist")
                    .formatted(imagesDirectory);
                JOptionPane.showMessageDialog(
                    gui.getContainer(),
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
            case DIRECTORY_HAS_NO_IMAGES -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("result.directoryHasNoImages")
                    .formatted(imagesDirectory, imageFileType.getType());
                JOptionPane.showMessageDialog(
                    gui.getContainer(),
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return false;
    }

    public static void closeProject(Gui gui) {
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

    public static void closeApp(JFrame frame) {
        if (settings.isConfirmExit()) {
            JCheckBox checkBox = ComponentUtils.createCheckBox(getWord("optionPane.closeApp.checkBox"), null);
            Object[] message = new Object[]{getWord("optionPane.closeApp.question"), checkBox};
            int option = JOptionPane.showConfirmDialog(
                frame,
                message,
                getWord("optionPane.title.confirm"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            // Wenn der Benutzer "Nein" klickt → Abbrechen
            if (option != JOptionPane.YES_OPTION) return;

            // Wenn Checkbox aktiv → Einstellung merken
            if (checkBox.isSelected()) GuiUtils.updateConfirmExit(false);
        }

        // Dateien abspeichern
        settings.save();
        history.save();
        workspace.saveConfigs();

        // Anwendung beenden
        System.exit(0);
    }
}
