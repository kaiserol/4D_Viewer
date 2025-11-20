package de.uzk;

import de.uzk.config.History;
import de.uzk.config.OperatingSystem;
import de.uzk.config.Settings;
import de.uzk.gui.Gui;
import de.uzk.gui.UIEnvironment;
import de.uzk.image.Workspace;
import de.uzk.logger.Logger;

import javax.swing.*;

/**
 * <b>Copyright © 2025 Universität zu Köln</b>
 * <p>
 * Lizenziert unter der Apache License, Version 2.0 (die "Lizenz");
 * Sie dürfen diese Datei nur im Einklang mit der Lizenz verwenden.
 * Eine Kopie der Lizenz erhalten Sie unter:
 * <br>
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * <br><br>
 * Sofern nicht gesetzlich vorgeschrieben oder schriftlich vereinbart,
 * wird die Software unter dieser Lizenz "wie besehen" bereitgestellt,
 * ohne jegliche ausdrückliche oder stillschweigende Gewährleistung.
 * Einzelheiten finden Sie in der Lizenz.
 */
public class Main {
    public static final Logger logger;
    public static final OperatingSystem operationSystem;
    public static final Settings settings;
    public static final History history;
    public static final Workspace workspace;

    // Statische Initialisierung
    static {
        logger = new Logger();
        operationSystem = OperatingSystem.load();
        settings = Settings.load();
        history = History.load();
        workspace = new Workspace();
    }

    /**
     * Hauptmethode
     */
    public static void main(String[] args) {
        // Platform Eigenschaften initialisieren
        UIEnvironment.initPlatformProperties();

        // Gui erstellen und anzeigen
        SwingUtilities.invokeLater(() -> {
            Gui gui = new Gui();
            UIEnvironment.initDesktopIntegration(gui);
        });
    }
}
