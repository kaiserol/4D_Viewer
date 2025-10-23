package de.uzk.config;

public enum Theme {
    DARK_MODE, LIGHT_MODE;

    public boolean isLight() {
        return this == LIGHT_MODE;
    }

    public Theme opposite() {
        return isLight() ? DARK_MODE : LIGHT_MODE;
    }

    public static Theme fromName(String name) {
        if (name != null) {
            try {
                for (Theme theme : Theme.values()) {
                    if (theme.name().equalsIgnoreCase(name)) return theme;
                }
            } catch (Exception ignore) {
            }
        }
        // Fallback
        return Theme.LIGHT_MODE;
    }
}