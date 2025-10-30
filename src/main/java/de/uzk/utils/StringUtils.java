package de.uzk.utils;

import java.awt.*;
import java.io.File;

/**
 * Die Hilfsklasse für String-Operationen und Formatierungen.
 * Diese Klasse bietet Methoden für:
 * <ul>
 *   <li>Array-zu-String Konvertierungen</li>
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

    // ---------- HTML FORMAT ----------

    /**
     * Umgibt Text mit einem HTML-Font-Tag, der die Farbe setzt.
     *
     * @param text  der anzuzeigende Text
     * @param color gewünschte Schriftfarbe
     * @return Text in HTML-Font-Tag mit Farbangabe
     */
    public static String wrapColor(String text, Color color) {
        return "<span color=\"" + colorToHex(color) + "\">" + text + "</span>";
    }

    /**
     * Ändert die Schriftgröße eines Textes durch HTML-Formatierung.
     * Die Größe wird in Prozent des Standardwertes angegeben und in em-Einheiten umgerechnet.
     * Der Text wird in einem span-Tag platziert.
     *
     * @param text       der zu formatierende Text
     * @param percentage Schriftgröße in Prozent (0-500)
     * @return HTML-formatierter Text mit angepasster Schriftgröße
     * @throws IllegalArgumentException wenn der Prozentwert außerhalb des gültigen Bereichs liegt
     */
    public static String applyFontSize(String text, int percentage) {
        if (percentage < 0 || percentage > 500)
            throw new IllegalArgumentException("Percentage must be between 0 and 500");
        String fontSizeString = (percentage / 100.0) + "em";
        return "<span style='font-size: %s;'>".formatted(fontSizeString) + text + "</span>";
    }

    /**
     * Hebt den Text fett hervor.
     */
    public static String wrapBold(String text) {
        return "<b>" + text + "</b>";
    }

    /**
     * Hebt den Text kursiv hervor.
     */
    public static String wrapItalic(String text) {
        return "<i>" + text + "</i>";
    }

    /**
     * Richtet den angegebenen Text aus, indem er in ein formatiertes HTML-Element <div> eingeschlossen wird.
     * Die Ausrichtung und die maximale Breite des Elements werden als Parameter angegeben.
     *
     * @param text     Der auszurichtende Text
     * @param align    Der Ausrichtungsstil (z. B. „left“, „center“, „right“, „justify“)
     * @param maxWidth Die maximale Breite des Elements in Pixeln
     * @return Eine formatierte HTML-Zeichenfolge, die den ausgerichteten Text enthält
     */
    public static String alignText(String text, String align, int maxWidth) {
        return "<div style=\"text-align: %s; width: %d; word-wrap: break-word;\">".formatted(align, maxWidth) + text + "</div>";
    }

    /**
     * Umgibt den Text mit einem <pre>-Tag und
     * ersetzt Zeilenumbrüche durch <br> und Tabs durch Leerzeichen.
     */
    public static String wrapPre(String text) {
        return "<pre>" + text
                .replace(NEXT_LINE, "<br>")
                .replace("\t", "    ")
                + "</pre>";
    }

    /**
     * Umgibt Text mit dem HTML-Haupttag.
     */
    public static String wrapHtml(String text) {
        return "<html>" + text + "</html>";
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
     * Wandelt eine AWT-Farbe in einen Hexadezimal-String um (#RRGGBB bzw. #AARRGGBB, wenn Alpha != 255).
     *
     * @param color zu konvertierende Farbe
     * @return Hexadezimalwert als String (z. B. "#FF00FF" oder "#80FF00FF" mit Alpha)
     */
    public static String colorToHex(Color color) {
        if (color == null) return "#000000";

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int alpha = color.getAlpha();

        if (alpha != 255) {
            int rgba = (red << 24) | (green << 16) | (blue << 8) | alpha;
            return String.format("#%08X", rgba);
        }

        int rgb = (red << 16) | (green << 8) | blue;
        return String.format("#%06X", rgb);
    }
}
