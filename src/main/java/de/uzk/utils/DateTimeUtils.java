package de.uzk.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.uzk.Main.settings;

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
     * Der Formatierer für einen vollständigen Zeitstempel inklusive Millisekunden.<br>
     * Typisches Einsatzgebiet: Log-Ausgaben.
     * <p>
     * Format: {@code yyyy-MM-dd HH:mm:ss.SSS}<br>
     * Beispiel: {@code 2025-11-19 10:00:00.000}
     */
    private static final DateTimeFormatter LOGGING_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Der Formatierer für ein Datum ohne Zeitanteil.
     * <p>
     * Format: {@code yyyy-MM-dd}<br>
     * Beispiel: {@code 2025-11-19}
     */
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Der Formatierer für ein langes deutsches Datumsformat.
     * <p>
     * Format: {@code dd. MMMM yyyy}<br>
     * Beispiel: {@code 19. November 2025}
     */
    private static final DateTimeFormatter DATE_LONG_DE_FORMATTER = DateTimeFormatter.ofPattern("dd. MMMM yyyy");

    /**
     * Der Formatierer für ein langes englisches Datumsformat.
     * <p>
     * Format: {@code MMMM dd, yyyy}<br>
     * Beispiel: {@code November 19, 2025}
     */
    private static final DateTimeFormatter DATE_LONG_EN_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    /**
     * Regulärer Ausdruck zur Validierung von Datumsangaben.
     * <p>
     * Format: {@code yyyy-MM-dd} (ISO-8601-Format)<br>
     * Beispiel: {@code 2025-11-19}
     */
    public static final String DATE_ONLY_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private DateTimeUtils() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Formatiert ein Datum anhand der aktuellen Spracheinstellung.
     * Erwartet wird ein Eingangsdatum im Format {@code yyyy-MM-dd}.
     * <p>
     * Bei deutscher Sprache wird {@link #DATE_LONG_DE_FORMATTER},
     * bei englischer Sprache {@link #DATE_LONG_EN_FORMATTER} verwendet.
     * <p>
     * Falls das Eingangsformat nicht geparst werden kann,
     * wird der ursprüngliche String unverändert zurückgegeben.
     *
     * @param dateStr Datum als String im Format {@code yyyy-MM-dd}
     * @return Formatiertes Datum in Langform oder unveränderter Eingabestring
     */
    public static String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_ONLY_FORMATTER);
            DateTimeFormatter formatter = settings.getLanguage().isGerman() ? DATE_LONG_DE_FORMATTER : DATE_LONG_EN_FORMATTER;
            return date.format(formatter);
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * Liefert das heutige Datum und die aktuelle Uhrzeit im Log-Format {@code yyyy-MM-dd HH:mm:ss.SSS}.
     *
     * @return Heutiges Datum und Uhrzeit
     */
    public static String getFormattedLoggerDateTimeNow() {
        return dateTimeToday().format(LOGGING_DATE_TIME_FORMATTER);
    }

    /**
     * Liefert das heutige Datum im Format {@code yyyy-MM-dd}.
     *
     * @return Heutiges Datum
     */
    public static String getFormattedDateToday() {
        return dateToday().format(DATE_ONLY_FORMATTER);
    }

    /**
     * Liefert das heutige Datum und die aktuelle Uhrzeit als {@link LocalDateTime}.
     *
     * @return Heutiges Datum und Uhrzeit
     */
    public static LocalDateTime dateTimeToday() {
        return LocalDateTime.now();
    }

    /**
     * Liefert das heutige Datum als {@link LocalDate}.
     *
     * @return Heutiges Datum
     */
    public static LocalDate dateToday() {
        return LocalDate.now();
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
