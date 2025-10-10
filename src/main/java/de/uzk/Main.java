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

import static de.uzk.config.LanguageHandler.getWord;

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
        Gui gui = new Gui();

        // catch: macOS Cmd+Q
        if (Desktop.isDesktopSupported()) {
            // Option: Handle Quit-Request
            Desktop desktop = Desktop.getDesktop();
            desktop.setQuitHandler((QuitEvent e, QuitResponse response) -> {
                response.cancelQuit();
                SwingUtilities.invokeLater(() -> closeApp(gui.getFrame(), config::saveConfig));
            });
        }
    }

    public static void closeApp(Window window, Runnable runForClosing) {
        if (window instanceof JDialog || config.isAskAgainClosingWindow()) {
            boolean checkBoxAllowed = config.isAskAgainClosingWindow();
            JCheckBox checkBox = new JCheckBox(getWord("optionPane.closeApp.dont_ask_again"));
            Object[] message = new Object[]{getWord("optionPane.closeApp.question"), checkBoxAllowed ? checkBox : null};

            int option = JOptionPane.showConfirmDialog(
                    window, message, getWord("optionPane.title.confirm"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
            if (option != JOptionPane.YES_OPTION) return;
            else if (checkBox.isSelected()) config.setAskAgainClosingWindow(false);
        }
        if (runForClosing != null) runForClosing.run();
        System.exit(0);
    }
}
