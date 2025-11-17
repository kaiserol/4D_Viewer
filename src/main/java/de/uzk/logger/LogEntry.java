package de.uzk.logger;

import de.uzk.utils.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert einen einzelnen Logeintrag innerhalb des Loggers {@link de.uzk.logger.Logger}.
 *
 * <p>
 * Ein {@code LogEntry} besteht aus:
 * <ul>
 *   <li>einem Zeitstempel</li>
 *   <li>einer Protokollebene</li>
 *   <li>einer Quelle, aus der der Log-Aufruf stammt</li>
 *   <li>einer Nachricht.</li>
 * </ul>
 * <p>
 */
public class LogEntry {

    /**
     * Die Protokollebene des Log-Eintrags.
     */
    private final LogLevel level;

    /**
     * Die Nachricht des Log-Eintrags.
     */
    private final String message;

    /**
     * Der Zeitstempel des Log-Eintrags.
     */
    private final String timestamp;

    /**
     * Die Quelle des Log-Eintrags, aus der der Log-Aufruf stammt.
     */
    private final String source;

    /**
     * Der Formatierer für den Zeitstempel (inklusive Millisekunden).
     */
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Erstellt einen neuen LogEntry.
     *
     * @param level Die Protokollebene (darf nicht null sein)
     * @param msg   Die zu loggende Nachricht (darf null sein)
     * @throws NullPointerException Wenn {@code level} null ist
     */
    public LogEntry(LogLevel level, String msg) {
        if (level == null) throw new NullPointerException("level is null");
        this.level = level;
        this.message = msg == null ? "" : msg;
        this.timestamp = initTimeStamp();
        this.source = initSource();
    }

    /**
     * @return Die Protokollebene des Log-Eintrags
     */
    public LogLevel getLevel() {
        return this.level;
    }

    /**
     * @return Die Nachricht des Log-Eintrags
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * @return Der Zeitstempel des Log-Eintrags. Zur Bestimmung des Zeitstempels wird die Methode
     * {@link #initTimeStamp()} verwendet.
     */
    public String getTimeStamp() {
        return this.timestamp;
    }

    /**
     * @return Die Quelle des Log-Eintrags, aus der der Log-Aufruf stammt. Zur Bestimmung der Quelle
     * wird die Methode {@link #initSource()} verwendet.
     */
    public String getSource() {
        return this.source;
    }


    // ========================================
    // Initialisierungen
    // ========================================

    /**
     * Erzeugt den aktuellen Zeitstempel im Format {@code yyyy-MM-dd HH:mm:ss.SSS}.
     *
     * @return Formatierter Zeitstempel
     */
    private String initTimeStamp() {
        return LocalDateTime.now().format(DATE_TIME_FORMAT);
    }

