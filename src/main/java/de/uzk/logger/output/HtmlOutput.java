package de.uzk.logger.output;

import de.uzk.logger.LogEntry;
import de.uzk.logger.LogLevel;
import de.uzk.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementierung eines {@link LogOutput}, die {@link LogEntry}-Objekte in HTML-formatierte und
 * farblich hervorgehobene Ausgabeblöcke umwandelt.
 *
 * <p>
 * Die Darstellung erfolgt mittels {@link StringUtils}, wodurch Farben,
 * Hervorhebungen und Strukturierungen konsistent über HTML-Tags umgesetzt werden.
 *
 * <p>
 * Alle erzeugten HTML-Fragmente werden in einem internen Buffer gesammelt und können über
 * {@link #exportHtml(LogLevel...)} als vollständige HTML-Ausgabe abgerufen werden.
 */
public class HtmlOutput extends LogOutput {
    /**
     * Speichert formatiertes HTML als Schlüssel und die zugehörige Protokollebene als Wert.
     * <p>
     * Die LinkedHashMap garantiert: Einträge bleiben in Einfüge-Reihenfolge.
     */
    private final Map<String, LogLevel> entries;

    /**
     * Erstellt eine neue {@code HtmlOutput}-Instanz und Buffer
     * den internen Ausgabepuffer.
     */
    public HtmlOutput() {
        entries = new LinkedHashMap<>();
    }

    /**
     * Formatiert den übergebenen {@link LogEntry} und fügt ihn dem internen Buffer hinzu.
     *
     * @param entry Der zu verarbeitende Logeintrag; darf nicht {@code null} sein
     */
    @Override
    public void writeInternal(LogEntry entry) {
        List<String> formattedEntry = entry.formatEntry(false);
        String result =
            // Zeitstempel
            StringUtils.applyColor(StringUtils.wrapBold(formattedEntry.get(0)), entry.getLevel().getColor()) +
                " " +

                // Protokollebene
                StringUtils.applyColor(formattedEntry.get(2), entry.getLevel().getColor()) +
                " - " +

                // Quelle
                StringUtils.wrapUnderlined(formattedEntry.get(4)) +
                ":" + StringUtils.NEXT_LINE +

                // Nachricht
                formattedEntry.get(6);

        entries.put(StringUtils.wrapPre(result), entry.getLevel());
    }

    /**
     * Exportiert alle gespeicherten HTML-Blöcke als einen zusammenhängenden HTML-String.
     * Optional können einzelne LogLevel ausgefiltert werden.
     *
     * @param blockedLevels Optionale Liste zusätzlicher zu blockierender Ebenen
     * @return Zusammengesetzte HTML-Ausgabe
     */
    public String exportHtml(LogLevel... blockedLevels) {
        List<String> output = new ArrayList<>();

        for (Map.Entry<String, LogLevel> e : entries.entrySet()) {
            boolean skip = false;
            if (blockedLevels != null) {
                for (LogLevel level : blockedLevels) {
                    if (level == e.getValue()) {
                        skip = true;
                        break;
                    }
                }
            }
            if (!skip) {
                output.add(e.getKey());
            }
        }
        return String.join("", output);
    }
}