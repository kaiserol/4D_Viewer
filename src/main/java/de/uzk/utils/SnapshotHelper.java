package de.uzk.utils;

import de.uzk.gui.ScreenshotCropper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.utils.PathManager.SNAPSHOTS_DIRECTORY;
import static de.uzk.utils.PathManager.resolveProjectPath;

public class SnapshotHelper {
    // Format und Pattern
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM");
    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    public static boolean saveSnapshot(BufferedImage image) {
        if (image == null || !workspace.isOpen()) return false;
        Path directory = resolveProjectPath(SNAPSHOTS_DIRECTORY);
        PathManager.createIfNotExist(directory);

        // Bild zuschneiden
        ScreenshotCropper cropper = new ScreenshotCropper(image);
        int option = JOptionPane.showConfirmDialog(null, cropper, "Crop Image", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return false;
        image = cropper.getCroppedImage();

        // Dateiname bauen
        Path file = buildSnapshotFile(directory);
        logger.info(String.format("Saving snapshot as '%s'", file));

        try {
            ImageIO.write(image, workspace.getConfig().getImageFileType().getType(), file.toFile());
            return true;
        } catch (IOException e) {
            logger.error(String.format("Failed saving snapshot '%s'", file));
        }
        return false;
    }

    public static int getSnapshotsCount() {
        if (!workspace.isOpen()) return 0;

        // Wenn das Verzeichnis nicht existiert, wird 0 zurückgegeben
        Path directory = resolveProjectPath(SNAPSHOTS_DIRECTORY);
        if (!Files.isDirectory(directory)) return 0;

        int count = 0;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
            String fileNamePattern = DATE_PATTERN + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Dateien
            for (Path file : files) {
                String fileName = file.getFileName().toString();

                // Prüfe, ob der Dateiname dem Muster entspricht
                if (fileName.matches(fileNamePattern)) count++;
            }
        } catch (IOException e) {
            logger.error(String.format("Failed getting snapshot count in the directory '%s'", directory));
        }
        return count;
    }

    // ========================================
    //  Hilfsmethoden
    // ========================================
    private static Path buildSnapshotFile(Path directory) {
        String fileName = workspace.getImageFile().getFileName();
        String date = DATE_FORMAT.format(new Date());
        int count = getNextSnapshotIndex(directory, date);

        String snapshotFileName = "%s(%02d)_%s".formatted(date, count, fileName);
        return directory.resolve(snapshotFileName);
    }

    private static int getNextSnapshotIndex(Path directory, String date) {
        if (!Files.isDirectory(directory)) return 1;

        int index = 1;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
            String fileNamePattern = date + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Dateien
            for (Path file : files) {
                String fileName = file.getFileName().toString();

                // Prüfe, ob der Dateiname dem Muster entspricht
                if (fileName.matches(fileNamePattern)) {
                    int indexStart = fileName.indexOf("(") + 1;
                    int indexEnd = fileName.indexOf(")");
                    int count = Integer.parseInt(fileName.substring(indexStart, indexEnd)) + 1;
                    if (count > index) index = count;
                }
            }
        } catch (IOException e) {
            logger.error(String.format("Failed getting next snapshot index in the directory '%s'", directory));
        }
        return index;
    }
}
