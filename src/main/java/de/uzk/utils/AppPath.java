package de.uzk.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.logger;

// TODO: Paths Update, alle Pfade sollen hier abgespeichert werden, die im Code verwendet werden
public class AppPath {
    public static final Path USER_WORKING_DIRECTORY = Path.of(System.getProperty("user.dir"));
    public static final Path USER_HOME_DIRECTORY = Path.of(System.getProperty("user.home"));
    public static final Path VIEWER_HOME_DIRECTORY = USER_HOME_DIRECTORY.resolve(".4D_Viewer");

    // Weitere Unterpfade von "4D_Viewer Verzeichnis"

    static {
        if (!Files.exists(VIEWER_HOME_DIRECTORY)) {
            try {
                Files.createDirectories(VIEWER_HOME_DIRECTORY);
            } catch (IOException e) {
                logger.logException(e);
            }
        }
    }
}
