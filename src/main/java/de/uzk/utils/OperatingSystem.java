package de.uzk.utils;

import com.formdev.flatlaf.util.SystemInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static OperatingSystem getOP() {
        return SystemInfo.isWindows ? WINDOWS : SystemInfo.isLinux ? LINUX : SystemInfo.isMacOS ? MACOS : OTHER;
    }

    public Path getDataDirectory() {
        Path base = switch (this) {
            case WINDOWS -> Path.of(System.getenv("LOCALAPPDATA"));
            case LINUX -> {
                String xdgHome =  System.getenv("XDG_DATA_HOME");
                if (xdgHome != null) {
                    yield Path.of(xdgHome);
                } else {
                    yield Path.of(System.getProperty("user.home")).resolve(".local/share");
                }
            }
            case MACOS -> {
                String xdgHome =  System.getenv("XDG_DATA_HOME");
                if (xdgHome != null) {
                    yield Path.of(xdgHome);
                } else {
                    yield Path.of(System.getProperty("user.home")).resolve("Library/Application Support");
                }
            }
            case OTHER -> Path.of(System.getProperty("user.home"));
        };

        Path result = base.resolve("4dviewer");
        if(!Files.exists(result)) {
            try {
                Files.createDirectories(result);
            } catch (IOException e) {
                return Path.of(System.getProperty("user.home"));
            }
        }
        return result;
    }
}