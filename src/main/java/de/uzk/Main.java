package de.uzk;

import de.uzk.config.Settings;
import de.uzk.gui.Gui;
import de.uzk.image.Workspace;
import de.uzk.logger.LogEntryHandler;
import de.uzk.markers.MarkerHandler;
import de.uzk.utils.OperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;

public class Main {


    public static final LogEntryHandler logger;
    public static final OperatingSystem operationSystem;
    public static Workspace workspace;
    public static final MarkerHandler markerHandler;
    public static final Settings settings;

    static {

        logger = new LogEntryHandler(Main.class.getName());
        operationSystem = OperatingSystem.getOP();
        markerHandler = new MarkerHandler();
        settings = Settings.load();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Gui gui = new Gui(Settings.load().getLastHistory());

            // Behandle den Shortcut: Cmd+Q (unter macOS)
            if (operationSystem.isMacOS() && Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.setQuitHandler((QuitEvent e, QuitResponse response) -> {
                    response.cancelQuit();
                    gui.confirmExitApp();
                });
            }
        });
    }
}
