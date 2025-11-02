package de.uzk.utils;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;

public class AppPath {
    // Pfade
    public static final Path USER_WORKING_DIRECTORY = Path.of(System.getProperty("user.dir"));
    public static final Path USER_HOME_DIRECTORY = Path.of(System.getProperty("user.home"));

    // Verzeichnis Namen
    public static final String APP_DIRECTORY_NAME = ".4D_Viewer";
    public static final String SNAPSHOTS_DIRECTORY_NAME = "snapshots";

    // Datei Namen (im App Verzeichnis)
    public static final String SETTINGS_FILE_NAME = "settings.json";
    public static final String HISTORY_FILE_NAME = "history.txt";

    // Datei Namen (im Projekt Verzeichnis)
    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String MARKERS_FILE_NAME = "markers.json";

    static {
        Path appDirectory = USER_HOME_DIRECTORY.resolve(APP_DIRECTORY_NAME);
        createIfNotExist(appDirectory);
    }

    public static String getFileBaseName(Path fileName) {
        int dotIndex = fileName.getFileName().toString().lastIndexOf(".");
        if (dotIndex == -1) return fileName.getFileName().toString();
        else return fileName.getFileName().toString().substring(0, dotIndex);
    }

    public static void createIfNotExist(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                String directoryName = switch (directory.getFileName().toString()) {
                    case APP_DIRECTORY_NAME -> "home";
                    case SNAPSHOTS_DIRECTORY_NAME -> "snapshots";
                    default -> directory.getFileName().toString();
                };
                logger.error(String.format("Failed creating %s directory '%s'", directoryName, directory));
            }
        }
    }

    public static Path getAppPath(Path path) {
        Path homeDirectory = USER_HOME_DIRECTORY.resolve(APP_DIRECTORY_NAME);
        return homeDirectory.resolve(path);
    }

    public static Path getAppProjectPath(Path path) {
        if (workspace.getImageFilesDirectory() == null) throw new NullPointerException("The image files directory is null.");

        Path appDirectory = USER_HOME_DIRECTORY.resolve(APP_DIRECTORY_NAME);
        Path projectDirectory = workspace.getImageFilesDirectory().getFileName();
        return appDirectory.resolve(projectDirectory).resolve(path);
    }

    public static void saveJson(Path jsonPath, Object object) {
        Path directory = jsonPath.getParent();
        String fileBaseName = getFileBaseName(jsonPath.getFileName());

        createIfNotExist(directory);
        logger.info(String.format("Saving %s file '%s'", fileBaseName, jsonPath));

        try {
            // Benutzerdefinierte Einrückungen mit Printer erstellen
            ObjectMapper mapper = new ObjectMapper();

            // Datei schreiben
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath, object);
        } catch (Exception e) {
            logger.error(String.format("Failed saving %s file '%s'", fileBaseName, jsonPath));
        }
    }

    public static void saveFile(Path filePath, List<String> lines) {
        Path directory = filePath.getParent();
        String fileBaseName = getFileBaseName(filePath.getFileName());

        createIfNotExist(directory);
        logger.info(String.format("Saving %s file '%s'", fileBaseName, filePath));

        try {
            Files.write(filePath, lines);
        } catch (Exception e) {
            logger.error(String.format("Failed saving %s file '%s'", fileBaseName, filePath));
        }
    }

    public static Object loadJson(Path jsonPath, Class<?> clazz) {
        String fileBaseName = getFileBaseName(jsonPath.getFileName());

        try {
            if (Files.exists(jsonPath)) {
                logger.info(String.format("Loading %s file '%s'", fileBaseName, jsonPath));
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(jsonPath.toFile(), clazz);
            }
        } catch (Exception e) {
            logger.error(String.format("Failed loading %s file '%s'", fileBaseName, jsonPath));
        }
        return null;
    }

    public static List<String> loadFile(Path filePath) {
        String fileBaseName = getFileBaseName(filePath.getFileName());

        try {
            if (Files.exists(filePath)) {
                logger.info(String.format("Loading %s file '%s'", fileBaseName, filePath));
                return Files.readAllLines(filePath);
            }
        } catch (Exception e) {
            logger.error(String.format("Failed loading %s file '%s'", fileBaseName, filePath));
        }
        return null;
    }
}
