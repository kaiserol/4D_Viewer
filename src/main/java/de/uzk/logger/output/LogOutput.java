package de.uzk.logger.output;


import de.uzk.logger.LogEntry;
import de.uzk.logger.LogLevel;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Abstrakte Basisklasse für alle Log-Ausgabeziele.
 *
 * <p>
 * Diese Klasse stellt eine gemeinsame Filterlogik bereit, über die bestimmte
 * {@link LogLevel}-Ebenen blockiert werden können. Jeder Output-Typ implementiert
 * lediglich die eigentliche Schreiblogik über {@link #writeInternal(LogEntry)}.
 */
public abstract class LogOutput {
    /**
     * Menge aller blockierten Protokollebenen.
     */
    private final Set<LogLevel> blockedLevels;

    /**
     * Erzeugt einen neuen LogOutput ohne blockierte Protokollebenen.
     */
    public LogOutput() {
        this.blockedLevels = new TreeSet<>(Comparator.comparing(LogLevel::getText));
    }

    /**
     * Fügt eine Protokollebene zur Blockliste hinzu.
     *
     * @param level Zu blockierende Protokollebene
     */
    public void blockLevel(LogLevel level) {
        this.blockedLevels.add(level);
    }

    /**
     * Prüft, ob eine Protokollebene blockiert ist.
     *
     * @param level Zu prüfende Protokollebene
     * @return {@code true}, wenn die Protokollebene blockiert ist
     */
    public boolean isBlocked(LogLevel level) {
        return this.blockedLevels.contains(level);
    }

    /**
     * Hauptschreibmethode. Sie prüft zunächst, ob der Level blockiert ist,
     * und reicht den Eintrag ansonsten an {@link #writeInternal(LogEntry)} weiter.
     *
     * @param entry Der zu schreibende Logeintrag; darf nicht {@code null} sein
     */
    public final void write(LogEntry entry) {
        if (entry == null) throw new NullPointerException("Entry is null.");
        if (!isBlocked(entry.getLevel())) {
            writeInternal(entry);
        }
    }

    /**
     * Implementierungsspezifische Ausgabelogik.
     *
     * @param entry Der zu schreibende Logeintrag; darf nicht null sein
     */
    protected abstract void writeInternal(LogEntry entry);
}