package de.uzk.logger.output;

import de.uzk.logger.LogEntry;
import de.uzk.logger.LogLevel;
import de.uzk.utils.StringUtils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Schreibt {@link LogEntry}-Daten zeilenweise in eine Logdatei.
 */
public class FileOutput implements LogOutput {
    /**
     * Standard-Dateiname für die Logdatei.
     */
    private static final String FILE_NAME = "app.log";

    /**
     * Gibt den formatierten Logeintrag in der Log-Datei aus. Ignoriert die Debug-Protokollebene.
     *
     * @param entry Der zu schreibende Logeintrag
     */
    @Override
    public void write(LogEntry entry) {
        if (entry.getLevel() == LogLevel.DEBUG) return;

        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            String result = String.join("", entry.formatEntry(true));
            writer.write(result + StringUtils.NEXT_LINE);
        } catch (IOException ex) {
            System.err.println("Failed writing to file '" + FILE_NAME + "'");
        }
    }
}