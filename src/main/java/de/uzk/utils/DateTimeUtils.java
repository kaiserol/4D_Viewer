package de.uzk.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility-Klasse zur einheitlichen Formatierung von Datum, Zeit und Dauer sowie zur einfachen Zeitmessung.
 *
 * <br><br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class DateTimeUtils {
    /**
     * Der Formatierer für einen vollständigen Zeitstempel inklusive Millisekunden.
     */
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Der Formatierer für ein Datum ohne Zeitanteil.
     */
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-dd-MM");

    /**
     * Reguläres Ausdrucksmuster zur Validierung eines Datums im Format {@code yyyy-MM-dd}.
     */
    public static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private DateTimeUtils() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Formatiert den aktuellen Zeitpunkt mithilfe des übergebenen Formatierers.
     *
     * @param formatter Der zu verwendende {@link DateTimeFormatter}
     * @return Eine formatierte Zeichenkette des aktuellen Datums bzw. Zeitstempels
     */
    public static String formatDateTime(DateTimeFormatter formatter) {
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Führt eine Aktion aus, misst dabei die Ausführungszeit und gibt das Ergebnis der Aktion zurück.
     * Die gemessene Zeit wird über den angegebenen {@link Consumer} als formatierter String ausgegeben.
     *
     * @param action       Die auszuführende Aktion, deren Laufzeit gemessen wird.
     * @param timeConsumer Ein Konsument, der den formatierten Zeit-String erhält.
     * @param <T>          Rückgabetyp der Aktion.
     * @return Das Ergebnis der ausgeführten Aktion.
     */
    public static <T> T measureTime(Supplier<T> action, Consumer<String> timeConsumer) {
        long start = System.nanoTime();
        T result = action.get();
        long duration = System.nanoTime() - start;
        timeConsumer.accept(formatNanosDuration(duration));
        return result;
    }

    /**
     * Formatiert eine Zeitdauer in Nanosekunden in einen gut lesbaren Zeit-String.
     * Das Ausgabeformat richtet sich automatisch nach der Länge der Dauer:
     *
     * <ul>
     *     <li>< 1 Sekunde → Millisekunden (z. B. {@code 87 ms})</li>
     *     <li>< 1 Minute → Sekunden.millisekunden (z. B. {@code 4.087 s})</li>
     *     <li>< 1 Stunde → mm:ss (z. B. {@code 02:15})</li>
     *     <li>≥ 1 Stunde → HH:mm:ss (z. B. {@code 01:02:45})</li>
     * </ul>
     *
     * @param nanos Die Dauer in Nanosekunden.
     * @return Ein formatierter Zeit-String entsprechend der gemessenen Dauer.
     */
    public static String formatNanosDuration(long nanos) {
        long totalMillis = nanos / 1_000_000;
        long millis = totalMillis % 1_000;

        long totalSeconds = totalMillis / 1_000;
        long seconds = totalSeconds % 60;

        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;

        long totalHours = totalMinutes / 60;

        if (totalSeconds == 0) {
            return millis + " ms";
        } else if (totalMinutes == 0) {
            return String.format("%d.%03d s", seconds, millis);
        } else if (totalHours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", totalHours, minutes, seconds);
        }
    }
}
