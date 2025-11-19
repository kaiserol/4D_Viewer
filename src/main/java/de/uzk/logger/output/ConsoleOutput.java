package de.uzk.logger.output;

import de.uzk.logger.LogEntry;
import de.uzk.utils.CLIStyle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Implementierung eines {@link LogOutput}, die {@link LogEntry}-Objekte formatiert und
 * farblich hervorgehoben in der Konsole ausgibt.
 *
 * <p>
 * Die Darstellung erfolgt mittels {@link CLIStyle}, wodurch Farben,
 * Hervorhebungen und Strukturierungen konsistent über ANSI-Stile umgesetzt werden.
 */
public class ConsoleOutput implements LogOutput {

    /**
     * Formatiert den übergebenen {@link LogEntry} und gibt ihn direkt
     * in der Konsole aus.
     *
     * @param entry Der auszugebende Logeintrag; darf nicht {@code null} sein
     */
    @Override
    public void write(@NotNull LogEntry entry) {
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
