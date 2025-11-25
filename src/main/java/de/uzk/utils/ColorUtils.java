package de.uzk.utils;

import com.formdev.flatlaf.util.ColorFunctions;

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
     * Blauton zur Visualisierung von Debug-Informationen und primäre Standardfarbe für UI-Elemente.
     */
    public static final Color COLOR_BLUE = new Color(0, 122, 255);

    /**
     * Grünton zur Visualisierung von Standard-Informationen oder Erfolgszuständen.
     */
    public static final Color COLOR_GREEN = new Color(10, 160, 50);

    /**
     * Gelbton zur Visualisierung von Warnhinweisen.
     */
    public static final Color COLOR_YELLOW = new Color(250, 180, 60);

    /**
     * Rotton zur Visualisierung von Fehlern.
     */
    public static final Color COLOR_RED = new Color(250, 50, 50);

    /**
     * Dunkler Rotton zur Visualisierung von Ausnahmen.
     */
    public static final Color COLOR_DARK_RED = new Color(150, 0, 0);

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private ColorUtils() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Berechnet die wahrgenommene Helligkeit einer Farbe nach der NTSC-Formel.
     * (Berücksichtigt die Empfindlichkeit des menschlichen Auges für R, G und B unterschiedlich.)
     *
     * @param color Zu analysierende Farbe
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
     * Passt einen Farbwert an, indem er entweder aufgehellt oder abgedunkelt wird.
     *
     * @param color    Die Ausgangsfarbe, darf nicht {@code null} sein
     * @param factor   Der Intensitätsfaktor der Anpassung im Bereich von {@code 0} bis {@code 1}
     * @param brighten Wenn {@code true}, wird die Farbe aufgehellt; andernfalls wird sie abgedunkelt
     * @return Die angepasste Farbe
     * @throws NullPointerException     Falls {@code color} {@code null} ist
     * @throws IllegalArgumentException Falls der Faktor kleiner als {@code 0} oder größer als {@code 1} ist
     */
    public static Color adjustColor(Color color, float factor, boolean brighten) {
        if (color == null) throw new NullPointerException("Color is null.");
        if (factor < 0) throw new IllegalArgumentException("Factor must be greater than or equal to 0.");
        if (factor > 1) throw new IllegalArgumentException("Factor must be less than or equal to 1.");
        if (factor == 0) return color;

        if (brighten) return ColorFunctions.lighten(color, factor);
        else return ColorFunctions.darken(color, factor);
    }

    /**
     * Wandelt eine AWT-Farbe in eine Hexadezimaldarstellung um.
     *
     * @param color Zu konvertierende Farbe. Ist der Wert {@code null}, wird {@code "#000000"} zurückgegeben.
     * @return Hexadezimaldarstellung im Format {@code "#RRGGBB"} oder {@code "#AARRGGBB"} bei Transparenz
     */
    public static String colorToHex(Color color) {
        return colorToHex(color, false);
    }

    /**
     * Wandelt eine AWT-Farbe in eine Hexadezimaldarstellung um und erlaubt die explizite
     * Steuerung, ob der Alphakanal berücksichtigt werden soll.
     *
     * @param color       Zu konvertierende Farbe. Ist der Wert {@code null}, wird {@code "#000000"} zurückgegeben.
     * @param ignoreAlpha Wenn {@code true}, wird der Alphakanal nicht berücksichtigt
     * @return Hexadezimaldarstellung der Farbe im Format {@code "#RRGGBB"} oder {@code "#AARRGGBB"}
     */
    public static String colorToHex(Color color, boolean ignoreAlpha) {
        if (color == null) return "#000000";

        int alpha = color.getAlpha();
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        if (ignoreAlpha || alpha == 255) {
            return "#%02X%02X%02X".formatted(red, green, blue);
        }
        return "#%02X%02X%02X%02X".formatted(alpha, red, green, blue);
    }
}
