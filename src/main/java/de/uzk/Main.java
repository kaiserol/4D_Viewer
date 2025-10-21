package de.uzk;

import de.uzk.config.ConfigHandler;
import de.uzk.gui.Gui;
import de.uzk.image.ImageFileHandler;
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
    public static final ConfigHandler configHandler;
    public static final ImageFileHandler imageFileHandler;
    public static final MarkerHandler markerHandler;

    static {
        logger = new LogEntryHandler(Main.class.getName());
        operationSystem = OperatingSystem.getOP();
        imageFileHandler = new ImageFileHandler();
        configHandler = new ConfigHandler();
        markerHandler = new MarkerHandler();
    }

    public static void main(String[] args) {
        String imageFilesDirectory = configHandler.loadConfig();

        SwingUtilities.invokeLater(() -> {
            Gui gui = new Gui(imageFilesDirectory);

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
