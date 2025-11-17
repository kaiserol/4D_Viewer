package de.uzk.logger;

import de.uzk.logger.output.LogOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert die zentrale Komponente des Logging-Systems.
 * <p>
 * Ein {@code Logger} verwaltet eine Liste von {@link LogOutput}-Instanzen.
 * Jede dieser Ausgaben repräsentiert ein konkretes Ziel, z.&nbsp;B.:
 *
 * <ul>
 *     <li>Dateiausgabe</li>
 *     <li>Konsolenausgabe</li>
 *     <li>HTML-generierte Log-Ausgabe</li>
 * </ul>
 *
 * <p>
 * Bei jedem Log-Aufruf wird ein {@link LogEntry} erzeugt und automatisch
 * an alle registrierten Ausgabekanäle weitergereicht.
 */
public class Logger {
    /**
     * Liste aller registrierten Ausgabekanäle.
     */
    private final List<LogOutput> outputs;

    /**
     * Erstellt einen neuen Logger ohne voreingestellte Ausgabekanäle.
     * Mithilfe der Methode {@link #addOutput(LogOutput)} können LogOutput-Instanzen hinzugefügt werden.
     */
    public Logger() {
        this.outputs = new ArrayList<>();
    }

    /**
     * Registriert einen neuen Ausgabekanal.
     *
     * @param output Konkrete {@link LogOutput}-Implementierung
     * @throws NullPointerException Falls {@code output} null ist
     */
    public void addOutput(LogOutput output) {
        if (output == null) throw new NullPointerException("Output is null.");
        this.outputs.add(output);
    }

    /**
     * Interne Methode zum Schreiben eines Logs an alle Ausgabekanäle.
     * Erzeugt ein {@link LogEntry}-Objekt und übergibt es an jede
     * registrierte {@link LogOutput}-Instanz.
     *
     * @param level Die Protokollebene (darf nicht null sein)
     * @param msg   Die zu loggende Nachricht (darf null sein)
     */
    void write(LogLevel level, String msg) {
        LogEntry entry = new LogEntry(level, msg);
        for (LogOutput out : outputs) {
            out.write(entry);
        }
    }

    /**
     * Schreibt eine Nachricht auf {@link LogLevel#DEBUG}.
     *
     * @param msg Nachricht
     */
    public void debug(String msg) {
        write(LogLevel.DEBUG, msg);
    }

    /**
     * Schreibt eine Nachricht auf {@link LogLevel#INFO}.
     *
     * @param msg Nachricht
     */
    public void info(String msg) {
        write(LogLevel.INFO, msg);
    }

    /**
     * Schreibt eine Nachricht auf {@link LogLevel#WARN}.
     *
     * @param msg Nachricht
     */
    public void warn(String msg) {
        write(LogLevel.WARN, msg);
    }

    /**
     * Schreibt eine Nachricht auf {@link LogLevel#ERROR}.
     *
     * @param msg Nachricht
     */
    public void error(String msg) {
        write(LogLevel.ERROR, msg);
    }
}