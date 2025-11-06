package de.uzk.utils;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

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

    // Systemverzeichnisse Pfade
    public static final Path USER_WORKING_DIRECTORY = Path.of(System.getProperty("user.dir"));
    public static final Path USER_HOME_DIRECTORY = Path.of(System.getProperty("user.home"));

    // Ressourcenverzeichnis Pfad
    public static final Path RESOURCES_DIRECTORY = Paths.get("src/main/resources");

    // Ordner- und Dateinamen im Ressourcenverzeichnis
    public static final String PROPERTIES_FILE_NAME_PATTERN = "*.properties";

    // Ordner und Dateinamen im App-Verzeichnis
    private static final Path APP_DIRECTORY = Path.of("4D_Viewer");
    private static final Path CONFIG_DIRECTORY = Path.of(".config");
    private static final Path PROJECTS_DIRECTORY = Path.of("projects");

    public static final Path SETTINGS_FILE_NAME = Path.of("settings.json");
    public static final Path HISTORY_FILE_NAME = Path.of("history.txt");

    // Ordner- und Dateinamen im Projekte-Verzeichnis
    public static final Path SNAPSHOTS_DIRECTORY = Path.of("snapshots");

    public static final Path CONFIG_FILE_NAME = Path.of("config.json");
    public static final Path MARKERS_FILE_NAME = Path.of("markers.json");

    // Statische Initialisierung
    static {
        Path appDirectory = getAppRoot();
        createIfNotExist(appDirectory);
        createIfNotExist(appDirectory.resolve(CONFIG_DIRECTORY));
        createIfNotExist(appDirectory.resolve(PROJECTS_DIRECTORY));
    }

    // ========================================
    // Pfad Erstellungsmethoden
    // ========================================
    private static Path getAppRoot() {
        return USER_HOME_DIRECTORY.resolve(APP_DIRECTORY);
    }

    private static Path getConfigPath() {
        return getAppRoot().resolve(CONFIG_DIRECTORY);
    }

    private static Path getProjectsPath() {
        return getAppRoot().resolve(PROJECTS_DIRECTORY);
    }

    public static Path resolveConfigPath(Path relativePath) {
        return getConfigPath().resolve(relativePath);
    }

    public static Path resolveProjectPath(Path relativePath) {
        if (workspace.getImageFilesDirectory() == null) {
            throw new NullPointerException("The image files directory is null.");
        }

        // Erstelle ein Projektverzeichnis, falls es noch nicht existiert
        Path projectName = workspace.getImageFilesDirectory().getFileName();
        Path projectPath = getProjectsPath().resolve(projectName);
        createIfNotExist(projectPath);

        return projectPath.resolve(relativePath);
    }

    // ========================================
    // Speichern Methode
    // ========================================
    public static void saveFile(Path file, Object data) {
        Path directory = file.getParent();
        String fileBaseName = getFileBaseName(file.getFileName());

        createIfNotExist(directory);
        logger.info(String.format("Saving %s file '%s'", fileBaseName, file.toAbsolutePath()));

        try {
            if (file.toString().endsWith(".json")) {
                // JSON-Datei
                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), data);
            } else if (data instanceof List<?> lines) {
                // Textdatei
                Files.write(file, lines.stream()
                    .map(Object::toString)
                    .toList());
            } else {
                throw new IllegalArgumentException("Unsupported data type for file: " + file);
            }
        } catch (Exception e) {
            logger.error(String.format("Failed saving %s file '%s'", fileBaseName, file.toAbsolutePath()));
        }
    }

    // ========================================
    // Laden Methode
    // ========================================
    public static Object loadFile(Path file, Class<?> clazz) {
        if (!Files.exists(file)) return null;

        String fileBaseName = getFileBaseName(file.getFileName());
        logger.info(String.format("Loading %s file '%s'", fileBaseName, file.toAbsolutePath()));

        try {
            if (file.toString().endsWith(".json")) {
                // JSON-Datei laden
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(file.toFile(), clazz);
            } else {
                // Textdatei laden
                return Files.readAllLines(file);
            }
        } catch (Exception e) {
            logger.error(String.format("Failed loading %s file '%s'", fileBaseName, file.toAbsolutePath()));
            return null;
        }
    }

    // ========================================
    //  Hilfsmethoden
    // ========================================
    public static void createIfNotExist(Path directory) {
        if (!Files.exists(directory)) {
            String directoryIdentifier = getDirectoryIdentifier(directory);
            try {
                logger.info(String.format("Creating %s directory '%s'", directoryIdentifier, directory.toAbsolutePath()));
                Files.createDirectories(directory);
            } catch (IOException e) {
                logger.error(String.format("Failed creating %s directory '%s'", directoryIdentifier, directory.toAbsolutePath()));
            }
        }
    }

    private static String getDirectoryIdentifier(Path directory) {
        String directoryName = directory.getFileName().toString();

        if (directoryName.equals(APP_DIRECTORY.getFileName().toString())) return "app";
        else if (directoryName.equals(CONFIG_DIRECTORY.getFileName().toString())) return "config";
        else if (directoryName.equals(PROJECTS_DIRECTORY.getFileName().toString())) return "projects";
        else if (directoryName.equals(SNAPSHOTS_DIRECTORY.getFileName().toString())) return "snapshots";
        else if (Objects.equals(workspace.getImageFilesDirectory(), directory)) return "project";
        return directoryName;
    }

    private static String getFileBaseName(Path file) {
        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
