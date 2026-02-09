package de.uzk.utils;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.dialogs.DialogDirectoryChooser;
import de.uzk.gui.dialogs.DialogLoadingImages;
import de.uzk.image.Axis;
import de.uzk.image.ImageFileType;
import de.uzk.image.LoadingResult;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.history;
import static de.uzk.Main.logger;
import static de.uzk.config.LanguageHandler.getWord;

/**
 * Utility-Klasse für Projektoperationen innerhalb der Anwendung.
 *
 * <br><br>
 * Diese Klasse stellt statische Hilfsmethoden bereit, um Projekte zu öffnen,
 * zuletzt verwendete Verzeichnisse auszuwählen sowie das aktuell geladene
 * Projekt zu schließen. Dabei interagieren die Methoden mit der grafischen
 * Benutzeroberfläche ({@link Gui}) und kapseln wiederkehrende Abläufe, wie das
 * Anzeigen von Dialogen oder das Laden von Bilddaten.
 *
 * <br><br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class ProjectUtils {
    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private ProjectUtils() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Öffnet einen Dialog, in dem der Benutzer ein Projektverzeichnis auswählen kann,
     * und lädt die enthaltenen Bilddateien in die Anwendung.
     *
     * <p>
     * Es wird ein Directory-Chooser angezeigt. Wählt der Nutzer ein gültiges Verzeichnis
     * aus, werden die Bilddateien anhand des ausgewählten {@link ImageFileType} geladen.
     * Bei Erfolg wird die GUI aktiviert, andernfalls wird der Ladevorgang abgebrochen.
     *
     * @param gui Die Gui-Instanz, über die Dialoge angezeigt und Ladeprozesse gesteuert werden
     */
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

    /**
     * Zeigt dem Benutzer eine Liste der zuletzt geöffneten Projekte an und ermöglicht
     * die Auswahl eines Verzeichnisses zum erneuten Laden.
     *
     * <p>
     * Die Liste basiert auf dem internen Verlauf der geöffneten Projekte. Wird ein
     * Eintrag ausgewählt, werden die Bilddaten des entsprechenden Verzeichnisses
     * geladen. Falls kein Eintrag ausgewählt wird, bricht der Vorgang ab.
     *
     * @param gui Die Gui-Instanz, über die die Auswahl angezeigt und die Projektbilder geladen werden
     */
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

    /**
     * Fragt den Benutzer, ob das aktuell geladene Projekt geschlossen werden soll,
     * und führt bei Zustimmung die notwendigen Schritte zum Bereinigen der GUI aus.
     *
     * <p>
     * Die Methode zeigt einen Bestätigungsdialog an. Bestätigt der Nutzer, werden
     * alle geladenen Bilder aus der GUI entfernt und die Benutzeroberfläche deaktiviert.
     *
     * @param gui Die Gui-Instanz, deren aktuelles Projekt geschlossen und zurückgesetzt wird
     */
    public static void closeProject(Gui gui) {
        int option = JOptionPane.showConfirmDialog(gui.getContainer(),
            getWord("dialog.loadingImages.closeProject"),
            getWord("optionPane.title.confirm"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            logger.info("Clear Images ...");

            gui.handleAction(ActionType.SHORTCUT_CLOSE_PROJECT);
            gui.toggleOff();
        }
    }

    // ========================================
    // Hilfsmethoden
    // ========================================

    /**
     * Lädt Bilddateien aus einem angegebenen Verzeichnis und verarbeitet dabei
     * unterschiedliche Szenarien wie leere Verzeichnisse, fehlende Dateien oder
     * bereits geladene Verzeichnisse.
     *
     * <p>
     * Vorhandene Dateien innerhalb des Verzeichnisses werden erkannt und die GUI
     * entsprechend aktiviert. Bei Fehlerfällen oder ungültigen Eingaben wird
     * der Benutzer über Dialogfenster informiert, außer {@code isGuiBeingBuilt} ist
     * {@code true}, dann werden Fehler stumm unterdrückt.
     *
     * @param gui             Die Gui-Instanz, die den Ladevorgang steuert
     * @param imagesDirectory Das zu ladende Bildverzeichnis (oder Datei innerhalb des Verzeichnisses)
     * @param imageFileType   Der zu verwendende Bildtyp oder {@code null} für automatische Erkennung
     * @param isGuiBeingBuilt Verhindert Fehlermeldungen während des GUI-Aufbaus
     * @return {@code true}, wenn das Laden erfolgreich war; andernfalls {@code false}
     */
    public static boolean loadImagesDirectory(Gui gui, Path imagesDirectory, ImageFileType imageFileType, boolean isGuiBeingBuilt) {
        // Wenn eine "gültige Datei" übergeben wird, wird ins Elternverzeichnis navigiert
        imagesDirectory = imagesDirectory != null && Files.isRegularFile(imagesDirectory) ? imagesDirectory.getParent() : imagesDirectory;

        // Prüfe, ob das Verzeichnis passende Bilder hat
        LoadingResult result = new DialogLoadingImages(gui.getContainer()).load(imagesDirectory, imageFileType);

        switch (result) {
            case LOADING_SUCCESSFUL -> {
                gui.toggleOn();

                // Das neue Projekt wird immer bei Bild 0, Ebene 0 geöffnet
                gui.update(Axis.TIME);
                gui.update(Axis.LEVEL);
                return true;
            }
            case DIRECTORY_ALREADY_LOADED -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("result.directoryAlreadyLoaded").formatted(imageFileType.getType(), imagesDirectory);
                JOptionPane.showMessageDialog(
                    gui.getContainer(),
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
            case DIRECTORY_DOES_NOT_EXIST -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("result.directoryDoesNotExist").formatted(imagesDirectory);
                JOptionPane.showMessageDialog(
                    gui.getContainer(),
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
            case DIRECTORY_HAS_NO_IMAGES -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("result.directoryHasNoImages").formatted(imagesDirectory, imageFileType.getType());
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
}
