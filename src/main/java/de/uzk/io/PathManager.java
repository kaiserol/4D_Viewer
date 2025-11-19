package de.uzk.io;

import de.uzk.utils.DateTimeUtils;
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
 *  │   ├── history.txt
 *  │   └── settings.json
 *  │
 *  ├── .logs/
 *  │   ├── &lt;Log-Datei&gt;
 *  │   └── ...
 *  │
 *  ├── projects/
 *  │   └── &lt;ProjektName&gt;/
 *  │       ├── snapshots/
 *  │       ├── config.json
 *  │       └── markers.json
 * </pre>
 */
public final class PathManager {
    // ---- Hauptpfade im Systemverzeichnis ----
    public static final Path USER_DIRECTORY = Path.of(System.getProperty("user.dir"));
    public static final Path USER_HOME_DIRECTORY = Path.of(System.getProperty("user.home"));

    // ---- Hauptpfade im Ressourcenverzeichnis ----
    public static final Path RESOURCES_DIRECTORY = Paths.get("src/main/resources");
    public static final String PROPERTIES_FILE_NAME_PATTERN = "*.properties";

    // ---- Pfade für das Appverzeichnis ----
    private static final Path APP_DIRECTORY = Path.of("4D_Viewer");
    private static final Path CONFIG_DIRECTORY = Path.of(".config");
    private static final Path LOGS_DIRECTORY = Path.of(".logs");
    private static final Path PROJECTS_DIRECTORY = Path.of("projects");
    private static final Path SNAPSHOTS_DIRECTORY = Path.of("snapshots");

    // ---- Pfade für das Konfigurationsverzeichnis ----
    public static final Path SETTINGS_FILE_NAME = Path.of("settings.json");
    public static final Path HISTORY_FILE_NAME = Path.of("history.txt");

    // ---- Pfade für das Protokollverzeichnis ----
    public static final String LOG_FILE_NAME_PATTERN = "logger_%s.log";

    // ---- Pfade für das Projektverzeichnis ----
    public static final Path CONFIG_FILE_NAME = Path.of("config.json");
    public static final Path MARKERS_FILE_NAME = Path.of("markers.json");

    // Statische Initialisierung
    static {
        Path appDirectory = getAppRoot();
        createIfNotExist(appDirectory);
        createIfNotExist(appDirectory.resolve(CONFIG_DIRECTORY));
        createIfNotExist(appDirectory.resolve(LOGS_DIRECTORY));
        createIfNotExist(appDirectory.resolve(PROJECTS_DIRECTORY));

        // Alte Log-Dateien löschen
        LogsHelper.cleanUpOldLogs();

        // Lade Log-Datei
        loadLogFile(true);
    }

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private PathManager() {
        // Verhindert die Instanziierung dieser Klasse
    }

    // ========================================
    // Hauptpfade
    // ========================================
    public static Path getAppRoot() {
        return USER_HOME_DIRECTORY.resolve(APP_DIRECTORY);
    }

    public static Path getConfigDirectory() {
        return getAppRoot().resolve(CONFIG_DIRECTORY);
    }

    public static Path getLogsDirectory() {
        return getAppRoot().resolve(LOGS_DIRECTORY);
    }

    public static Path getProjectsDirectory() {
        return getAppRoot().resolve(PROJECTS_DIRECTORY);
    }

    public static Path getProjectDirectory() {
        if (workspace.getImagesDirectory() == null) {
            throw new NullPointerException("The images directory is null.");
        }

        // Erstelle das Projektverzeichnis, falls es noch nicht existiert
        Path imagesDirectoryName = workspace.getImagesDirectory().getFileName();
        Path projectPath = getProjectsDirectory().resolve(imagesDirectoryName);
        createIfNotExist(projectPath);

        return projectPath;
    }

    public static Path getProjectSnapshotsDirectory() {
        return getProjectDirectory().resolve(SNAPSHOTS_DIRECTORY);
    }

    // ========================================
    // Pfad Auflösungen
    // ========================================
    public static Path resolveConfigPath(Path relativePath) {
        return getConfigDirectory().resolve(relativePath);
    }

    public static Path resolveLogsPath(Path relativePath) {
        return getLogsDirectory().resolve(relativePath);
    }

    public static Path resolveProjectPath(Path relativePath) {
        return getProjectDirectory().resolve(relativePath);
    }

    // ========================================
    // Speichern Methode
    // ========================================
    public static void save(Path filePath, Object data) {
        Path parentDirectory = filePath.getParent();
        createIfNotExist(parentDirectory);

        String fileBaseName = getFileBaseName(filePath.getFileName());
        logger.info(String.format("Saving %s file '%s'", fileBaseName, filePath.toAbsolutePath()));

        try {
            if (filePath.toString().endsWith(".json")) {
                // JSON-Datei
                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), data);
            } else if (data instanceof List<?> lines) {
                // Textdatei
                Files.write(filePath, lines.stream()
                    .map(Object::toString)
                    .toList());
            } else {
                throw new IllegalArgumentException("Unsupported data type for file: " + filePath.getFileName());
            }
        } catch (Exception e) {
            logger.error(String.format("Failed saving %s file '%s'", fileBaseName, filePath.toAbsolutePath()));
        }
    }

    // ========================================
    // Laden Methode
    // ========================================
    public static Object load(Path filePath, Class<?> clazz) {
        if (!Files.exists(filePath)) return null;

        String fileBaseName = getFileBaseName(filePath.getFileName());
        logger.info(String.format("Loading %s file '%s'", fileBaseName, filePath.toAbsolutePath()));

        try {
            if (filePath.toString().endsWith(".json")) {
                // JSON-Datei laden
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(filePath.toFile(), clazz);
            } else {
                // Textdatei laden
                return Files.readAllLines(filePath);
            }
        } catch (Exception e) {
            logger.error(String.format("Failed loading %s file '%s'", fileBaseName, filePath.toAbsolutePath()));
            return null;
        }
    }

    public static Path loadLogFile(boolean allowLoggerInfo) {
        // Dateiname bauen
        String formattedDate = DateTimeUtils.getFormattedDateToday();
        String logFileName = PathManager.LOG_FILE_NAME_PATTERN.formatted(formattedDate);
        Path filePath = PathManager.resolveLogsPath(Path.of(logFileName));

        if (allowLoggerInfo) logger.info(String.format("Loading logs file '%s'", filePath.toAbsolutePath()));
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                logger.error(String.format("Failed loading logs file '%s'", filePath.toAbsolutePath()));
                return null;
            }
        }
        return filePath;
    }

    // ========================================
    // Hilfsmethoden
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

        if (Objects.equals(directoryName, APP_DIRECTORY.getFileName().toString())) return "app";
        else if (Objects.equals(directoryName, CONFIG_DIRECTORY.getFileName().toString())) return "config";
        else if (Objects.equals(directoryName, LOGS_DIRECTORY.getFileName().toString())) return "logs";
        else if (Objects.equals(directoryName, PROJECTS_DIRECTORY.getFileName().toString())) return "projects";
        else if (Objects.equals(directoryName, SNAPSHOTS_DIRECTORY.getFileName().toString())) return "snapshots";
        else {
            if (workspace.getImagesDirectory() != null) {
                String workspaceDirectoryName = workspace.getImagesDirectory().getFileName().toString();
                if (Objects.equals(directoryName, workspaceDirectoryName)) return "project";
            }
        }

        return directoryName;
    }

    private static String getFileBaseName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
