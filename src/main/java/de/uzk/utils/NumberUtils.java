package de.uzk.utils;

/**
 * Die Hilfsklasse für numerische Operationen und Berechnungen.
 *
 * <p>
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

    private NumberUtils() {
        // Verhindert die Instanziierung dieser Hilfsklasse
    }

    /**
     * Prüft, ob ein Wert innerhalb eines bestimmten Bereichs liegt.
     *
     * @param value    Zu prüfender Wert
     * @param minValue Untere Grenze (einschließlich)
     * @param maxValue Obere Grenze (einschließlich)
     * @return True, wenn der Wert im Bereich liegt
     */
    public static boolean valueInRange(double value, double minValue, double maxValue) {
        return value >= minValue - EPSILON && value <= maxValue + EPSILON;
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
        return (angle / 90) * 90; // integer division → floor
    }

    /**
     * Rundet einen Winkel auf das nächste (rechte) Vielfache von 90°.
     *
     * @param rotationAngle Beliebiger Winkel in Grad (auch negativ)
     * @return Kleinstes Vielfaches von 90°, das größer gleich dem aktuellen Winkel ist; normalisiert in [0,359]
     */
    public static int snapToRight90(int rotationAngle) {
        int angle = normalizeAngle(rotationAngle);
        return ((angle + 89) / 90) * 90; // trick → ceil-artig
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
}