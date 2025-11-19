package de.uzk.logger.output;

import de.uzk.logger.LogEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Schnittstelle für verschiedene Log-Ausgabeziele.
 */
public interface LogOutput {

    /**
     * Schreibt den übergebenen {@code LogEntry} in das jeweilige Ausgabeziel.
     *
     * @param entry Der zu schreibende Logeintrag; darf nicht {@code null} sein
     */
    void write(@NotNull LogEntry entry);
}