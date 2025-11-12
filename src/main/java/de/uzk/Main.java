package de.uzk;

import de.uzk.config.History;
import de.uzk.config.Settings;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.image.Workspace;
import de.uzk.logger.LogEntryHandler;
import de.uzk.utils.OperatingSystem;

import javax.swing.*;

/*
 * Copyright © 2025 Oliver Kaiser
 *
 * Lizenziert unter der Apache License, Version 2.0 (die "Lizenz");
 * Sie dürfen diese Datei nur im Einklang mit der Lizenz verwenden.
 * Eine Kopie der Lizenz erhalten Sie unter:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Sofern nicht gesetzlich vorgeschrieben oder schriftlich vereinbart,
 * wird die Software unter dieser Lizenz "wie besehen" bereitgestellt,
 * ohne jegliche ausdrückliche oder stillschweigende Gewährleistung.
 * Einzelheiten finden Sie in der Lizenz.
 */
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
        // Systemeigenschaften initialisieren
        GuiUtils.initSystemProperties();

        // Gui erstellen und anzeigen
        SwingUtilities.invokeLater(() -> {
            Gui gui = new Gui();
            GuiUtils.initDesktopHandlers(gui);
        });
    }
}
