package de.uzk.logger.output;

import de.uzk.logger.LogEntry;
import de.uzk.logger.LogLevel;
import de.uzk.utils.StringUtils;

import java.util.List;

/**
 * Konvertiert {@link LogEntry}-Daten in HTML-darstellbare Blöcke.
 *
 * <p>Alle generierten HTML-Fragmente werden in einem internen Buffer gesammelt und können
 * über {@link #exportHtml()} als kompletter HTML-String abgerufen werden.</p>
 */
public class HtmlOutput implements LogOutput {
    /**
     * Interner Buffer, der sukzessive mit formatierten HTML-Fragmenten befüllt wird.
     */
    private final StringBuilder buffer;

    /**
     * Erzeugt eine neue Instanz von {@code HtmlOutput} und initialisiert
     * den internen Ausgabepuffer.
     */
    public HtmlOutput() {
        this.buffer = new StringBuilder();
    }

    /**
     * Gibt den Logeintrag farbig, formatiert und strukturiert aus und fügt ihn im
     * HTML-Stil dem internen Buffer hinzu. Ignoriert die Debug-Protokollebene.
     *
     * @param entry Der zu schreibende Logeintrag
     */
    @Override
    public void write(LogEntry entry) {
        if (entry.getLevel() == LogLevel.DEBUG) return;

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