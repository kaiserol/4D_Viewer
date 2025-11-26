package de.uzk.io;

import de.uzk.utils.DateTimeUtils;
import de.uzk.utils.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    private static final int DEFAULT_MAX_LOG_AGE_DAYS = 30;

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private LogsHelper() {
        // Verhindert die Instanziierung dieser Klasse
    }

    public static void writeInLogFile(String logEntry) {
        if (logEntry == null || logEntry.isBlank()) return;

        Path filePath = loadLogFile();
        if (filePath == null) return;

        try (FileWriter writer = new FileWriter(filePath.toFile(), true)) {
            writer.write(logEntry + StringUtils.NEXT_LINE);
        } catch (IOException ex) {
            logger.warn("Could not write in the log-file '%s'.".formatted(filePath.toAbsolutePath()));
        }
    }

    public static void cleanupOldLogs(int daysToKeep) {
        Path logsDirectory = PathManager.getLogsDirectory();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(logsDirectory)) {
            String fileNamePattern = PathManager.LOG_FILE_NAME_PATTERN.formatted(DateTimeUtils.DATE_ONLY_PATTERN);

            // Durchlaufe alle Pfade
            for (Path path : paths) {
                String fileName = path.getFileName().toString();

                // Prüft, ob der Pfad eine reguläre Datei ist und der Name dem Muster entspricht
                if (Files.isRegularFile(path) && fileName.matches(fileNamePattern)) {
                    deleteLogFileIfOld(path, daysToKeep);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to stream the logs-directory '%s'.".formatted(logsDirectory.toAbsolutePath()));
        }
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public static void cleanUpOldLogs() {
        cleanupOldLogs(DEFAULT_MAX_LOG_AGE_DAYS);
    }

    private static Path loadLogFile() {
        // Dateiname bauen
        String formattedDate = DateTimeUtils.getFormattedDateToday();
        String logFileName = PathManager.LOG_FILE_NAME_PATTERN.formatted(formattedDate);
        Path filePath = PathManager.resolveLogsPath(Path.of(logFileName));

        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                logger.warn("Could not create the log-file '%s'.".formatted(filePath.toAbsolutePath()));
                return null;
            }
        }
        return filePath;
    }

    private static void deleteLogFileIfOld(Path path, int daysToKeep) {
        // Datum finden und parsen (Abbrechen, wenn kein gültiges Datum gefunden wurde)
        String fileName = path.getFileName().toString();
        LocalDate fileDate = DateTimeUtils.parseDate(fileName);
        if (fileDate == null) return;

        // Schwellenwert erstellen
        LocalDate today = DateTimeUtils.dateToday();
        LocalDate threshold = today.minusDays(daysToKeep);

        // Prüfen, ob Datei zu alt ist
        if (fileDate.isBefore(threshold)) {
            long daysOld = ChronoUnit.DAYS.between(fileDate, today);
            try {
                logger.info("Deleting the log-file '%s' ... (%d days old, threshold: %d days)".formatted(path.toAbsolutePath(), daysOld, daysToKeep));
                Files.deleteIfExists(path);
            } catch (IOException e) {
                logger.warn("Could not delete the log-file '%s'.".formatted(path.toAbsolutePath()));
            }
        }
    }
}
