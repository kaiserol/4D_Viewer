package de.uzk.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility-Klasse für numerische Operationen und Berechnungen.
 *
 * <br><br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class NumberUtils {
    /**
     * Toleranzwert für Rundungsfehler
     */
    private static final double EPSILON = 1e-10;

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private NumberUtils() {
        // Verhindert die Instanziierung dieser Hilfsklasse
    }

    /**
     * Prüft, ob ein Wert innerhalb eines bestimmten Bereichs liegt.
     * <p>
     * Aufgrund von möglichen Gleitkomma-Rundungsfehlern wird ein kleiner Toleranzwert (EPSILON)
     * berücksichtigt, sodass Werte knapp außerhalb der Grenzen dennoch als gültig gelten.
     *
     * @param value    Zu prüfender Wert
     * @param minValue Untere Grenze (einschließlich)
     * @param maxValue Obere Grenze (einschließlich)
     * @return True, wenn der Wert im Bereich liegt (mit EPSILON-Toleranz)
     */
    public static boolean valueInRange(double value, double minValue, double maxValue) {
        return minValue - EPSILON <= value && value <= maxValue + EPSILON;
    }

    /**
     * Prüft, ob ein Wert innerhalb eines bestimmten Bereichs liegt.
     *
     * @param value    Zu prüfender Wert
     * @param minValue Untere Grenze (einschließlich)
     * @param maxValue Obere Grenze (einschließlich)
     * @return True, wenn der Wert im Bereich liegt
     */
    public static boolean valueInRange(int value, int minValue, int maxValue) {
        return minValue <= value && value <= maxValue;
    }

    /**
     * Parst die angegebene Zeichenfolge in einen ganzzahligen Wert.
     * Wenn die Zeichenfolge aufgrund eines ungültigen Formats oder
     * eines Überlaufs nicht als Ganzzahl analysiert werden kann,
     * gibt die Methode {@link Integer#MIN_VALUE} als Fallback-Wert zurück.
     *
     * @param value Die als Ganzzahl zu analysierende Zeichenfolge.
     * @return Der durch die Zeichenfolge dargestellte Ganzzahlwert oder {@link Integer#MIN_VALUE}
     */
    public static int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    /**
     * Rundet einen Winkel auf das vorherige (linke) Vielfache von 90°.
     *
     * @param rotationAngle Beliebiger Winkel in Grad (auch negativ)
     * @return Größtes Vielfaches von 90°, das kleiner gleich dem aktuellen Winkel ist; normalisiert in [0,359]
     */
    public static int snapToLeft90(int rotationAngle) {
        int angle = normalizeAngle(rotationAngle);
        int mod = angle % 90;

        // Wenn bereits exakt auf einem 90°-Raster: 90° nach links springen
        if (mod == 0) {
            return (angle + 270) % 360;
        }

        // Ansonsten: nach links auf das vorherige 90°-Raster runden
        return (angle - mod + 360) % 360;
    }

    /**
     * Rundet einen Winkel auf das nächste (rechte) Vielfache von 90°.
     *
     * @param rotationAngle Beliebiger Winkel in Grad (auch negativ)
     * @return Kleinstes Vielfaches von 90°, das größer gleich dem aktuellen Winkel ist; normalisiert in [0,359]
     */
    public static int snapToRight90(int rotationAngle) {
        int angle = normalizeAngle(rotationAngle);
        int mod = angle % 90;

        // Wenn bereits exakt auf einem 90°-Raster: 90° nach rechts springen
        if (mod == 0) {
            return (angle + 90) % 360;
        }

        // Ansonsten: nach rechts auf das nächste 90°-Raster runden
        return (angle + (90 - mod)) % 360;
    }

    /**
     * Normalisiert einen Winkel auf den Bereich [0, 359].
     *
     * @param rotationAngle Beliebiger Winkel in Grad (auch negativ)
     * @return Normierter Winkel im Intervall [0, 359]
     */
    private static int normalizeAngle(int rotationAngle) {
        int a = rotationAngle % 360;
        return a < 0 ? a + 360 : a;
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
        timeConsumer.accept(formatDuration(duration));
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
    private static String formatDuration(long nanos) {
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