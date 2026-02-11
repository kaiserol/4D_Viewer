package de.uzk.logger;

import de.uzk.logger.output.ConsoleOutput;
import de.uzk.logger.output.FileOutput;
import de.uzk.logger.output.HtmlOutput;
import de.uzk.logger.output.LogOutput;
import de.uzk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert die Logger-Klasse für das Erfassen und Ausgeben von Logeinträgen über verschiedene Kanäle.
 *
 * <p>
 * Standardmäßig werden Logs auf der Konsole, in einer Datei sowie im HTML-Format
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
     * Erstellt einen neuen Logger mit voreingestellten Ausgabekanälen.
     * <p>
     * Standardmäßig werden folgende Ausgaben aktiviert:
     * <ul>
     *     <li>Konsolenausgabe ({@link ConsoleOutput})</li>
     *     <li>Dateiausgabe ({@link FileOutput})</li>
     *     <li>HTML-Ausgabe ({@link HtmlOutput})</li>
     * </ul>
     *
     * <p>
     * Weitere Ausgabekanäle können über {@link #addOutput(LogOutput)} hinzugefügt werden.
     */
    public Logger() {
        outputs = new ArrayList<>();

        // Konsolenausgabe
        LogOutput consoleOutput = new ConsoleOutput();
        addOutput(consoleOutput);

        // Dateiausgabe
        LogOutput fileOutput = new FileOutput();
        fileOutput.blockLevel(LogLevel.DEBUG);
        addOutput(fileOutput);

        // HTML-Ausgabe
        htmlOutput = new HtmlOutput();
        addOutput(htmlOutput);
    }

    /**
     * Exportiert alle bisher erfassten Logeinträge im HTML-Format.
     * <p>
     * Übergebene {@link LogLevel}-Werte dienen als zusätzliche Filter und
     * bewirken, dass entsprechende Einträge nicht in der exportierten
     * HTML-Ausgabe erscheinen.
     *
     * @param blockedLevels Optionale Liste von Protokollebenen, die aus der Ausgabe
     *                      ausgeschlossen werden sollen
     */
    public String exportHtml(LogLevel... blockedLevels) {
        return htmlOutput.exportHtml(blockedLevels);
    }

    /**
     * Registriert einen neuen Ausgabekanal.
     *
     * @param output Konkrete {@link LogOutput}-Implementierung
     * @throws NullPointerException Falls {@code output} null ist
     */
    public void addOutput(LogOutput output) {
        if (output == null) throw new NullPointerException("Output is null.");
        outputs.add(output);
    }

    /**
     * Interne Methode zum Schreiben eines Logs an alle Ausgabekanäle.
     * Erzeugt ein {@link LogEntry}-Objekt und übergibt es an jede
     * registrierte {@link LogOutput}-Instanz.
     *
     * @param level Die Protokollebene (darf nicht null sein)
     * @param msg   Die Nachricht, die geloggt werden soll
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
     * @param msg Die Nachricht, die geloggt werden soll
     */
    public void debug(String msg) {
        write(LogLevel.DEBUG, msg);
    }

    /**
     * Schreibt eine Nachricht auf {@link LogLevel#INFO}.
     *
     * @param msg Die Nachricht, die geloggt werden soll
     */
    public void info(String msg) {
        write(LogLevel.INFO, msg);
    }

    /**
     * Schreibt eine Nachricht auf {@link LogLevel#WARN}.
     *
     * @param msg Die Nachricht, die geloggt werden soll
     */
    public void warn(String msg) {
        write(LogLevel.WARN, msg);
    }

    /**
     * Schreibt eine Nachricht auf {@link LogLevel#ERROR}.
     *
     * @param msg Die Nachricht, die geloggt werden soll
     */
    public void error(String msg) {
        write(LogLevel.ERROR, msg);
    }

    /**
     * Schreibt eine Ausnahme auf {@link LogLevel#EXCEPTION}.
     *
     * @param e   Die Ausnahme, die geloggt werden soll
     * @param msg Die Nachricht, die vor der Ausnahme geloggt werden soll
     */
    public void exception(Exception e, String msg) {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("An exception '%s' was thrown: %s".formatted(e.getClass().getCanonicalName(), e.getMessage()))
            .append(" (%s)".formatted(msg))
            .append(StringUtils.NEXT_LINE);

        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            msgBuilder.append("\tat ").append(stackTrace[i]);
            if (i < stackTrace.length - 1) msgBuilder.append(StringUtils.NEXT_LINE);
        }
        write(LogLevel.EXCEPTION, msgBuilder.toString());
    }
}