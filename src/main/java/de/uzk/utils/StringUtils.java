package de.uzk.utils;

import org.intellij.lang.annotations.MagicConstant;

import java.awt.*;
import java.io.File;

/**
 * Die Hilfsklasse für String-Operationen und Formatierungen.
 * Diese Klasse bietet Methoden für:
 * <ul>
 *   <li>Formatierung von Ebenen und Zeitangaben</li>
 *   <li>Array-zu-String Konvertierungen</li>
 *   <li>Text-Parsing und Wort-Extraktion</li>
 *   <li>HTML-Formatierung und Styling</li>
 *   <li>Farb-zu-Hex Konvertierung</li>
 * </ul>
 *
 * <p>
 * Die Klasse ist als "final" deklariert und der Konstruktor ist privat,
 * um die Instanziierung zu verhindern, da alle Methoden statisch sind.
 */
public final class StringUtils {
    // Systemabhängige Dateitrennzeichen (z. B. "/" oder "\")
    public static final String FILE_SEP = File.separator;

    // Systemabhängiger Zeilenumbruch (z. B. "\n" oder "\r\n")
    public static final String NEXT_LINE = System.lineSeparator();

    private StringUtils() {
        // Verhindert die Instanziierung dieser Hilfsklasse
    }

    /**
     * Formatiert eine numerische Ebene (z. B. Höhenwert) in Mikrometer.
     *
     * @param level      numerischer Wert (z. B. Ebenennummer)
     * @param multiplier Umrechnungsfaktor zu μm
     * @return formatierte Zeichenkette (z. B. „12.3 μm“)
     */
    public static String formatLevel(int level, double multiplier) {
        return String.format("%.01f μm", level * multiplier);
    }

    /**
     * Formatiert eine Zeitangabe (z. B. Sekunden) im Format hh:mm:ss.
     *
     * @param time       Zeitwert (z. B. Sekunden)
     * @param multiplier Multiplikator zur Umrechnung (z. B. Zeitfaktor)
     * @return formatierte Zeitzeichenkette
     */
    public static String formatTime(int time, double multiplier) {
        time = (int) (time * multiplier);

        int seconds = time % 60;
        int minute = time / 60 % 60;
        int hour = time / 60 / 60;

        return String.format("%02d:%02d:%02d", hour, minute, seconds);
    }

    /**
     * Formatiert ein Array als Zeichenkette mit Trennzeichen und Begrenzern.
     *
     * @param arr         Array, das formatiert werden soll
     * @param arrSep      Trennzeichen zwischen Elementen
     * @param leftBorder  linke Klammer (z. B. '[')
     * @param rightBorder rechte Klammer (z. B. ']')
     * @return formatierte Array-Darstellung, z. B. [1,2,3]
     */
    public static String formatArray(Object[] arr, String arrSep, char leftBorder, char rightBorder) {
        if (arr == null) return String.valueOf(leftBorder) + rightBorder;

        int iMax = arr.length - 1;
        if (iMax == -1) return String.valueOf(leftBorder) + rightBorder;

        StringBuilder arrBuilder = new StringBuilder();
        arrBuilder.append(leftBorder);

        for (int i = 0; i <= iMax; i++) {
            arrBuilder.append(arr[i]);
            if (i == iMax) break;
            arrBuilder.append(arrSep);
        }
        return arrBuilder.append(rightBorder).toString();
    }

    /**
     * Teilt einen Text anhand von Leerzeichen in einzelne Wörter.
     *
     * @param text Eingabetext
     * @return Array aus Wörtern (leer, wenn text == null)
     */
    public static String[] splitTextInWords(String text) {
        return text == null ? new String[0] : text.split("\\s+");
    }

    // ---------- HTML FORMAT ----------

    /**
     * Umgibt Text mit einem HTML-Font-Tag, das die Farbe setzt.
     *
     * @param text  der anzuzeigende Text
     * @param color gewünschte Schriftfarbe
     * @return Text in HTML-Font-Tag mit Farbangabe
     */
    public static String wrapColor(String text, Color color) {
        return "<font color=\"" + colorToHex(color) + "\">" + text + "</font>";
    }

    /**
     * Hebt den Text fett hervor (HTML <b>-Tag).
     */
    public static String wrapBold(String text) {
        return "<b>" + text + "</b>";
    }

    /**
     * Hebt den Text kursiv hervor (HTML <i>-Tag).
     */
    public static String wrapItalic(String text) {
        return "<i>" + text + "</i>";
    }

    /**
     * Zentriert den Text (HTML <center>-Tag).
     */
    public static String wrapCenter(String text) {
        return "<center>" + text + "</center>";
    }

    /**
     * Umgibt den Text mit einem HTML-Absatz (<p>),
     * ersetzt Zeilenumbrüche durch <br> und Tabs durch Leerzeichen.
     */
    public static String wrapP(String text) {
        return "<p>" + text
                .replace(NEXT_LINE, "<br>")
                .replace("\t", "    ")
                + "</p>";
    }

    /**
     * Baut ein vollständiges HTML-Dokument mit Grundstil auf
     * (Monospaced-Schriftart und einfache Absatzabstände).
     *
     * @param htmlContent der eigentliche HTML-Inhalt
     * @return vollständiges HTML-Dokument
     */
    public static String wrapHtmlDocument(String htmlContent) {
        String fontFamilyText = "font-family: %s;".formatted("monospaced");
        return wrapHtml("<head><style>body { %s } p {margin: 5px 0}</style></head><body>"
                .formatted(fontFamilyText) + htmlContent + "</body>");
    }

    /**
     * Umgibt Text mit dem HTML-Haupttag <html>.
     */
    public static String wrapHtml(String text) {
        return "<html>" + text + "</html>";
    }

    /**
     * Wendet einen bestimmten Schriftstil auf Text an (fett, kursiv oder beides).
     *
     * @param text      der Text, der formatiert werden soll
     * @param fontStyle Schriftstil (Font.PLAIN, Font.BOLD, Font.ITALIC)
     * @return entsprechend formatierter Text in HTML-Form
     */
    public static String applyFontStyle(String text, @MagicConstant(flags = {Font.PLAIN, Font.BOLD, Font.ITALIC}) int fontStyle) {
        return switch (fontStyle) {
            case Font.BOLD -> wrapBold(text);
            case Font.ITALIC -> wrapItalic(text);
            case Font.BOLD | Font.ITALIC -> wrapBold(wrapItalic(text));
            default -> text;
        };
    }

    /**
     * Wandelt eine AWT-Farbe in einen Hexadezimal-String um (#RRGGBB).
     *
     * @param color zu konvertierende Farbe
     * @return Hexadezimalwert als String (z. B. "#FF00FF")
     */
    public static String colorToHex(Color color) {
        if (color == null) return "#000000";

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int rgb = (r << 16) | (g << 8) | b;

        return String.format("#%06X", rgb);
    }
}
