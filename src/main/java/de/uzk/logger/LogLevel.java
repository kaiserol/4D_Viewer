package de.uzk.logger;

import de.uzk.gui.GuiUtils;

import java.awt.*;

public enum LogLevel {
    DEBUG(GuiUtils.COLOR_BLUE),
    ERROR(GuiUtils.COLOR_RED),
    INFO(GuiUtils.COLOR_GREEN),
    WARNING(GuiUtils.COLOR_YELLOW);

    private final Color color;

    LogLevel(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
