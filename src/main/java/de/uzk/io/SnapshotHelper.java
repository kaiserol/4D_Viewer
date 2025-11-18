package de.uzk.io;

import de.uzk.gui.SnapshotCropper;
import de.uzk.utils.DateTimeUtils;
import de.uzk.utils.NumberUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public final class SnapshotHelper {
    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private SnapshotHelper() {
        // Verhindert die Instanziierung dieser Klasse
    }

    public static boolean saveSnapshot(BufferedImage image) {
        if (image == null || !workspace.isLoaded()) return false;
        Path directory = PathManager.resolveProjectPath(PathManager.SNAPSHOTS_DIRECTORY);
        PathManager.createIfNotExist(directory);

        // Bild zuschneiden
        SnapshotCropper cropper = new SnapshotCropper(image);
        int option = JOptionPane.showConfirmDialog(null, cropper, getWord("dialog.snapshot"), JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return false;
        image = cropper.getCroppedImage();

        // Dateiname bauen
        Path filePath = buildSnapshotFile(directory);
        logger.info(String.format("Saving snapshot as '%s'", filePath.toAbsolutePath()));

        try {
            ImageIO.write(image, workspace.getConfig().getImageFileType().getType(), filePath.toFile());
            return true;
        } catch (IOException e) {
            logger.error(String.format("Failed saving snapshot '%s'", filePath.toAbsolutePath()));
        }
        return false;
    }

    public static int getSnapshotsCount() {
        if (!workspace.isLoaded()) return 0;

        // Wenn das Verzeichnis nicht existiert, wird 0 zurückgegeben
        Path directory = PathManager.resolveProjectPath(PathManager.SNAPSHOTS_DIRECTORY);
        if (!Files.isDirectory(directory)) {
            PathManager.createIfNotExist(directory);
            return 0;
        }

        int count = 0;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) {
            String fileNamePattern = DateTimeUtils.DATE_PATTERN + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Pfade
            for (Path path : paths) {
                String fileName = path.getFileName().toString();

                // Prüft, ob der Pfad eine reguläre Datei ist und der Name dem Muster entspricht
                if (Files.isRegularFile(path) && fileName.matches(fileNamePattern)) count++;
            }
        } catch (IOException e) {
            logger.error(String.format("Failed getting snapshot count in the directory '%s'", directory));
        }
        return count;
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private static Path buildSnapshotFile(Path directory) {
        String formattedDate = DateTimeUtils.formatDateTime(DateTimeUtils.DATE_FORMAT);
        int count = getNextSnapshotIndex(directory, formattedDate);

        String imageFileName = workspace.getCurrentImageFile().getFileName();
        String snapshotFileName = "%s(%02d)_%s".formatted(formattedDate, count, imageFileName);
        return directory.resolve(snapshotFileName);
    }

    private static int getNextSnapshotIndex(Path directory, String date) {
        if (!Files.isDirectory(directory)) return 1;

        int index = 1;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) {
            String fileNamePattern = date + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Pfade
            for (Path path : paths) {
                String fileName = path.getFileName().toString();

                // Prüft, ob der Pfad eine reguläre Datei ist und der Name dem Muster entspricht
                if (Files.isRegularFile(path) && fileName.matches(fileNamePattern)) {
                    int startIndex = fileName.indexOf("(") + 1;
                    int endIndex = fileName.indexOf(")");
                    int count = NumberUtils.parseInteger(fileName.substring(startIndex, endIndex)) + 1;
                    if (count > index) index = count;
                }
            }
        } catch (IOException e) {
            logger.error(String.format("Failed getting next snapshot index in the directory '%s'", directory.toAbsolutePath()));
        }
        return index;
    }
}
