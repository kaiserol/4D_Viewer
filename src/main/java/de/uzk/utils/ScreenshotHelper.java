package de.uzk.utils;

import de.uzk.gui.GuiUtils;

import javax.imageio.ImageIO;
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
import static de.uzk.utils.AppPath.SNAPSHOTS_DIRECTORY_NAME;
import static de.uzk.utils.AppPath.getAppProjectPath;

public class ScreenshotHelper {
    // Format und Pattern
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM");
    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    public static boolean saveScreenshot(BufferedImage image) {
        if (image == null || !workspace.isOpen()) return false;
        Path directory = getAppProjectPath(Path.of(SNAPSHOTS_DIRECTORY_NAME));
        String fileName = workspace.getImageFile().getFileName();
        AppPath.createIfNotExist(directory);

        // Dateiname bauen
        String date = DATE_FORMAT.format(new Date());
        int count = getNextScreenshotIndex(date);
        String newFileName = "%s(%02d)_%s".formatted(date, count, fileName);
        Path filePath = directory.resolve(newFileName);
        logger.info(String.format("Saving snapshot to '%s'", filePath));

        try {
            // Bild bearbeiten und speichern
            BufferedImage editedImage = GuiUtils.getEditedImage(image, false);
            ImageIO.write(editedImage, workspace.getConfig().getImageFileType().getType(), filePath.toFile());
            return true;
        } catch (IOException e) {
            logger.error(String.format("Failed saving snapshot to '%s'", filePath));
        }
        return false;
    }

    private static int getNextScreenshotIndex(String date) {
        Path directory = getAppProjectPath(Path.of(SNAPSHOTS_DIRECTORY_NAME));
        if (!Files.exists(directory)) return 1;

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
            logger.error(String.format("Failed getting next snapshot index in '%s'", directory));
        }
        return index;
    }

    public static int getScreenshotCount() {
        Path directory = getAppProjectPath(Path.of(SNAPSHOTS_DIRECTORY_NAME));
        if (!Files.exists(directory) || !workspace.isOpen()) return 0;

        int count = 0;
        try (DirectoryStream<Path> filePaths = Files.newDirectoryStream(directory)) {
            String fileNamePattern = DATE_PATTERN + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

            // Durchlaufe alle Dateien
            for (Path filePath : filePaths) {
                String fileName = filePath.getFileName().toString();

                // Prüfe, ob der Dateiname dem Muster entspricht
                if (fileName.matches(fileNamePattern)) count++;
            }
        } catch (IOException e) {
            logger.error(String.format("Failed getting snapshot count in '%s'", directory));
        }
        return count;
    }
}
