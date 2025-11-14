package de.uzk.config;

import com.formdev.flatlaf.util.SystemInfo;

public enum OperatingSystem {
    WINDOWS,
    LINUX,
    MACOS,
    OTHER;

    public boolean isMacOS() {
        return this == MACOS;
    }

    public static OperatingSystem load() {
        return SystemInfo.isWindows ? WINDOWS : SystemInfo.isLinux ? LINUX : SystemInfo.isMacOS ? MACOS : OTHER;
    }
}