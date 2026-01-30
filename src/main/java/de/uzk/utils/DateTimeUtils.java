package de.uzk.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.uzk.Main.settings;
import static de.uzk.Main.workspace;

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
     * Beispiel: {@code 2025-01-01 00:00:00.000}
     */
    public static final DateTimeFormatter LOGGING_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Der Formatierer für ein Datum ohne Zeitanteil.
     * <p>
     * Format: {@code yyyy-MM-dd}<br>
     * Beispiel: {@code 2025-01-01}
     */
    public static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Der Formatierer für ein langes deutsches Datumsformat.
     * <p>
     * Format: {@code dd. MMMM yyyy}<br>
     * Beispiel: {@code 01. Januar 2025}
     */
    public static final DateTimeFormatter DATE_LONG_DE_FORMATTER = DateTimeFormatter.ofPattern("dd. MMMM yyyy");

    /**
     * Der Formatierer für ein langes englisches Datumsformat.
     * <p>
     * Format: {@code MMMM dd, yyyy}<br>
     * Beispiel: {@code January 01, 2025}
     */
    public static final DateTimeFormatter DATE_LONG_EN_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    /**
     * Regulärer Ausdruck zur Validierung von Datumsangaben.
     * <p>
     * Format: {@code yyyy-MM-dd} (ISO-8601-Format)<br>
     * Beispiel: {@code 2025-01-01}
     */
    public static final String DATE_ONLY_PATTERN = "\\d{4}-\\d{1,2}-\\d{1,2}";

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private DateTimeUtils() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Formatiert ein Datum anhand der aktuellen Spracheinstellung der Anwendung.
     * <p>
     * Erwartet wird ein Datum im Format {@code yyyy-MM-dd}. Das Datum wird
     * zunächst mittels {@link #parseDate(String)} geparst und anschließend in ein
     * sprachabhängiges Langformat umgewandelt:
     * <ul>
     *     <li>Deutsch: {@link #DATE_LONG_DE_FORMATTER}</li>
     *     <li>Englisch: {@link #DATE_LONG_EN_FORMATTER}</li>
     * </ul>
     * Kann das Eingabedatum nicht geparst werden, wird der ursprüngliche String
     * unverändert zurückgegeben.
     *
     * @param dateStr Datum im Format {@code yyyy-MM-dd}
     * @return Sprachabhängig formatiertes Datum oder unveränderter Eingabestring
     */
    public static String parseAndReformatDate(String dateStr) {
        LocalDate date = parseDate(dateStr);

        if (date == null) return dateStr;
        DateTimeFormatter formatter = settings.getLanguage().isGerman() ? DATE_LONG_DE_FORMATTER : DATE_LONG_EN_FORMATTER;
        return date.format(formatter);
    }

    /**
     * Parst ein Datum im Format {@code yyyy-MM-dd} zu einem {@link LocalDate}.
     * <p>
     * Ein- oder zweistellige Monate und Tage werden automatisch auf zwei Ziffern normalisiert.
     * Schlägt das Parsen fehl (ungültiges Format oder Inhalt), wird {@code null} zurückgegeben.
     *
     * @param dateStr Ein Text, der ein Datum enthält (z. B. {@code 2025-1-1} oder {@code 2025-01-01})
     * @return Geparstes {@link LocalDate} oder {@code null}, wenn das Datum ungültig ist oder nicht gefunden wurde
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;

        // Datum extrahieren (Abbrechen, wenn Muster nicht gefunden wurde)
        Pattern datePattern = Pattern.compile(DATE_ONLY_PATTERN);
        Matcher matcher = datePattern.matcher(dateStr);
        if (!matcher.find()) return null;

        String matchedDate = matcher.group();

        // Ein- oder zweistellige Werte normalisieren
        try {
            String[] parts = matchedDate.split("-");
            String normalized = "%04d-%02d-%02d".formatted(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
            );
            return LocalDate.parse(normalized, DATE_ONLY_FORMATTER);
        } catch (Exception e) {
            return null;
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
            return "%d.%03d s".formatted(seconds, millis);
        } else if (totalHours == 0) {
            return "%02d:%02d".formatted(minutes, seconds);
        } else {
            return "%02d:%02d:%02d".formatted(totalHours, minutes, seconds);
        }
    }

    /**
     * Erzeugt einen Zeitstempel im Format HH:MM:SS basierend auf einem Frame-Index.
     *
     * <p>
     * Der übergebene Frame-Wert {@code frame} wird mithilfe der in der
     * Workspace-Konfiguration definierten Zeitauflösung in Sekunden umgerechnet.
     * Die konfigurierte Zeiteinheit bestimmt dabei, wie viele Sekunden ein einzelner
     * Frame repräsentiert ({@code workspace.getConfig().getTimeUnit()}).
     *
     * <p>Aus den berechneten Gesamtsekunden werden anschließend Stunden, Minuten
     * und Sekunden extrahiert und als formatierter String im Muster
     * {@code "HH:MM:SS.S"} zurückgegeben. Dabei werden führende Nullen automatisch
     * ergänzt.</p>
     *
     * @param frame Der Frame-Index, der zeitlich interpretiert werden soll
     * @return Ein formatierter Zeitstempel im Muster {@code "HH:MM:SS.S"}
     */
    public static String formatFrameTimeStamp(int frame) {
        double totalSeconds =  frame * workspace.getConfig().getTimeUnit();
        int fullSeconds = (int)totalSeconds;
        double seconds = totalSeconds % 60;
        int minute = fullSeconds / 60 % 60;
        int hour = fullSeconds / 60 / 60;

        return "%02d:%02d:%02.01f".formatted(hour, minute, seconds);
    }
}
