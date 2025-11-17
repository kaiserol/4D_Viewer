package de.uzk.logger.output;

import de.uzk.logger.LogEntry;

/**
 * Schnittstelle für verschiedene Log-Ausgabeziele.
 */
public interface LogOutput {

    /**
     * Schreibt den angegebenen Logeintrag in das jeweilige Ausgabeziel.
     *
     * @param entry Der zu schreibende Logeintrag
     */
    void write(LogEntry entry);
}