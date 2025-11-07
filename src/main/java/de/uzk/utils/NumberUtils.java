package de.uzk.utils;

import java.awt.*;

/**
 * Die Hilfsklasse für numerische Operationen und Berechnungen.
 * Diese Klasse bietet Methoden für:
 * <ul>
 *   <li>Bereichsprüfungen mit Schrittweiten</li>
 *   <li>Winkelberechnungen (90° Drehungen)</li>
 *   <li>Helligkeitsberechnungen für Farben</li>
 * </ul>
 *
 * <p>
 * Die Klasse ist als "final" deklariert und der Konstruktor ist privat,
 * um die Instanziierung zu verhindern, da alle Methoden statisch sind.
 */
public final class NumberUtils {
    // Toleranzwert für Rundungsfehler
    private static final double EPSILON = 1e-10;

    private NumberUtils() {
        // Verhindert die Instanziierung dieser Hilfsklasse
    }

    /**
     * Prüft, ob ein Wert innerhalb eines bestimmten Bereichs liegt
     * und in die Schrittweite (stepSize) passt.
     *
     * @param value    zu prüfender Wert
     * @param minValue untere Grenze (einschließlich)
     * @param maxValue obere Grenze (einschließlich)
     * @param stepSize Schrittweite, in der der Wert liegen muss
     * @return true, wenn der Wert im Bereich liegt und exakt auf die Schrittweite passt
     */
    public static boolean valueFitsInRange(double value, double minValue, double maxValue, double stepSize) {
        // Prüfe, ob der Wert innerhalb des Bereichs liegt
        if (value < minValue - EPSILON || value > maxValue + EPSILON) {
            return false;
        }

        // Berechne den Rest bei der Division (value - minValue) durch stepSize
        double remainder = (value - minValue) % stepSize;

        // Aufgrund von Rundungsfehlern kann der Rest leicht negativ sein – korrigieren
        if (remainder < 0) {
            remainder += stepSize;
        }

        // Prüfe, ob der Rest nahe bei 0 oder bei stepSize liegt
        return remainder < EPSILON || Math.abs(remainder - stepSize) < EPSILON;
    }

    /**
     * Dreht einen gegebenen Winkel um 90° nach links.
     *
     * @param oldAngle alter Winkel in Grad
     * @return neuer Winkel in Grad
     */
    public static int turn90Left(int oldAngle) {
        int angle = oldAngle % 360;
        if (angle == 0) return 270;

        int remainder = angle % 90;
        // Wenn der Winkel ein Vielfaches von 90° ist → einfach um 90° verringern
        if (remainder == 0) {
            return (360 + angle - 90) % 360;
        }
        // Sonst bis zum nächsten „linken“ Vielfachen von 90° drehen
        return (360 + angle - remainder) % 360;
    }

    /**
     * Dreht einen gegebenen Winkel um 90° nach rechts.
     *
     * @param oldAngle alter Winkel in Grad
     * @return neuer Winkel in Grad
     */
    public static int turn90Right(int oldAngle) {
        int angle = oldAngle % 360;
        if (angle == 270) return 0;

        int remainder = angle % 90;
        // Wenn der Winkel ein Vielfaches von 90° ist → einfach um 90° erhöhen
        if (remainder == 0) {
            return (360 + angle + 90) % 360;
        }
        // Sonst bis zum nächsten „rechten“ Vielfachen von 90° drehen
        return (360 + angle - remainder + 90) % 360;
    }

    /**
     * Berechnet die wahrgenommene Helligkeit einer Farbe nach der NTSC-Formel.
     * (Berücksichtigt die Empfindlichkeit des menschlichen Auges für R, G und B unterschiedlich.)
     *
     * @param color zu analysierende Farbe
     * @return wahrgenommene Helligkeit (0 = dunkel, 255 = hell)
     */
    public static double calculatePerceivedBrightness(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return (0.299 * r) + (0.587 * g) + (0.114 * b);
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
}