package de.uzk.utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Die Klasse {@link CLIStyle} ermöglicht die Formatierung von Text in der Konsole
 * mithilfe von ANSI-Escape-Codes. Sie unterstützt verschiedene Stile und Farben.
 * Diese Klasse ist nützlich, um die Lesbarkeit und das visuelle Erscheinungsbild
 * von Konsolenausgaben zu verbessern.
 *
 * <br><br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class CLIStyle {
    // ANSI Escape Prefix für die Formatierung
    private static final String ESC = "\033[";

    /**
     * Beispiel für die Nutzung von CLIStyle.
     * Zeigt alle Stile und Farboptionen.
     */
    public static void main(String[] args) {
        // Beispieltext
        String demoText = "Beispieltext";

        printLine("=== CLIStyle Demo ===");

        // Alle Stile einzeln
        printLine(CLIStyle.text(demoText).bold());
        printLine(CLIStyle.text(demoText).dim());
        printLine(CLIStyle.text(demoText).italic());
        printLine(CLIStyle.text(demoText).underline());
        printLine(CLIStyle.text(demoText).blink());
        printLine(CLIStyle.text(demoText).inverted());
        printLine(CLIStyle.text(demoText).hidden());
        printLine(CLIStyle.text(demoText).strikethrough());

        // Kombination von Stilen
        printLine(CLIStyle.text(demoText).bold().underline().italic());

        // Textfarbe RGB
        printLine(CLIStyle.text(demoText).foreground(new Color(255, 0, 0))); // Rot
        printLine(CLIStyle.text(demoText).foreground(new Color(0, 255, 0))); // Grün
        printLine(CLIStyle.text(demoText).foreground(new Color(0, 0, 255))); // Blau

        // Hintergrundfarbe RGB
        printLine(CLIStyle.text(demoText).background(new Color(255, 255, 0))); // Gelber Hintergrund
        printLine(CLIStyle.text(demoText).foreground(new Color(0, 0, 0)).background(new Color(255, 255, 0))); // Schwarz auf Gelb

        // Kombination von Text- und Hintergrundfarbe + Stile
        printLine(CLIStyle.text(demoText)
            .foreground(new Color(255, 255, 255))
            .background(new Color(128, 0, 128))
            .bold()
            .underline()
            .italic());

        printLine("=== Ende der Demo ===");
    }

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private CLIStyle() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Gibt das angegebene Objekt an die Standardausgabe aus.
     *
     * @param object Das auszugebende Objekt. Seine Zeichenfolgendarstellung wird an die Standardausgabe gesendet.
     */
    public static void printLine(Object object) {
        System.out.println(object);
    }

    /**
     * Einstiegsmethode, um eine neue {@code Text} Instanz zu erstellen.
     *
     * @param text Der Text, der formatiert werden soll.
     * @return Eine neue {@code Text} Instanz.
     */
    public static Text text(String text) {
        return new Text(text);
    }

    /**
     * Enum {@code TextStyle} definiert die verfügbaren Stile für den Text.
     */
    public enum TextStyle {
        RESET(0),          // Setzt alle Stile zurück
        BOLD(1),           // Fett
        DIM(2),            // Gedimmt
        ITALIC(3),         // Kursiv
        UNDERLINE(4),      // Unterstrichen
        BLINK(5),          // Blinkend
        INVERTED(7),       // Farben invertieren
        HIDDEN(8),         // Versteckt
        STRIKETHROUGH(9);  // Durchgestrichen

        private final int ansiCode;

        /**
         * Konstruktor für TextStyle.
         *
         * @param ansiCode ANSI-Code des Stils
         */
        TextStyle(int ansiCode) {
            this.ansiCode = ansiCode;
        }

        /**
         * Gibt den ANSI-Code zurück.
         *
         * @return ANSI-Code
         */
        public int getAnsiCode() {
            return this.ansiCode;
        }
    }

    /**
     * {@code Text} dient zum schrittweisen Aufbau eines formatierten Textes
     * mit Farben und Stilen.
     */
    public static class Text {

        /**
         * Der Text, der formatiert werden soll
         */
        private final String text;
        /**
         * Textfarbe
         */
        private Color foregroundColor;

        /**
         * Hintergrundfarbe
         */
        private Color backgroundColor;

        /**
         * Liste der angewendeten Stile
         */
        private final List<TextStyle> styles;

        /**
         * Konstruktor für die Text-Klasse.
         *
         * @param text Der Text, der formatiert werden soll.
         */
        private Text(String text) {
            this.text = text;
            this.foregroundColor = null;
            this.backgroundColor = null;
            this.styles = new ArrayList<>();
        }

        /**
         * Setzt die Textfarbe.
         *
         * @param color Die Textfarbe
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text foreground(Color color) {
            this.foregroundColor = color;
            return this;
        }

        /**
         * Setzt die Hintergrundfarbe.
         *
         * @param color Die Hintergrundfarbe
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text background(Color color) {
            this.backgroundColor = color;
            return this;
        }

        /**
         * Fügt den Stil "Fett" hinzu.
         *
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text bold() {
            this.styles.add(TextStyle.BOLD);
            return this;
        }

        /**
         * Fügt den Stil "Gedimmt" hinzu.
         *
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text dim() {
            this.styles.add(TextStyle.DIM);
            return this;
        }

        /**
         * Fügt den Stil "Kursiv" hinzu.
         *
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text italic() {
            this.styles.add(TextStyle.ITALIC);
            return this;
        }

        /**
         * Fügt den Stil "Unterstrichen" hinzu.
         *
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text underline() {
            this.styles.add(TextStyle.UNDERLINE);
            return this;
        }

        /**
         * Fügt den Stil "Blinkend" hinzu.
         *
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text blink() {
            this.styles.add(TextStyle.BLINK);
            return this;
        }

        /**
         * Fügt den Stil "Farben invertieren" hinzu.
         *
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text inverted() {
            this.styles.add(TextStyle.INVERTED);
            return this;
        }

        /**
         * Fügt den Stil "Versteckt" hinzu.
         *
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text hidden() {
            this.styles.add(TextStyle.HIDDEN);
            return this;
        }

        /**
         * Fügt den Stil "Durchgestrichen" hinzu.
         *
         * @return Die aktuelle {@code Text} Instanz
         */
        public Text strikethrough() {
            this.styles.add(TextStyle.STRIKETHROUGH);
            return this;
        }

        /**
         * Baut den formatierten Text mit ANSI-Codes.
         *
         * @return Formatierter Text
         */
        public String build() {
            List<String> ansiCodes = new ArrayList<>();

            // Stile hinzufügen
            for (TextStyle style : this.styles) {
                ansiCodes.add(String.valueOf(style.getAnsiCode()));
            }

            // Textfarbe (RGB)
            if (this.foregroundColor != null) {
                ansiCodes.add("38;2;" +
                    this.foregroundColor.getRed() + ";" +
                    this.foregroundColor.getGreen() + ";" +
                    this.foregroundColor.getBlue());
            }

            // Hintergrundfarbe (RGB)
            if (this.backgroundColor != null) {
                ansiCodes.add("48;2;" +
                    this.backgroundColor.getRed() + ";" +
                    this.backgroundColor.getGreen() + ";" +
                    this.backgroundColor.getBlue());
            }

            // Escape-Sequenz aufbauen
            String prefix = ESC + String.join(";", ansiCodes) + "m";
            String reset = ESC + TextStyle.RESET.getAnsiCode() + "m";

            return prefix + this.text + reset;
        }

        /**
         * Gibt den formatierten Text zurück.
         *
         * @return Der formatierte Text
         */
        @Override
        public String toString() {
            return build();
        }
    }
}
