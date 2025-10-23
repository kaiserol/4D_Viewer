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
    private static final Path SCREENSHOT_DIRECTORY = Path.of("screenshots");

    // Format und Pattern
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM");
    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    public static boolean saveScreenshot(BufferedImage originalImage) {
        try {
            if (!Files.exists(SCREENSHOT_DIRECTORY)) {
                Files.createDirectories(SCREENSHOT_DIRECTORY);
            }

            if (Files.isDirectory(SCREENSHOT_DIRECTORY)) {
                String date = DATE_FORMAT.format(new Date());
                int count = getNextScreenshotIndex(date);

                Path fileName = Path.of("%s(%02d)_%s".formatted(date, count, workspace.getImageFile().getName()));
                Path savePath = SCREENSHOT_DIRECTORY.toAbsolutePath().resolve(fileName);

                BufferedImage edited = GuiUtils.getEditedImage(originalImage, false);
                ImageIO.write(edited, settings.getFileNameExt().getType(), savePath.toFile());
                logger.info("Saved Screenshot under: '" + savePath.toAbsolutePath() + "'.");
                return true;
            }
        } catch (IOException e) {
            logger.logException(e);
        }
        return false;
    }

    private static int getNextScreenshotIndex(String date) {
        int index = 1;
        if (Files.isDirectory(SCREENSHOT_DIRECTORY)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(SCREENSHOT_DIRECTORY)) {
                String fileNamePattern = date + "\\(\\d+\\)_" + workspace.getFileNamePattern();

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
                logger.logException(e);
            }
        }
        return index;
    }

    public static int getScreenshotCount() {
        int count = 0;
        if (Files.isDirectory(SCREENSHOT_DIRECTORY)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(SCREENSHOT_DIRECTORY)) {
                String filePattern = DATE_PATTERN + "\\(\\d+\\)_" + workspace.getFileNamePattern();
                for (Path path : paths) {
                    String fileName = path.getFileName().toString();
                    if (fileName.matches(filePattern)) count++;
                }
            } catch (IOException e) {
                logger.logException(e);
            }
        }
        return count;
    }
}
