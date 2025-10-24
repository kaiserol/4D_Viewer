package de.uzk.utils;

import com.formdev.flatlaf.util.SystemInfo;

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

    public static OperatingSystem load() {
        return SystemInfo.isWindows ? WINDOWS : SystemInfo.isLinux ? LINUX : SystemInfo.isMacOS ? MACOS : OTHER;
    }
}