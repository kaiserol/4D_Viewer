package de.uzk.utils;

import de.uzk.gui.GuiUtils;

import java.awt.*;
import java.io.File;

/**
 * Dienstklasse für Zeichenkettenoperationen und HTML-Formatierungen.
 * <p>
 * Bietet Methoden zur formatierten Darstellung von Arrays,
 * Einbettung von Text in HTML-Tags (z.&nbsp;B. fett, kursiv, Farbe, Größe, Ausrichtung)
 * sowie zur Umwandlung von {@link Color}-Objekten in Hexadezimaldarstellung.
 * <p>
 * Diese Klasse ist nicht instanziierbar.
 */
public final class StringUtils {
    /**
     * Systemabhängiges Dateitrennzeichen (z.&nbsp;B. "/" oder "\\").
     */
    public static final String FILE_SEP = File.separator;

    /**
     * Systemabhängiger Zeilenumbruch (z.&nbsp;B. "\n" oder "\r\n").
     */
    public static final String NEXT_LINE = System.lineSeparator();

    private StringUtils() {
        // Verhindert Instanziierung dieser Hilfsklasse
    }

    /**
     * Formatiert ein Array als Zeichenkette mit Trennzeichen und Begrenzern.
     *
     * @param arr         das zu formatierende Array
     * @param arrSep      Trennzeichen zwischen den Elementen
     * @param leftBorder  linke Begrenzung (z.&nbsp;B. '[')
     * @param rightBorder rechte Begrenzung (z.&nbsp;B. ']')
     * @return Formatierte Darstellung, z.&nbsp;B. "[1, 2, 3]"
     */
    public static String formatArray(Object[] arr, String arrSep, char leftBorder, char rightBorder) {
        if (arr == null || arr.length == 0) return leftBorder + "" + rightBorder;

        StringBuilder builder = new StringBuilder();
        builder.append(leftBorder);
        for (int i = 0; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i < arr.length - 1) builder.append(arrSep);
        }
        return builder.append(rightBorder).toString();
    }

    // ==========================================================
    // HTML FORMATIERUNG
    // ==========================================================

    /**
     * Richtet den Text in einem &lt;div&gt;-Tag aus.
     *
     * @param text     der auszurichtende Text
     * @param align    Textausrichtung (z.&nbsp;B. "left", "center", "right", "justify")
     * @param maxWidth maximale Breite in Pixeln
     * @return HTML-String mit entsprechendem Stil
     */
    public static String applyDivAlignment(String text, String align, int maxWidth) {
        return String.format(
                "<div style=\"text-align:%s; width:%dpx; word-wrap:break-word;\">%s</div>",
                align, maxWidth, text
        );
    }

    /**
     * Umgibt den Text mit einem &lt;span&gt;-Tag, der eine Schriftfarbe definiert.
     *
     * @param text  der anzuzeigende Text
     * @param color gewünschte Schriftfarbe
     * @return HTML-String mit farbigem Text
     */
    public static String applyColor(String text, Color color) {
        return String.format("<span style=\"color:%s;\">%s</span>", colorToHex(color), text);
    }

    /**
     * Ändert die Schriftgröße eines Textes durch HTML-Formatierung.
     * <p>
     * Die Schriftgröße wird in Prozent angegeben (relativ zur Standardgröße)
     * und intern in <code>em</code>-Einheiten umgesetzt.
     *
     * @param text       der zu formatierende Text
     * @param percentage Schriftgröße in Prozent (0–500)
     * @return HTML-formatierter Text mit angepasster Schriftgröße
     * @throws IllegalArgumentException falls der Prozentwert außerhalb des gültigen Bereichs liegt
     */
    public static String applyFontSize(String text, int percentage) {
        if (percentage < 0 || percentage > 500)
            throw new IllegalArgumentException("Prozentwert muss zwischen 0 und 500 liegen.");

        String fontSize = (percentage / 100.0) + "em";
        return String.format("<span style=\"font-size:%s;\">%s</span>", fontSize, text);
    }

    /**
     * Hebt den angegebenen Text fett hervor.
     *
     * @param text der Text, der fett dargestellt werden soll
     * @return HTML-String mit &lt;b&gt;-Tag
     */
    public static String wrapBold(String text) {
        return "<b>" + text + "</b>";
    }

    /**
     * Hebt den angegebenen Text kursiv hervor.
     *
     * @param text der Text, der kursiv dargestellt werden soll
     * @return HTML-String mit &lt;i&gt;-Tag
     */
    public static String wrapItalic(String text) {
        return "<i>" + text + "</i>";
    }

    /**
     * Erstellt einen klickbaren Hyperlink aus Text und URL.
     *
     * @param text der anzuzeigende Text
     * @param url  die Ziel-URL
     * @return HTML-Link (&lt;a href="..."&gt;text&lt;/a&gt;)
     */
    public static String wrapA(String text, String url) {
        return String.format("<a href=\"%s\">%s</a>", url, text);
    }

    /**
     * Umgibt Text mit einem &lt;pre&gt;-Tag und ersetzt
     * Zeilenumbrüche durch &lt;br&gt; sowie Tabs durch Leerzeichen.
     *
     * @param text der zu formatierende Text
     * @return HTML-String im &lt;pre&gt;-Format
     */
    public static String wrapPre(String text) {
        return "<pre>" + text.replace(NEXT_LINE, "<br>").replace("\t", "    ") + "</pre>";
    }

    /**
     * Umgibt den angegebenen HTML-Inhalt mit einem vollständigen &lt;html&gt;-Block
     * und verwendet dabei die Standard-Schriftart des Systems.
     * <p>
     * Diese Methode ist eine bequeme Kurzform von
     * {@link #wrapHtml(String, String)} und nutzt {@link GuiUtils#getFontName()}
     * als Standardfont.
     *
     * @param htmlContent der einzubettende HTML-Inhalt
     * @return vollständiger HTML-String inklusive &lt;html&gt;-, &lt;head&gt;- und &lt;body&gt;-Tags
     */
    public static String wrapHtml(String htmlContent) {
        return wrapHtml(htmlContent, GuiUtils.getFontName());
    }

    /**
     * Erzeugt ein vollständiges HTML-Dokument mit eingebetteten CSS-Stilregeln.
     * <p>
     * Der erzeugte HTML-Block nutzt die angegebene Schriftart und definiert
     * ein einfaches Standard-Layout:
     * <ul>
     *   <li>Verwendet die angegebene Schriftart für den gesamten Text.</li>
     *   <li>Reduziert Abstände bei &lt;pre&gt;-Elementen.</li>
     *   <li>Setzt Standard-Farbe (schwarz) und Hover-Farbe
     *       ({@link GuiUtils#COLOR_BLUE}) für Links.</li>
     * </ul>
     *
     * @param htmlContent der eigentliche HTML-Inhalt
     * @param fontName    Name der zu verwendenden Schriftart (z.&nbsp;B. "Arial", "Monospaced")
     * @return vollständiger HTML-String mit eingebettetem CSS-Stil
     */
    public static String wrapHtml(String htmlContent, String fontName) {
        String style = String.format("""
                body { font-family: %s; }
                pre { margin: 5px 0; }
                a { color: %s; }
                a:hover { color: %s; }
                """, fontName, colorToHex(Color.BLACK), colorToHex(GuiUtils.COLOR_BLUE));

        return "<html><head><style>" + style + "</style></head><body>" + htmlContent + "</body></html>";
    }

    /**
     * Formatiert beliebige Texteingaben zu einfachem HTML,
     * erkennt URLs automatisch und richtet den Text nach Wunsch aus.
     *
     * @param text     der Eingabetext (z.&nbsp;B. mit Zeilenumbrüchen)
     * @param align    Textausrichtung (z.&nbsp;B. "left", "center", "right", "justify")
     * @param maxWidth maximale Breite in Pixeln
     * @return HTML-formatierter Text
     */
    public static String formatInputToHTML(String text, String align, int maxWidth) {
        if (text == null || text.isEmpty()) return "";

        String[] words = text.replace("\r\n", "<br>")
                .replace("\n", "<br>")
                .trim()
                .split("\\s+");

        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            // Hyperlink-Erkennung (http/https)
            if (word.matches("https?://\\S+")) {
                builder.append(wrapA(word, word));
            } else {
                builder.append(word);
            }
            builder.append(" ");
        }

        String aligned = applyDivAlignment(builder.toString().trim(), align, maxWidth);
        return wrapHtml(aligned);
    }

    /**
     * Wandelt eine AWT-Farbe in eine Hexadezimaldarstellung um.
     * <p>
     * Falls der Alphawert ungleich 255 ist, wird dieser ebenfalls berücksichtigt.
     *
     * @param color die zu konvertierende Farbe
     * @return Hexadezimalwert (z.&nbsp;B. "#FF00FF" oder "#80FF00FF" bei Transparenz)
     */
    public static String colorToHex(Color color) {
        if (color == null) return "#000000";

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int alpha = color.getAlpha();

        if (alpha != 255) {
            return String.format("#%02X%02X%02X%02X", alpha, red, green, blue);
        }
        return String.format("#%02X%02X%02X", red, green, blue);
    }
}
