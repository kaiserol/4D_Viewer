package de.uzk.config;

import de.uzk.gui.GuiUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uzk.Main.*;

public class ScreenshotHelper {

    private static final Path SCREENSHOT_DIRECTORY = Path.of("screenshots");

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM");
    private static final String DATE_FORMAT_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    public static boolean saveScreenshot(BufferedImage originalImage) {
        try {
            if (!Files.exists(SCREENSHOT_DIRECTORY)) {
                Files.createDirectories(SCREENSHOT_DIRECTORY);
            }

            if (Files.isDirectory(SCREENSHOT_DIRECTORY)) {
                String date = DATE_FORMAT.format(new Date());
                int count = getNextScreenshotIndex(date);

                Path fileName = Path.of(date, String.valueOf(count), imageFileHandler.getImageFile().getName());
                Path saveFile = SCREENSHOT_DIRECTORY.toAbsolutePath().resolve(fileName);

                BufferedImage edited = GuiUtils.getEditedImage(originalImage, false);
                ImageIO.write(edited, settings.getFileNameExt().getType(), saveFile.toFile());
                logger.info("Saved Screenshot under: '" + saveFile.toAbsolutePath() + "'.");
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
            try (DirectoryStream<Path> files = Files.newDirectoryStream(SCREENSHOT_DIRECTORY)) {
                String fileNamePattern = date + "\\(\\d+\\)_" + imageFileHandler.getFileNamePattern();
                for (Path file : files) {
                    String filename = file.getFileName().toString();
                    if (filename.matches(fileNamePattern)) {
                        int indexStart = filename.indexOf("(") + 1;
                        int indexEnd = filename.indexOf(")");

                        int count = Integer.parseInt(filename.substring(indexStart, indexEnd)) + 1;
                        if (count > index) index = count;
                    }
                }
            } catch (IOException e) {
                return index;
            }
        }
        return index;
    }

    public static int getScreenshotCount() {
        int count = 0;
        if (Files.isDirectory(SCREENSHOT_DIRECTORY) &&  imageFileHandler != null) {
            try(DirectoryStream<Path> files = Files.newDirectoryStream(SCREENSHOT_DIRECTORY)) {
                String filePattern = DATE_FORMAT_PATTERN + "\\(\\d+\\)_" + imageFileHandler.getFileNamePattern();
                for (Path file : files) {
                    String filename = file.getFileName().toString();
                    if (filename.matches(filePattern)) count++;
                }
            } catch (IOException e) {
                return count;
            }


        }
        return count;
    }
}
