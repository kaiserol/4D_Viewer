package de.uzk.logger;

import de.uzk.utils.DateTimeUtils;
import de.uzk.utils.StringUtils;

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
     * Erstellt einen neuen LogEntry.
     *
     * @param level Die Protokollebene (darf nicht null sein)
     * @param msg   Die zu loggende Nachricht (darf null sein)
     * @throws NullPointerException Wenn {@code level} null ist
     */
    public LogEntry(LogLevel level, String msg) {
        if (level == null) throw new NullPointerException("Level is null");
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
     * Erzeugt den aktuellen Zeitstempel.
     *
     * @return Formatierter Zeitstempel
     */
    private String initTimeStamp() {
        return DateTimeUtils.getFormattedLoggerDateTimeNow();
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
            return "#" + abbrClassName + "." + methodName + "()";
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
     * index 0 → "[" + Zeitstempel + "]"
     * index 1 → &lt;Trenner&gt;
     * index 2 → Protokollebene (rechtsbündig gepolstert)
     * index 3 → &lt;Trenner&gt;
     * index 4 → Quelle (z. B. Klassenname)
     * index 5 → &lt;Trenner&gt;
     * index 6 → Nachricht (optional mit Einrückung)
     * </pre>
     *
     * <p>
     * <b>Einrückung:</b>
     *
     * <p>
     * Wenn {@code indentedMessage = true}, werden Folgezeilen der Nachricht unterhalb
     * der eigentlichen Nachrichtenposition eingerückt.
     *
     * @param indentedMessage {@code true}, wenn mehrzeilige Nachrichten eingerückt werden sollen;
     *                        {@code false}, wenn die Nachricht unverändert ausgegeben wird
     * @return Eine Liste mit genau sieben strukturierten Einträgen zur weiteren Ausgabe
     */
    public List<String> formatEntry(boolean indentedMessage) {
        List<String> list = new ArrayList<>();

        // Zeitstempel hinzufügen
        String timestampStr = "[" + getTimeStamp() + "]";
        list.add(timestampStr);   // 0
        list.add("\t");           // 1

        // Protokollebene hinzufügen
        String levelStr = getLevel().toString();
        list.add(levelStr);       // 2
        list.add(" - ");          // 3

        // Quelle hinzufügen
        String sourceStr = getSource();
        list.add(sourceStr);      // 4
        list.add(" - ");          // 5

        // Nachricht hinzufügen
        String messageStr = indentedMessage ? getIndentedMessage(list, getMessage()) : getMessage();
        list.add(messageStr);     // 6

        // Rückgabe als Liste
        return list;
    }

    /**
     * Erzeugt für mehrzeilige Nachrichten einen Einrückungstext, der exakt
     * an der Startposition der Nachricht ausgerichtet ist.
     *
     * <p>
     * Die Einrückung basiert ausschließlich auf den bereits formatierten
     * Strukturelementen aus {@link #formatEntry(boolean)} und ist daher dynamisch
     * an deren Länge gekoppelt.
     *
     * @param formattedEntry Die bereits erzeugte Basisstruktur (Elemente 0–5)
     * @param messageStr     Die ursprüngliche Nachricht
     * @return Die Nachricht mit korrekt eingerückten Folgezeilen
     */
    private String getIndentedMessage(List<String> formattedEntry, String messageStr) {
        String indent = StringUtils.NEXT_LINE +
            // Zeitstempel
            " ".repeat(formattedEntry.get(0).length()) +
            formattedEntry.get(1) +

            // Protokollebene
            " ".repeat(formattedEntry.get(2).length()) +
            formattedEntry.get(3) +

            // Quelle
            " ".repeat(formattedEntry.get(4).length()) +
            formattedEntry.get(5);

        String cleanIndent = indent.replaceAll("\\S", " ");
        return messageStr.replace(StringUtils.NEXT_LINE, cleanIndent);
    }
}