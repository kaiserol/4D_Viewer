package de.uzk.utils;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;

/**
 * Die Klasse {@code PathManager} verwaltet sämtliche Verzeichnispfade und Dateistrukturen
 * der Anwendung. Sie sorgt automatisch für das Erstellen der benötigten Ordner.
 * <p>
 * Struktur im Benutzerverzeichnis:
 * <pre>
 *  ~/4D_Viewer/
 *  ├── .config/
 *      ├── history.txt
 *      └── settings.json
 *  ├── projects/
 *  │   └── &lt;ProjektName&gt;/
 *  │       ├── config.json
 *  │       ├── markers.json
 *  │       └── snapshots/
 * </pre>
 */
public final class PathManager {

    // Systemverzeichnisse
    public static final Path USER_WORKING_DIRECTORY = Path.of(System.getProperty("user.dir"));
    public static final Path USER_HOME_DIRECTORY = Path.of(System.getProperty("user.home"));

    // Programmverzeichnisse
    public static final Path RESOURCES_DIRECTORY = Paths.get("src/main/resources");

    // Ordner- und Dateinamen im Ressourcenverzeichnis
    public static final String PROPERTIES_FILE_NAME_PATTERN = "*.properties";

    // Ordner- und Dateinamen im App-Verzeichnis
    public static final String APP_DIRECTORY_NAME = "4D_Viewer";
    public static final String CONFIG_DIRECTORY_NAME = ".config";
    public static final String PROJECTS_DIRECTORY_NAME = "projects";
    public static final String SETTINGS_FILE_NAME = "settings.json";
    public static final String HISTORY_FILE_NAME = "history.txt";

    // Ordner- und Dateinamen im Projekte-Verzeichnis
    public static final String SNAPSHOTS_DIRECTORY_NAME = "snapshots";
    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String MARKERS_FILE_NAME = "markers.json";

    // Statische Initialisierung
    static {
        Path appDirectory = getAppRoot();
        createIfNotExist(appDirectory);
        createIfNotExist(appDirectory.resolve(CONFIG_DIRECTORY_NAME));
        createIfNotExist(appDirectory.resolve(PROJECTS_DIRECTORY_NAME));
    }

    // ========================================
    // Pfad Getter
    // ========================================
    private static Path getAppRoot() {
        return USER_HOME_DIRECTORY.resolve(APP_DIRECTORY_NAME);
    }

    private static Path getAppConfigPath() {
        return getAppRoot().resolve(CONFIG_DIRECTORY_NAME);
    }

    private static Path getAppProjectsPath() {
        return getAppRoot().resolve(PROJECTS_DIRECTORY_NAME);
    }

    public static Path resolveInAppConfigPath(Path relativePath) {
        return getAppConfigPath().resolve(relativePath);
    }

    public static Path resolveInAppProjectsPath(Path relativePath) {
        if (workspace.getImageFilesDirectory() == null) {
            throw new NullPointerException("The image files directory is null.");
        }

        // Erstelle ein Projektverzeichnis, falls es noch nicht existiert
        Path projectName = workspace.getImageFilesDirectory().getFileName();
        Path projectPath = getAppProjectsPath().resolve(projectName);
        createIfNotExist(projectPath);

        return projectPath.resolve(relativePath);
    }

    // ========================================
    // Speichern Methoden
    // ========================================
    public static void saveJson(Path jsonFile, Object object) {
        Path directory = jsonFile.getParent();
        String fileBaseName = getFileBaseName(jsonFile.getFileName());

        createIfNotExist(directory);
        logger.info(String.format("Saving %s file '%s'", fileBaseName, jsonFile));

        try {
            // Benutzerdefinierte Einrückungen mit Printer erstellen
            ObjectMapper mapper = new ObjectMapper();

            // Datei schreiben
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, object);
        } catch (Exception e) {
            logger.error(String.format("Failed saving %s file '%s'", fileBaseName, jsonFile));
        }
    }

    public static void saveFile(Path file, List<String> lines) {
        Path directory = file.getParent();
        String fileBaseName = getFileBaseName(file.getFileName());

        createIfNotExist(directory);
        logger.info(String.format("Saving %s file '%s'", fileBaseName, file));

        try {
            Files.write(file, lines);
        } catch (Exception e) {
            logger.error(String.format("Failed saving %s file '%s'", fileBaseName, file));
        }
    }

    // ========================================
    // Laden Methoden
    // ========================================
    public static Object loadJson(Path jsonFile, Class<?> clazz) {
        String fileBaseName = getFileBaseName(jsonFile.getFileName());

        try {
            if (Files.exists(jsonFile)) {
                logger.info(String.format("Loading %s file '%s'", fileBaseName, jsonFile));
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(jsonFile.toFile(), clazz);
            }
        } catch (Exception e) {
            logger.error(String.format("Failed loading %s file '%s'", fileBaseName, jsonFile));
        }
        return null;
    }

    public static List<String> loadFile(Path file) {
        String fileBaseName = getFileBaseName(file.getFileName());

        try {
            if (Files.exists(file)) {
                logger.info(String.format("Loading %s file '%s'", fileBaseName, file));
                return Files.readAllLines(file);
            }
        } catch (Exception e) {
            logger.error(String.format("Failed loading %s file '%s'", fileBaseName, file));
        }
        return null;
    }

    // ========================================
    //  Hilfsmethoden
    // ========================================
    public static void createIfNotExist(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                String directoryName = switch (directory.getFileName().toString()) {
                    case APP_DIRECTORY_NAME -> "app";
                    case CONFIG_DIRECTORY_NAME -> "config";
                    case PROJECTS_DIRECTORY_NAME -> "projects";
                    case SNAPSHOTS_DIRECTORY_NAME -> "snapshots";
                    default -> directory.getFileName().toString();
                };
                logger.error(String.format("Failed creating %s directory '%s'", directoryName, directory));
            }
        }
    }

    public static String getFileBaseName(Path file) {
        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
