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

import static de.uzk.Main.*;

public class ScreenshotHelper {
    // Pfade
    private static final Path SCREENSHOT_DIRECTORY_PATH = Path.of("screenshots");

    // Format und Pattern
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM");
    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    public static boolean saveScreenshot(BufferedImage image) {
        if (image == null || !workspace.isOpen()) return false;
        try {
            if (!Files.exists(SCREENSHOT_DIRECTORY_PATH)) {
                Files.createDirectories(SCREENSHOT_DIRECTORY_PATH);
            }

            if (Files.isDirectory(SCREENSHOT_DIRECTORY_PATH)) {
                String date = DATE_FORMAT.format(new Date());
                int count = getNextScreenshotIndex(date);

                String fileName = "%s(%02d)_%s".formatted(date, count, workspace.getImageFile().getName());
                Path path = SCREENSHOT_DIRECTORY_PATH.toAbsolutePath().resolve(fileName);

                BufferedImage editedImage = GuiUtils.getEditedImage(image, false);
                ImageIO.write(editedImage, workspace.getConfig().getImageFileType().getType(), path.toFile());
                logger.info("Saved Screenshot under '" + path.toAbsolutePath() + "'.");
                return true;
            }
        } catch (IOException e) {
            logger.error("Failed to save screenshot: " + e.getMessage());
        }
        return false;
    }

    private static int getNextScreenshotIndex(String date) {
        int index = 1;
        if (Files.isDirectory(SCREENSHOT_DIRECTORY_PATH)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(SCREENSHOT_DIRECTORY_PATH)) {
                String fileNamePattern = date + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

                for (Path path : paths) {
                    String fileName = path.getFileName().toString();
                    if (fileName.matches(fileNamePattern)) {
                        int indexStart = fileName.indexOf("(") + 1;
                        int indexEnd = fileName.indexOf(")");
                        int count = Integer.parseInt(fileName.substring(indexStart, indexEnd)) + 1;
                        if (count > index) index = count;
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to get next screenshot index: " + e.getMessage());
            }
        }
        return index;
    }

    public static int getScreenshotCount() {
        int count = 0;
        if (workspace.isOpen() && Files.isDirectory(SCREENSHOT_DIRECTORY_PATH)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(SCREENSHOT_DIRECTORY_PATH)) {
                String fileNamePattern = DATE_PATTERN + "\\(\\d+\\)_" + workspace.getImageFileNamePattern();

                for (Path path : paths) {
                    String fileName = path.getFileName().toString();
                    if (fileName.matches(fileNamePattern)) count++;
                }
            } catch (IOException e) {
                logger.error("Failed to get screenshot count: " + e.getMessage());
            }
        }
        return count;
    }
}
