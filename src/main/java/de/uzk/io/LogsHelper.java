package de.uzk.io;

import de.uzk.utils.DateTimeUtils;
import de.uzk.utils.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static de.uzk.Main.logger;

/**
 * Hilfsklasse zum Bereinigen alter Logdateien.
 * <p>
 * Löscht automatisch alle Dateien im {@code Protokollverzeichnis},
 * deren Änderungsdatum älter als die angegebene Anzahl an Tagen ist.
 *
 * <br><br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class LogsHelper {
    private static final LocalDate START_DATE = LocalDate.now();

    /**
     * Standard: 14 Tage
     */
    private static final int DEFAULT_MAX_LOG_AGE_DAYS = 14;

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private LogsHelper() {
        // Verhindert die Instanziierung dieser Klasse
    }

    public static void writeInLogFile(String logEntry) {
        if (logEntry == null || logEntry.isBlank()) return;

        Path filePath = PathManager.loadLogFile(false);
        if (filePath == null) return;

        try (FileWriter writer = new FileWriter(filePath.toFile(), true)) {
            writer.write(logEntry + StringUtils.NEXT_LINE);
        } catch (IOException ex) {
            logger.error(String.format("Failed writing to file '%s'", filePath.toAbsolutePath()));
        }
    }

    public static void cleanupOldLogs(int daysToKeep) {
        Path logsDirectory = PathManager.getLogsDirectory();

        // Schwellenwert
        LocalDate threshold = LocalDate.now().minusDays(daysToKeep);

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(logsDirectory)) {
            String fileNamePattern = PathManager.LOG_FILE_NAME_PATTERN.formatted(DateTimeUtils.DATE_ONLY_PATTERN);

            // Durchlaufe alle Pfade
            for (Path path : paths) {
                String fileName = path.getFileName().toString();

                // Prüft, ob der Pfad eine reguläre Datei ist und der Name dem Muster entspricht
                if (Files.isRegularFile(path) && fileName.matches(fileNamePattern)) deleteIfOld(path, threshold);
            }
        } catch (IOException e) {
            logger.error(String.format("Failed to scan logs directory '%s'", logsDirectory.toAbsolutePath()));
        }
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public static void cleanUpOldLogs() {
        cleanupOldLogs(DEFAULT_MAX_LOG_AGE_DAYS);
    }

    private static void deleteIfOld(Path path, LocalDate threshold) {
        // TODO: zu implementieren
    }
}
