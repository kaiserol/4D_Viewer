package de.uzk.logger.output;

import de.uzk.logger.LogEntry;
import de.uzk.utils.StringUtils;

import java.util.List;

/**
 * Wandelt {@link LogEntry}-Daten in HTML-darstellbare Blöcke um.
 * Die Einträge werden im internen Buffer gesammelt und können mit {@link #exportHtml()} abgerufen werden.
 */
public class HtmlOutput implements LogOutput {
    /**
     * Interner Buffer, in den die bereits formatierten HTML-Blöcke angefügt werden.
     */
    private final StringBuilder buffer = new StringBuilder();

    /**
     * Gibt den Logeintrag farbig, formatiert und strukturiert aus und fügt ihn im
     * HTML-Stil dem internen Buffer hinzu.
     *
     * @param entry Der zu schreibende Logeintrag
     */
    @Override
    public void write(LogEntry entry) {
        List<String> formattedEntry = entry.formatEntry(false);
        String result =
            // Zeitstempel
            StringUtils.applyColor(StringUtils.wrapBold(formattedEntry.get(0)), entry.getLevel().getColor()) +
                formattedEntry.get(1) +

                // Protokollebene
                StringUtils.applyColor(formattedEntry.get(2), entry.getLevel().getColor()) +
                formattedEntry.get(3) +

                // Quelle
                StringUtils.wrapUnderlined(formattedEntry.get(4)) +
                formattedEntry.get(5) + StringUtils.NEXT_LINE +

                // Nachricht
                formattedEntry.get(6);

        this.buffer.append(StringUtils.wrapPre(result));
    }

    /**
     * Liefert den gesamten Buffer als String.
     *
     * @return HTML-String aller bisher geschriebenen Logeinträge
     */
    public String exportHtml() {
        return this.buffer.toString();
    }
}