package de.uzk.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import de.uzk.utils.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import static de.uzk.Main.logger;
import static de.uzk.Main.settings;

public final class Icons {
    // Bearbeiten Icons
    public static final FlatSVGIcon ICON_PIN = loadResourceSVG("images/icons/pin.svg");
    public static final FlatSVGIcon ICON_ARROW_LEFT_TURN = loadResourceSVG("images/icons/arrow_left_turn.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT_TURN = loadResourceSVG("images/icons/arrow_right_turn.svg");
    public static final FlatSVGIcon ICON_DELETE = loadResourceSVG("images/icons/delete.svg");

    // Navigieren Icons
    public static final FlatSVGIcon ICON_ARROW_LEFT_START = loadResourceSVG("images/icons/arrow_left_start.svg");
    public static final FlatSVGIcon ICON_ARROW_LEFT = loadResourceSVG("images/icons/arrow_left.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT = loadResourceSVG("images/icons/arrow_right.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT_END = loadResourceSVG("images/icons/arrow_right_end.svg");

    public static final FlatSVGIcon ICON_ARROW_UP_START = loadResourceSVG("images/icons/arrow_up_start.svg");
    public static final FlatSVGIcon ICON_ARROW_UP = loadResourceSVG("images/icons/arrow_up.svg");
    public static final FlatSVGIcon ICON_ARROW_DOWN = loadResourceSVG("images/icons/arrow_down.svg");
    public static final FlatSVGIcon ICON_ARROW_DOWN_END = loadResourceSVG("images/icons/arrow_down_end.svg");

    // App Image
    public static final Image APP_IMAGE = Objects.requireNonNull(loadResourceSVG("images/4D.svg")).getImage();

    // Icons Arrays
    private static final FlatSVGIcon[] ICONS_COLOR_BLUE = {
        // Bearbeiten Icons
        ICON_PIN,
    };
    private static final FlatSVGIcon[] ICONS_COLOR_ON_THEME_SWITCH = {
        // Bearbeiten Icons
        ICON_ARROW_LEFT_TURN,
        ICON_ARROW_RIGHT_TURN,
        ICON_DELETE,

        // Navigieren Icons
        ICON_ARROW_LEFT_START,
        ICON_ARROW_LEFT,
        ICON_ARROW_RIGHT,
        ICON_ARROW_RIGHT_END,

        ICON_ARROW_UP_START,
        ICON_ARROW_UP,
        ICON_ARROW_DOWN,
        ICON_ARROW_DOWN_END,
    };

    private Icons() {
    }

    public static BufferedImage loadImage(Path imagePath, boolean showErrorIfNotFound) {
        if (imagePath != null) {
            try {
                return ImageIO.read(imagePath.toFile());
            } catch (Exception e) {
                if (showErrorIfNotFound) {
                    logger.error(String.format("Failed loading image '%s'", imagePath.toAbsolutePath()));
                }
            }
        }
        return null;
    }

    private static FlatSVGIcon loadResourceSVG(String svgPath) {
        String svgFilePathCleaned = svgPath.replace("/", StringUtils.FILE_SEP);
        URL svgUrl = Icons.class.getClassLoader().getResource(svgFilePathCleaned);

        try {
            if (svgUrl == null) throw new IOException();
            InputStream svgStream = svgUrl.openStream();
            return new FlatSVGIcon(svgStream);
        } catch (IOException e) {
            logger.error(String.format("Failed loading SVG ressource '%s'", svgFilePathCleaned));
            return null;
        }
    }

    public static void updateSVGIcons() {
        for (FlatSVGIcon svgIcon : ICONS_COLOR_BLUE) {
            // Tausche Farben aus
            updateSVGIconsColor(svgIcon, new FlatSVGIcon.ColorFilter(color -> {
                if (Objects.equals(color, Color.BLACK)) return GuiUtils.COLOR_BLUE;
                else return color;
            }));
        }

        for (FlatSVGIcon svgIcon : ICONS_COLOR_ON_THEME_SWITCH) {
            // Tausche Farben aus
            updateSVGIconsColor(svgIcon, new FlatSVGIcon.ColorFilter(color -> {
                if (Objects.equals(color, Color.BLACK)) return settings.getTheme().isLight() ? Color.GRAY : Color.WHITE;
                return color;
            }));
        }
    }

    public static void updateSVGIconsColor(FlatSVGIcon svgIcon, FlatSVGIcon.ColorFilter colorFilter) {
        if (svgIcon == null) return;
        svgIcon.setColorFilter(colorFilter);
    }
}