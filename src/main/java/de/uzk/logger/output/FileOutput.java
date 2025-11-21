package de.uzk.logger.output;

import de.uzk.io.LogsHelper;
import de.uzk.logger.LogEntry;

/**
 * Implementierung eines {@link LogOutput}, die {@link LogEntry}-Objekte
 * zeilenweise in eine Logdatei schreibt.
 */
public class FileOutput extends LogOutput {

    /**
     * Schreibt den übergebenen {@link LogEntry} in die Logdatei.
     *
     * @param entry Der zu schreibende Logeintrag; darf nicht {@code null} sein
     */
    @Override
    public void writeInternal(LogEntry entry) {
        String result = String.join("", entry.formatEntry(true));
        LogsHelper.writeInLogFile(result);
    }
}