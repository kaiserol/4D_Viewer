package de.uzk.config;

import java.io.File;

public final class SystemConstants {
    public static final String FILE_SEP;
    public static final String NEXT_LINE;
    public static final File CONFIG_FILE;
    public static final File SCREENSHOT_FOLDER;

    static {
        FILE_SEP = File.separator;
        NEXT_LINE = System.lineSeparator();
        CONFIG_FILE = new File("config.cfg");
        SCREENSHOT_FOLDER = new File("screenshots");
    }

    private SystemConstants() {
    }
}
