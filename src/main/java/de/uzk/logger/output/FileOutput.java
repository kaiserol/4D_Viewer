package de.uzk.logger.output;

import de.uzk.io.LogsHelper;
import de.uzk.logger.LogEntry;
import de.uzk.logger.LogLevel;
import org.jetbrains.annotations.NotNull;

/**
 * Implementierung eines {@link LogOutput}, die {@link LogEntry}-Objekte
 * zeilenweise in eine Logdatei schreibt.
 */
public class FileOutput implements LogOutput {

    /**
     * Schreibt den übergebenen {@link LogEntry} in die Logdatei.
     *
     * <p>
     * Logeinträge mit der Protokollebene {@link LogLevel#DEBUG} werden
     * ignoriert und nicht in die Datei geschrieben.
     *
     * @param entry Der zu schreibende Logeintrag; darf nicht {@code null} sein
     */
    @Override
    public void write(@NotNull LogEntry entry) {
        if (entry.getLevel() == LogLevel.DEBUG) return;
        String result = String.join("", entry.formatEntry(true));
        LogsHelper.writeInLogFile(result);
    }
}