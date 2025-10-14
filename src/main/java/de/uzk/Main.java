package de.uzk;

import de.uzk.config.ConfigHandler;
import de.uzk.gui.Gui;
import de.uzk.image.ImageHandler;
import de.uzk.logger.LogEntryHandler;
import de.uzk.markers.MarkerHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;

public class Main {
    public static final LogEntryHandler logger;
    public static final ConfigHandler config;
    public static final ImageHandler imageHandler;
    public static final MarkerHandler markerHandler;

    static {
        logger = new LogEntryHandler(Main.class.getName());
        imageHandler = new ImageHandler();
        config = new ConfigHandler();
        markerHandler = new MarkerHandler();
    }

    public static void main(String[] args) {
        config.loadConfig();

        SwingUtilities.invokeLater(() -> {
            Gui gui = new Gui();

            // Behandle den Shortcut: Cmd+Q (unter macOS)
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.setQuitHandler((QuitEvent e, QuitResponse response) -> {
                    response.cancelQuit();
                    gui.confirmExitApp();
                });
            }
        });
    }
}
