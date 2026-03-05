package de.uzk.config;

import com.formdev.flatlaf.util.SystemInfo;

public enum OperatingSystem {
    WINDOWS,
    LINUX,
    MACOS,
    OTHER;

    private static OperatingSystem os;

    public static OperatingSystem get() {
        if(os == null) {
            os  =SystemInfo.isWindows ? WINDOWS : SystemInfo.isLinux ? LINUX : SystemInfo.isMacOS ? MACOS : OTHER;

        }
        return os;
    }

    public boolean isMacOS() {
        return this == MACOS;
    }
}