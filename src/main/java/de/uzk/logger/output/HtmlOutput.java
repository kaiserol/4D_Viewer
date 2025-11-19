package de.uzk.logger.output;

import de.uzk.logger.LogEntry;
import de.uzk.logger.LogLevel;
import de.uzk.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
 * {@link #exportHtml()} als vollständige HTML-Ausgabe abgerufen werden.
 */
public class HtmlOutput implements LogOutput {
    /**
     * Interner Puffer für sukzessive generierte HTML-Fragmente.
     */
    private final StringBuilder buffer;

    /**
     * Erstellt eine neue {@code HtmlOutput}-Instanz und initialisiert
     * den internen Ausgabepuffer.
     */
    public HtmlOutput() {
        this.buffer = new StringBuilder();
    }

    /**
     * Formatiert den übergebenen {@link LogEntry} und fügt ihn dem internen Buffer hinzu.
     *
     * <p>
     * Einträge mit der Protokollebene {@link LogLevel#DEBUG} werden ignoriert und
     * nicht in das HTML-Protokoll aufgenommen.
     *
     * @param entry Der zu verarbeitende Logeintrag; darf nicht {@code null} sein
     */
    @Override
    public void write(@NotNull LogEntry entry) {
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
     * Gibt alle bisher hinzugefügten HTML-Fragmente als zusammenhängenden
     * HTML-String zurück.
     *
     * @return vollständige HTML-Ausgabe aller geschriebenen Logeinträge
     */
    public String exportHtml() {
        return this.buffer.toString();
    }
}