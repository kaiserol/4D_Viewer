package de.uzk;

import de.uzk.config.History;
import de.uzk.config.Settings;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.image.Workspace;
import de.uzk.logger.LogEntryHandler;
import de.uzk.utils.OperatingSystem;

import javax.swing.*;

public class Main {
    public static final LogEntryHandler logger;
    public static final OperatingSystem operationSystem;
    public static final Settings settings;
    public static final History history;
    public static final Workspace workspace;

    static {
        logger = new LogEntryHandler(Main.class.getName());
        operationSystem = OperatingSystem.load();
        settings = Settings.load();
        history = History.load();
        workspace = new Workspace();
    }

    public static void main(String[] args) {
        // Systemeigenschaften aktualisieren
        GuiUtils.setSystemProperties();

        // Gui erstellen und anzeigen
        SwingUtilities.invokeLater(() -> {
            Gui gui = new Gui();
            GuiUtils.initMacOS(gui);
        });
    }
}
