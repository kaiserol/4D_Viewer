package de.uzk.logger;

import de.uzk.utils.ColorUtils;

import java.awt.*;

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
     * Debug-Informationen für Entwickler.
     */
    DEBUG("DEBUG", ColorUtils.COLOR_BLUE),

    /**
     * Standard-Informationen.
     */
    INFO("INFO", ColorUtils.COLOR_GREEN),

    /**
     * Warnungen, die auf potenzielle Probleme hinweisen.
     */
    WARN("WARN", ColorUtils.COLOR_ORANGE),

    /**
     * Fehler, die eine Ausnahme oder Fehlfunktion darstellen.
     */
    ERROR("ERROR", ColorUtils.COLOR_RED);

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
     * Gibt die Textdarstellung der Protokollebene zurück.
     *
     * @return {@link #getText()}
     */
    @Override
    public String toString() {
        return getText();
    }
}
