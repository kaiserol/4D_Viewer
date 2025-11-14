package de.uzk.utils;

import java.awt.*;

/**
 * Utility-Klasse für Farboperationen und Farbkonvertierungen.
 *
 * <br><br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class ColorUtils {
    /**
     * Farben
     */
    public static final Color COLOR_BLUE = new Color(0, 122, 255);
    public static final Color COLOR_GREEN = new Color(8, 166, 52);
    public static final Color COLOR_YELLOW = new Color(252, 204, 78);
    public static final Color COLOR_RED = new Color(255, 86, 86);
    public static final Color COLOR_DARK_RED = new Color(148, 0, 0);

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private ColorUtils() {
        // Verhindert die Instanziierung dieser Hilfsklasse
    }

    /**
     * Berechnet die wahrgenommene Helligkeit einer Farbe nach der NTSC-Formel.
     * (Berücksichtigt die Empfindlichkeit des menschlichen Auges für R, G und B unterschiedlich.)
     *
     * @param color Die zu analysierende Farbe
     * @return Wahrgenommene Helligkeit (0 = dunkel, 1 = hell)
     */
    public static double calculatePerceivedBrightness(Color color) {
        if (color == null) return 0;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return ((0.299 * r) + (0.587 * g) + (0.114 * b)) / 255;
    }

    /**
     * Wandelt eine AWT-Farbe in eine Hexadezimaldarstellung um.
     *
     * @param color Die zu konvertierende Farbe. Ist der Wert {@code null}, wird {@code "#000000"} zurückgegeben.
     * @return Hexadezimaldarstellung im Format {@code "#RRGGBB"} oder {@code "#AARRGGBB"} bei Transparenz
     */
    public static String colorToHex(Color color) {
        return colorToHex(color, false);
    }

    /**
     * Wandelt eine AWT-Farbe in eine Hexadezimaldarstellung um und erlaubt die explizite
     * Steuerung, ob der Alphakanal berücksichtigt werden soll.
     *
     * @param color       Die zu konvertierende Farbe. Ist der Wert {@code null}, wird {@code "#000000"} zurückgegeben.
     * @param ignoreAlpha Wenn {@code true}, wird der Alphakanal nicht berücksichtigt
     * @return Hexadezimaldarstellung der Farbe im Format {@code "#RRGGBB"} oder {@code "#AARRGGBB"}
     */
    public static String colorToHex(Color color, boolean ignoreAlpha) {
        if (color == null) return "#000000";

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int alpha = color.getAlpha();

        if (ignoreAlpha || alpha == 255) {
            return String.format("#%02X%02X%02X", red, green, blue);
        }
        return String.format("#%02X%02X%02X%02X", alpha, red, green, blue);
    }
}
