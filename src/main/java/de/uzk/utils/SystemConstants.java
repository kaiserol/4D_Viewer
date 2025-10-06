package de.uzk.utils;

import java.io.File;

public final class SystemConstants {
    public static final String FILE_SEP;
    public static final String NEXT_LINE;
    public static final File CONFIG_FILE;
    public static final File DOWNLOAD_FOLDER;
    public static final File SCREENSHOT_FOLDER;
    public static final String ICONS_PATH;

    static {
        FILE_SEP = File.separator;
        NEXT_LINE = System.lineSeparator();
        CONFIG_FILE = new File("config.cfg");
        DOWNLOAD_FOLDER = new File(System.getProperty("user.home") + FILE_SEP + "Downloads");
        SCREENSHOT_FOLDER = new File("screenshots");
        ICONS_PATH = "icons/";
    }

    private SystemConstants() {
    }
}
