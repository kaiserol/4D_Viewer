package de.uzk.logger;

import de.uzk.utils.ColorUtils;

import java.awt.*;

public enum LogLevel {
    DEBUG(ColorUtils.COLOR_BLUE),
    ERROR(ColorUtils.COLOR_RED),
    EXCEPTION(ColorUtils.COLOR_DARK_RED),
    INFO(ColorUtils.COLOR_GREEN),
    WARNING(ColorUtils.COLOR_YELLOW);

    private final Color color;

    LogLevel(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
