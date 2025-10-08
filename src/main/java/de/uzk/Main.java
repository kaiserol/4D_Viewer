package de.uzk;

import de.uzk.gui.Gui;
import de.uzk.config.ConfigHandler;
import de.uzk.image.ImageHandler;
import de.uzk.logger.LogDataHandler;
import de.uzk.markers.MarkerHandler;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static final LogDataHandler logger;
    public static final ConfigHandler config;
    public static final ImageHandler imageHandler;
    public static final MarkerHandler markerHandler;

    static {
        logger = new LogDataHandler(Main.class.getName());
        imageHandler = new ImageHandler();
        config = new ConfigHandler();
        markerHandler = new MarkerHandler();
    }

    public static void main(String[] args) {
        config.loadConfig();
        // create gui
        new Gui();
    }

    public static void closeApplication(Window parentWindow, Window currentWindow, Runnable runForClosing) {
        if (config.isAskAgainClosingWindow() || currentWindow instanceof Dialog) {
            Object[] options = {"Yes", "No"};
            Object[] message;
            JCheckBox checkBox = new JCheckBox("Don't ask me again");
            if (currentWindow instanceof Frame) {
                message = new Object[]{"Do you want to close the app?", checkBox};
            } else {
                message = new Object[]{"Do you want to close the app?"};
            }

            int choice = JOptionPane.showOptionDialog(parentWindow, message, "Confirm",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            } else if (currentWindow instanceof Frame && checkBox.isSelected()) {
                config.setAskAgainClosingWindow(false);
            }
        }
        if (runForClosing != null) runForClosing.run();
        System.exit(0);
    }
}