    /**
     * Analysiert den aktuellen StackTrace, um den ursprünglichen Aufrufer
     * einer logger-Methode zu identifizieren.
     *
     * <p>
     * StackTrace-Struktur (vereinfacht):
     *
     * <pre>
     * [0] Aufruf vom aktuellen Thread {@link Thread#getStackTrace()}
     * [1] Aufruf der vorliegenden Methode (initSource)
     * [2] Aufruf des Konstruktors {@link LogEntry#LogEntry(LogLevel, String)}
     * [3] Aufruf von {@link Logger#write(LogLevel, String)}
     * [4] Aufruf einer logger-Methode (debug, info, warn, error)
     * [5] Ursprünglicher Aufrufer einer logger-Methode
     * </pre>
     *
     * @return Abgekürzter Klassenname + Methodenname des Log-Aufrufers,
     * oder "unknown", falls nicht bestimmbar
     */
    private String initSource() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 6) {
            StackTraceElement caller = stackTrace[5];
            String abbrClassName = abbreviateClassName(caller.getClassName());
            String methodName = extractMethodName(caller.getMethodName());
            return abbrClassName + "." + methodName + "()";
        }
        return "unknown";
    }

    // ========================================
    // Hilfsmethoden
    // ========================================

    /**
     * Kürzt einen vollständigen Klassennamen, indem die Paketanteile
     * auf ihren Anfangsbuchstaben reduziert werden.
     *
     * <p>
     * Beispiel:
     *
     * <pre>
     * de.uzk.module.MyClass
     * → d.u.m.MyClass
     * </pre>
     *
     * @param fullClassName Vollständiger Klassenname
     * @return Abgekürzte Form
     */
    private String abbreviateClassName(String fullClassName) {
        String[] parts = fullClassName.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            sb.append(parts[i].charAt(0)).append(".");
        }
        sb.append(parts[parts.length - 1]); // Klassenname
        return sb.toString();
    }

    /**
     * Extrahiert einen lesbaren Methodennamen.
     * Lambda-Ausdrücke (z. B. {@code lambda$myMethod$0})
     * werden zu ihrem realen Namen zurückgeführt.
     *
     * @param methodName Originaler Methodenname aus dem StackTrace
     * @return Bereinigter Methodenname
     */
    private String extractMethodName(String methodName) {
        if (methodName.contains("lambda$")) {
            return methodName.substring(methodName.indexOf("$") + 1, methodName.lastIndexOf("$"));
        }
        return methodName;
    }

    /**
     * Formatiert den aktuellen {@link LogEntry} in seine strukturellen Bestandteile.
     *
     * <p>
     * Die Methode erzeugt noch keine farbliche Darstellung und dient ausschließlich
     * zur konsistenten Vorformatierung.
     *
     * <p>
     * <b>Rückgabeformat (Liste mit streng definierten Elementen):</b>
     *
     * <pre>
     * index 0 → "[" + timestamp + "]"
     * index 1 → " "
     * index 2 → level (rechtsbündig gepolstert)
     * index 3 → " "
     * index 4 → "#" + source
     * index 5 → ": "
     * index 6 → message (optional mit Einrückung)
     * </pre>
     *
     * <p>
     * <b>Einrückung:</b>
     * <p>
     * Wenn {@code indentedMessage = true}, werden Folgezeilen der Nachricht unterhalb
     * der eigentlichen Nachrichtenposition eingerückt.</p>
     *
     * @param indentedMessage {@code true}, wenn mehrzeilige Nachrichten eingerückt werden sollen;
     *                        {@code false}, wenn die Nachricht unverändert ausgegeben wird
     * @return Liste aus sieben Elementen, welche die komplette Log-Ausgabe strukturiert darstellen
     */
    public List<String> formatEntry(boolean indentedMessage) {
        // Komponenten vorbereiten
        String timestampStr = "[" + getTimeStamp() + "]";
        String levelRaw = getLevel().toString();
        String levelStr = levelRaw + " ".repeat(LogLevel.maxLevelLength() - levelRaw.length());
        String sourceStr = "#" + getSource();
        String messageStr = indentedMessage ? getIndentedMessage(timestampStr, levelStr, sourceStr, getMessage()) : getMessage();

        // Rückgabe als Liste
        List<String> list = new ArrayList<>();
        list.add(timestampStr);
        list.add(" ");
        list.add(levelStr);
        list.add(" ");
        list.add(sourceStr);
        list.add(": ");
        list.add(messageStr);
        return list;
    }

    /**
     * Erzeugt für mehrzeilige Nachrichten die korrekte Einrückung, sodass alle
     * Folgezeilen exakt unterhalb der Nachrichtenposition beginnen.
     *
     * @param timestampStr Der Zeitstempel des Log-Eintrags
     * @param levelStr     Die Protokollebene des Log-Eintrags
     * @param sourceStr    Die Quelle des Log-Eintrags
     * @param messageStr   Die Nachricht des Log-Eintrags
     * @return Die Nachricht mit korrekt eingerückten Folgezeilen
     */
    private String getIndentedMessage(String timestampStr, String levelStr, String sourceStr, String messageStr) {
        String indent = StringUtils.NEXT_LINE +
            " ".repeat(timestampStr.length() + 1) +
            " ".repeat(levelStr.length() + 1) +
            " ".repeat(sourceStr.length() + 2);
        return messageStr.replace(StringUtils.NEXT_LINE, indent);
    }
}