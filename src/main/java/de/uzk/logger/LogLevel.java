package de.uzk.logger;

import de.uzk.utils.ColorUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

import static de.uzk.config.LanguageHandler.getWord;

/**
 * Repräsentiert die verschiedenen Protokollebenen für Log-Einträge.
 *
 * <p>
 * Jede Protokollebene besitzt:
 *
 * <ul>
 *     <li>einen Klartext-Namen</li>
 *     <li>eine zugehörige Farbe, die für die Ausgabe genutzt werden kann.</li>
 * </ul>
 */
public enum LogLevel {
    /**
     * Debug-Informationen für Entwickler
     */
    DEBUG("DEBUG", ColorUtils.COLOR_BLUE),

    /**
     * Standard-Informationen
     */
    INFO("INFO", ColorUtils.COLOR_GREEN),

    /**
     * Warnhinweise
     */
    WARN("WARN", ColorUtils.COLOR_YELLOW),

    /**
     * Fehler
     */
    ERROR("ERROR", ColorUtils.COLOR_RED),

    /**
     * Ausnahmen
     */
    EXCEPTION("EXCEPTION", ColorUtils.COLOR_DARK_RED);

    /**
     * Textdarstellung der Protokollebene.
     */
    private final String text;

    /**
     * Die zugehörige Farbe für die farbige Ausgabe.
     */
    private final Color color;

    /**
     * Erstellt eine neue Protokollebene.
     *
     * @param text  Textdarstellung der Protokollebene
     * @param color Die zugehörige Farbe für die farbige Ausgabe
     */
    LogLevel(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    /**
     * Gibt den Klartext-Namen der Protokollebene zurück.
     *
     * @return Klartext-Name der Protokollebene
     */
    public String getName() {
        return switch (this) {
            case DEBUG -> getWord("logLevel.debug");
            case INFO -> getWord("logLevel.info");
            case WARN -> getWord("logLevel.warn");
            case ERROR -> getWord("logLevel.error");
            case EXCEPTION -> getWord("logLevel.exception");
        };
    }

    /**
     * Gibt die Textdarstellung der Protokollebene zurück.
     *
     * @return Die Textdarstellung der Protokollebene
     */
    public String getText() {
        return text;
    }

    /**
     * Gibt die zugehörige Farbe für die farbige Ausgabe zurück.
     *
     * @return Die Farbe, die für die Ausgabe verwendet werden kann
     */
    public Color getColor() {
        return color;
    }

    /**
     * Berechnet die längste Protokollebene Textdarstellung über alle definierten
     * {@link LogLevel}-Konstanten hinweg.
     *
     * @return Die Länge der längsten Protokollebene Textdarstellung
     */
    public static int maxLevelLength() {
        int maxLevelLength = 0;
        for (LogLevel level : LogLevel.values()) {
            maxLevelLength = Math.max(maxLevelLength, level.toString().length());
        }
        return maxLevelLength;
    }

    /**
     * Liefert alle {@link LogLevel}-Werte alphabetisch sortiert nach ihrer
     * Textdarstellung zurück.
     *
     * <p>
     * Die Sortierung erfolgt anhand des Rückgabewerts von {@link #toString()},
     * also nach den Klartext-Namen der Protokollebenen, unabhängig von
     * Groß- und Kleinschreibung.
     *
     * @return Ein alphabetisch sortiertes Array aller definierten {@link LogLevel}-Konstanten
     */
    public static LogLevel[] sortedValues() {
        LogLevel[] values = LogLevel.values();
        Arrays.sort(values, (logLevel1, level2) -> logLevel1.getName().compareToIgnoreCase(level2.getName()));
        Arrays.sort(values, Comparator.comparing(LogLevel::getName, String.CASE_INSENSITIVE_ORDER));
        return values;
    }

    /**
     * Gibt die Textdarstellung der Protokollebene zurück.
     *
     * @return {@link #getText()}
     */
    @Override
    public String toString() {
        return getText();
    }
}
