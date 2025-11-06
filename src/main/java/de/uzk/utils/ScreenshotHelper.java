package de.uzk.utils;

import de.uzk.gui.ScreenshotCropper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.utils.PathManager.SNAPSHOTS_DIRECTORY_NAME;
import static de.uzk.utils.PathManager.resolveInAppProjectsPath;

public class ScreenshotHelper {
    // Format und Pattern
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM");
    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    public static boolean saveScreenshot(BufferedImage image) {
        if (image == null || !workspace.isOpen()) return false;
        Path directory = resolveInAppProjectsPath(Path.of(SNAPSHOTS_DIRECTORY_NAME));

        ScreenshotCropper cropper = new ScreenshotCropper(image);
        int option = JOptionPane.showConfirmDialog(null, cropper, "Crop Image", JOptionPane.OK_CANCEL_OPTION);
        if(option != JOptionPane.OK_OPTION) return false;

        image = cropper.getCroppedImage();

        // Dateiname bauen
        String fileName = workspace.getImageFile().getFileName();
        String date = DATE_FORMAT.format(new Date());
        int count = getNextScreenshotIndex(date);
        String newFileName = "%s(%02d)_%s".formatted(date, count, fileName);
        Path filePath = directory.resolve(newFileName);
        logger.info(String.format("Saving snapshot '%s'", filePath));

        try {
            ImageIO.write(image, workspace.getConfig().getImageFileType().getType(), filePath.toFile());
            return true;
        } catch (IOException e) {
            logger.error(String.format("Failed saving snapshot '%s'", filePath));
        }
        return false;
    }

    private static int getNextScreenshotIndex(String date) {
        Path directory = resolveInAppProjectsPath(Path.of(SNAPSHOTS_DIRECTORY_NAME));
        if (!Files.isDirectory(directory)) return 1;

        int index = 1;
        try (DirectoryStream<Path> filePaths = Files.newDirectoryStream(directory)) {
            String fileNamePattern = date + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Dateien
            for (Path filePath : filePaths) {
                String fileName = filePath.getFileName().toString();

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

    public static int getScreenshotCount() {
        if (!workspace.isOpen()) return 0; // Muss zuerst geprüft werden, da sonst NullPointerException
        Path directory = resolveInAppProjectsPath(Path.of(SNAPSHOTS_DIRECTORY_NAME));

        int count = 0;
        try (DirectoryStream<Path> filePaths = Files.newDirectoryStream(directory)) {
            String fileNamePattern = DATE_PATTERN + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Dateien
            for (Path filePath : filePaths) {
                String fileName = filePath.getFileName().toString();

                // Prüfe, ob der Dateiname dem Muster entspricht
                if (fileName.matches(fileNamePattern)) count++;
            }
        } catch (NoSuchFileException e) {
            // Per se kein Fehler, z. B. bei erstmals geöffneten Workspaces
            logger.info(String.format("The snapshots directory '%s' does not exist yet, creating it...", directory));
            PathManager.createIfNotExist(directory);
        } catch (IOException e) {
            logger.error(String.format("Failed getting snapshot count in the directory '%s'", directory));
        }
        return count;
    }
}
