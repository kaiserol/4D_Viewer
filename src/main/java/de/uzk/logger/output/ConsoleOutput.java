package de.uzk.logger.output;

import de.uzk.logger.LogEntry;
import de.uzk.utils.CLIStyle;

import java.util.List;

/**
 * Schreibt {@link LogEntry}-Daten zeilenweise formartiert in die Konsole.
 */
public class ConsoleOutput implements LogOutput {

    /**
     * Gibt den Logeintrag farbig, formatiert und strukturiert aus.
     *
     * @param entry Der zu schreibende Logeintrag
     */
    @Override
    public void write(LogEntry entry) {
        List<String> formattedEntry = entry.formatEntry(true);
        String result =
            // Zeitstempel
            CLIStyle.text(formattedEntry.get(0)).bold().foreground(entry.getLevel().getColor()) +
                formattedEntry.get(1) +

                // Protokollebene
                CLIStyle.text(formattedEntry.get(2)).foreground(entry.getLevel().getColor()).toString() +
                formattedEntry.get(3) +

                // Quelle
                CLIStyle.text(formattedEntry.get(4)).underline().toString() +
                formattedEntry.get(5) +

                // Nachricht
                CLIStyle.text(formattedEntry.get(6));

        System.out.println(result);
    }
}
