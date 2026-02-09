package de.uzk.io;

import de.uzk.gui.dialogs.DialogCropSnapshot;
import de.uzk.image.ImageFileType;
import de.uzk.utils.DateTimeUtils;
import de.uzk.utils.GraphicsUtils;
import de.uzk.utils.NumberUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;

public final class SnapshotHelper {
    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private SnapshotHelper() {
        // Verhindert die Instanziierung dieser Klasse
    }

    public static boolean saveSnapshot(BufferedImage image) {
        if (image == null || !workspace.isLoaded()) return false;

        Path snapshotsDirectory = PathManager.getProjectSnapshotsDirectory();

        // Bild zuschneiden
        DialogCropSnapshot dialogCropSnapshot = new DialogCropSnapshot(image);
        dialogCropSnapshot.setVisible(true);
        Optional<BufferedImage> result = dialogCropSnapshot.getCroppedImage();
         if (result.isEmpty()) return false;

        // Manche Formate (z.B. JPEG) unterstützen keine Transparenz.
        image = result.get();
        ImageFileType fileType = workspace.getConfig().getImageFileType();
        if (fileType == ImageFileType.GIF || fileType == ImageFileType.JPEG) {
            // Diese Formate unterstützen keine Transparenz (ARGB).
            // Der Speichervorgang scheitert bei transparenten Bildern dann, ohne eine Exception zu werfen.
            // Deshalb wandeln wir sicherheitshalber zu RGB um.
            image = GraphicsUtils.transformToRGB(image);
        }

        // Dateiname bauen
        Path filePath = buildSnapshotFile(snapshotsDirectory);
        logger.info("Saving the snapshot as '%s' ...".formatted(filePath.toAbsolutePath()));

        try {
            boolean success = ImageIO.write(image, fileType.getType(), filePath.toFile());
            if (success) return true;
        } catch (IOException ignore) {
        }

        logger.warn("Could not save the snapshot-file '%s'.".formatted(filePath.toAbsolutePath()));
        return false;
    }

    public static int getSnapshotsCount() {
        if (!workspace.isLoaded()) return 0;

        // Wenn das Verzeichnis nicht existiert, wird es erstellt und 0 zurückgegeben
        Path snapshotsDirectory = PathManager.getProjectSnapshotsDirectory();
        if (!Files.isDirectory(snapshotsDirectory)) {
            PathManager.createIfNotExist(snapshotsDirectory);
            return 0;
        }

        int count = 0;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(snapshotsDirectory)) {
            String snapShotNamePattern = DateTimeUtils.DATE_ONLY_PATTERN + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Pfade
            for (Path path : paths) {
                String fileName = path.getFileName().toString();

                // Prüft, ob der Pfad eine reguläre Datei ist und der Name dem Muster entspricht
                if (Files.isRegularFile(path) && fileName.matches(snapShotNamePattern)) count++;
            }
        } catch (IOException e) {
            logger.error("Failed to stream the snapshots-directory '%s'.".formatted(snapshotsDirectory.toAbsolutePath()));
        }
        return count;
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private static Path buildSnapshotFile(Path snapshotsDirectory) {
        String formattedDate = DateTimeUtils.getFormattedDateToday();
        int count = getNextSnapshotIndex(snapshotsDirectory, formattedDate);

        String imageFileName = workspace.getCurrentImageFile().getFileName();
        String snapshotFileName = "%s(%02d)_%s".formatted(formattedDate, count, imageFileName);
        return snapshotsDirectory.resolve(snapshotFileName);
    }

    private static int getNextSnapshotIndex(Path snapshotsDirectory, String date) {
        if (!Files.isDirectory(snapshotsDirectory)) return 1;

        int index = 1;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(snapshotsDirectory)) {
            String snapShotNamePattern = date + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Pfade
            for (Path path : paths) {
                String fileName = path.getFileName().toString();

                // Prüft, ob der Pfad eine reguläre Datei ist und der Name dem Muster entspricht
                if (Files.isRegularFile(path) && fileName.matches(snapShotNamePattern)) {
                    int startIndex = fileName.indexOf("(") + 1;
                    int endIndex = fileName.indexOf(")");
                    int count = NumberUtils.parseInteger(fileName.substring(startIndex, endIndex)) + 1;
                    if (count > index) index = count;
                }
            }
        } catch (IOException e) {
            logger.error("Failed to stream the snapshots-directory '%s'.".formatted(snapshotsDirectory.toAbsolutePath()));
        }
        return index;
    }
}
