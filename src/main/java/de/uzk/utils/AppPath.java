package de.uzk.utils;

import java.nio.file.Path;

public class AppPath {
    public static final Path USER_WORKING_DIRECTORY = Path.of(System.getProperty("user.dir"));
    public static final Path USER_HOME_DIRECTORY = Path.of(System.getProperty("user.home"));
    public static final Path VIEWER_HOME_DIRECTORY = USER_HOME_DIRECTORY.resolve(".4DViewer");
}
