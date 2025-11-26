package de.uzk.config;

import java.awt.*;

public class ThemeColor {
    private Color light;
    private Color dark;

    public ThemeColor(Color light, Color dark) {
        setLight(light);
        setDark(dark);
    }

    public ThemeColor(Color defaultColor) {
        this(defaultColor, defaultColor);
    }

    private void setLight(Color light) {
        this.light = light;
    }

    private void setDark(Color dark) {
        this.dark = dark;
    }

    public final Color light() {
        return light;
    }

    public final Color dark() {
        return dark;
    }

    public final Color getThemeColor(boolean light) {
        return light ? light() : dark();
    }
}