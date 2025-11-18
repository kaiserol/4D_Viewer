package de.uzk.logger;

import de.uzk.logger.output.ConsoleOutput;
import de.uzk.logger.output.FileOutput;
import de.uzk.logger.output.HtmlOutput;
import de.uzk.logger.output.LogOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert die Logger-Klasse für das Erfassen und Ausgeben von Logeinträgen über verschiedene Kanäle.
 *
 * <p>
 * Standardmäßig werden Logs auf der Konsole, in einer HTML-Ausgabe sowie in einer Datei
 * gespeichert. Weitere Ausgabekanäle können über {@link #addOutput(LogOutput)} hinzugefügt werden.
 */
public class Logger {
    /**
     * Liste aller registrierten Ausgabekanäle.
     */
    private final List<LogOutput> outputs;

    /**
     * HTML-Ausgabekanal für Logeinträge.
     */
    private final HtmlOutput htmlOutput;

    /**
     * Dateibasierter Ausgabekanal für Logeinträge.
     */
    private final FileOutput fileOutput;

    /**
     * Erstellt einen neuen Logger mit voreingestellten Ausgabekanälen.
     * <p>
     * Standardmäßig werden folgende Ausgaben aktiviert:
     * <ul>
     *     <li>Konsole ({@link ConsoleOutput})</li>
     *     <li>HTML-Ausgabe ({@link HtmlOutput})</li>
     *     <li>Dateiausgabe ({@link FileOutput})</li>
     * </ul>
     *
     * <p>
     * Weitere Ausgabekanäle können über {@link #addOutput(LogOutput)} hinzugefügt werden.
     */
    public Logger() {
        this.outputs = new ArrayList<>();
        addOutput(new ConsoleOutput());
        addOutput(this.htmlOutput = new HtmlOutput());
        addOutput(this.fileOutput = new FileOutput());
    }

    /**
     * Exportiert alle bisher erfassten Logeinträge im HTML-Format.
     *
     * @return Die HTML-Darstellung aller Logeinträge
     */
    public String exportHtml() {
        return this.htmlOutput.exportHtml();
    }

    /**
     * Exportiert alle bisher erfassten Logeinträge in eine Datei.
     */
    public void exportToFile() {
        this.fileOutput.exportToFile();
    }

    /**
     * Registriert einen neuen Ausgabekanal.
     *
     * @param output Konkrete {@link LogOutput}-Implementierung
     * @throws NullPointerException Falls {@code output} null ist
     */
    private void addOutput(LogOutput output) {
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