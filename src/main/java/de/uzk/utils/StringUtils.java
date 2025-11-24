package de.uzk.utils;

import de.uzk.gui.UIEnvironment;

import java.awt.*;
import java.io.File;

/**
 * Utility-Klasse für Zeichenkettenoperationen und HTML-Formatierungen.
 *
 * <br><br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class StringUtils {
    /**
     * Systemabhängiges Dateitrennzeichen (z.&nbsp;B. "/" oder "\\")
     */
    public static final String FILE_SEP = File.separator;

    /**
     * Systemabhängiger Zeilenumbruch (z.&nbsp;B. "\n" oder "\r\n")
     */
    public static final String NEXT_LINE = System.lineSeparator();

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private StringUtils() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Formatiert ein Array als Zeichenkette mit Trennzeichen und Begrenzern.
     *
     * @param arr         Zu formatierende Array
     * @param arrSep      Trennzeichen zwischen den Elementen
     * @param leftBorder  Linke Begrenzung (z.&nbsp;B. '[')
     * @param rightBorder Rechte Begrenzung (z.&nbsp;B. ']')
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

    // ========================================
    // HTML FORMATIERUNG
    // ========================================

    /**
     * Hebt den angegebenen Text fett hervor.
     *
     * @param text Der Text, der fett dargestellt werden soll
     * @return HTML-String mit {@code <b>}-Tag
     */
    public static String wrapBold(String text) {
        return "<b>%s</b>".formatted(text);
    }

    /**
     * Hebt den angegebenen Text kursiv hervor.
     *
     * @param text Der Text, der kursiv dargestellt werden soll
     * @return HTML-String mit {@code <i>}-Tag
     */
    public static String wrapItalic(String text) {
        return "<i>%s</i>".formatted(text);
    }


    /**
     * Hebt den angegebenen Text unterstrichen hervor.
     *
     * @param text Der Text, der unterstrichen dargestellt werden soll
     * @return HTML-String mit {@code <u>}-Tag
     */
    public static String wrapUnderlined(String text) {
        return "<u>%s</u>".formatted(text);
    }

    /**
     * Umgibt den Text mit einem {@code <span>}-Tag, der eine Schriftfarbe definiert.
     *
     * @param text  Der anzuzeigende Text
     * @param color Gewünschte Schriftfarbe
     * @return HTML-String mit farbigem Text
     */
    public static String applyColor(String text, Color color) {
        return "<span style='color:%s;'>%s</span>".formatted(ColorUtils.colorToHex(color), text);
    }

    /**
     * Ändert die Schriftgröße eines Textes durch HTML-Formatierung.
     * <p>
     * Die Schriftgröße wird in Prozent angegeben (relativ zur Standardgröße)
     * und intern in <code>em</code>-Einheiten umgesetzt.
     *
     * @param text       Zu formatierendem Text
     * @param percentage Schriftgröße in Prozent (0–500)
     * @return HTML-formatierter Text mit angepasster Schriftgröße
     * @throws IllegalArgumentException Falls der Prozentwert außerhalb des gültigen Bereichs liegt
     */
    public static String applyFontSize(String text, int percentage) {
        if (!NumberUtils.valueInRange(percentage, 0, 500)) {
            throw new IllegalArgumentException("Percentage value must be between 0 and 500.");
        }

        String fontSize = (percentage / 100.0) + "em";
        return "<span style='font-size:%s;'>%s</span>".formatted(fontSize, text);
    }

    /**
     * Umgibt Text mit einem {@code <pre>}-Tag und ersetzt
     * Zeilenumbrüche durch {@code <br>}-Tags.
     *
     * @param text Zu formatierendem Text
     * @return HTML-String im {@code <pre>}-Format
     */
    public static String wrapPre(String text) {
        return "<pre>%s</pre>".formatted(text.replace(NEXT_LINE, "<br>"));
    }

    /**
     * Erstellt einen klickbaren Hyperlink aus Text und URL.
     *
     * @param text Der anzuzeigende Text
     * @param url  Die Ziel-URL
     * @return HTML-Link {@code <a href="">text</a>}
     */
    public static String wrapA(String text, String url) {
        return "<a href='%s'>%s</a>".formatted(url, text);
    }

    /**
     * Erkennt URLs automatisch.
     *
     * @param text Der Eingabetext (z.&nbsp;B. mit Zeilenumbrüchen)
     * @return HTML-formatierter Text mit Hyperlinks
     */
    public static String wrapLinks(String text) {
        if (text == null || text.isEmpty()) return "";

        String[] words = text.replace(NEXT_LINE, "<br>")
            .replace("<br>", " <br>")
            .trim()
            .split("\\s+");

        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            // Hyperlink-Erkennung (http/https)
            if (word.matches("https?://\\S+")) {
                // Trenne Link von allen abschließenden Satzzeichen
                int endIndex = word.length();
                while (endIndex > 0 && ".!?;,:()[][]{}".indexOf(word.charAt(endIndex - 1)) != -1) {
                    endIndex--;
                }

                // Den eigentlichen Link einbetten
                String link = word.substring(0, endIndex);
                builder.append(wrapA(link, link));

                // Satzzeichen am Ende wieder anhängen
                String punctuation = word.substring(endIndex);
                builder.append(punctuation);
            } else {
                builder.append(word);
            }
            builder.append(" ");
        }

        return builder.toString();
    }

    /**
     * Richtet den Text in einem {@code <div>}-Tag aus.
     *
     * @param text     Der auszurichtende Text
     * @param align    Textausrichtung (z.&nbsp;B. "left", "center", "right", "justify")
     * @param maxWidth Maximale Breite in Pixeln
     * @return HTML-String mit entsprechendem Stil
     */
    public static String applyDivAlignment(String text, String align, int maxWidth) {
        return """
            <div style="text-align:%s; width:%dpx; word-wrap:break-word;">
                %s
            </div>
            """.formatted(align, maxWidth, text.trim());
    }

    /**
     * Erzeugt ein vollständiges HTML-Dokument mit eingebetteten CSS-Stilregeln.
     * <p>
     * Der erzeugte HTML-Block nutzt die angegebene Schriftart und definiert
     * ein einfaches Standard-Layout:
     * <ul>
     *   <li>Verwendet die angegebene Schriftart für den gesamten Text.</li>
     *   <li>Reduziert Abstände bei {@code <pre>}-Elementen.</li>
     * </ul>
     *
     * @param htmlContent Der eigentliche HTML-Inhalt
     * @param fontName    Name der zu verwendenden Schriftart (z.&nbsp;B. "Arial", "Monospaced")
     * @return Vollständiger HTML-String mit eingebettetem CSS-Stil
     */
    public static String wrapHtml(String htmlContent, String fontName) {
        return """
            <html>
                <head>
                    <style>
                        body { font-family: %s; }
                        pre { margin: 5px 0; }
                    </style>
                </head>
                <body>
                    %s
                </body>
            </html>
            """.formatted(fontName, htmlContent.trim().replaceAll(NEXT_LINE, NEXT_LINE + "\t".repeat(2)));
    }

    /**
     * Umgibt den angegebenen HTML-Inhalt mit einem vollständigen {@code <html>}-Block
     * und verwendet dabei die Standard-Schriftart des Systems.
     * <p>
     * Diese Methode ist eine bequeme Kurzform von
     * {@link #wrapHtml(String, String)} und nutzt {@link UIEnvironment#getFontName()}
     * als Standardfont.
     *
     * @param htmlContent Der einzubettende HTML-Inhalt
     * @return Vollständiger HTML-String inklusive {@code <html>}-, {@code <head>}- und {@code <body>}-Tags
     */
    public static String wrapHtml(String htmlContent) {
        return wrapHtml(htmlContent, UIEnvironment.getFontName());
    }

    /**
     * Formatiert beliebige Texteingaben zu einfachem HTML,
     * erkennt URLs automatisch und richtet den Text nach Wunsch aus.
     *
     * @param text     Der Eingabetext (z.&nbsp;B. mit Zeilenumbrüchen)
     * @param align    Textausrichtung (z.&nbsp;B. "left", "center", "right", "justify")
     * @param maxWidth Maximale Breite in Pixeln
     * @return HTML-formatierter Text
     */
    public static String wrapHtmlWithLinks(String text, String align, int maxWidth) {
        String aligned = applyDivAlignment(wrapLinks(text), align, maxWidth);
        return wrapHtml(aligned);
    }
}
