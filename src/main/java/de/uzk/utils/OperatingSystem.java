package de.uzk.utils;

import com.formdev.flatlaf.util.SystemInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.logger;

public enum OperatingSystem {
    WINDOWS,
    LINUX,
    MACOS,
    OTHER;

    public boolean isWindows() {
        return this == WINDOWS;
    }

    public boolean isLinux() {
        return this == LINUX;
    }

    public boolean isMacOS() {
        return this == MACOS;
    }

    public boolean isOther() {
        return this == OTHER;
    }

    // TODO: Validate method
    // TODO: Alle Paths auslagern: AppPath Klasse (auch ".4D_Viewer")
    public Path getDirectory(boolean isProjectData) {
        Path userHome = Path.of(System.getProperty("user.home"));
        Path directory;
        if (!isProjectData) {
            directory = switch (this) {
                case WINDOWS -> Path.of(System.getenv("LOCALAPPDATA"));
                case LINUX -> {
                    String xdgHome = System.getenv("XDG_DATA_HOME");
                    if (xdgHome != null) yield Path.of(xdgHome);
                    else yield userHome.resolve(".local/share");
                }
                case MACOS -> {
                    String xdgHome = System.getenv("DG_CONFIG_HOME");
                    if (xdgHome != null) yield Path.of(xdgHome);
                    else yield userHome.resolve("Library/Application Support");
                }
                case OTHER -> userHome;
            };
        } else {
            // Für Projektdaten immer im Benutzerverzeichnis
            directory = userHome;
        }

        Path result = directory.resolve(".4D_Viewer");
        if (!Files.exists(result)) {
            try {
                Files.createDirectories(result);
            } catch (IOException e) {
                logger.logException(e);
            }
        }
        return result;
    }

    public static OperatingSystem load() {
        return SystemInfo.isWindows ? WINDOWS : SystemInfo.isLinux ? LINUX : SystemInfo.isMacOS ? MACOS : OTHER;
    }
}