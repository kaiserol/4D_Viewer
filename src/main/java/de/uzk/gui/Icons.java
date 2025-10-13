package de.uzk.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import de.uzk.utils.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static de.uzk.Main.config;
import static de.uzk.Main.logger;

public final class Icons {
    // edit icons
    public static final FlatSVGIcon ICON_PIN = loadResourceSVG("images/icons_edit/pin.svg");
    public static final FlatSVGIcon ICON_TURN_RIGHT = loadResourceSVG("images/icons_edit/turn_right.svg");
    public static final FlatSVGIcon ICON_TURN_LEFT = loadResourceSVG("images/icons_edit/turn_left.svg");
    public static final FlatSVGIcon ICON_SCREENSHOT = loadResourceSVG("images/icons_edit/screenshot.svg");

    // nav icons
    public static final FlatSVGIcon ICON_ARROW_LEFT_START = loadResourceSVG("images/icons_nav/arrow_left_start.svg");
    public static final FlatSVGIcon ICON_ARROW_LEFT = loadResourceSVG("images/icons_nav/arrow_left.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT = loadResourceSVG("images/icons_nav/arrow_right.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT_END = loadResourceSVG("images/icons_nav/arrow_right_end.svg");

    public static final FlatSVGIcon ICON_ARROW_UP_START = loadResourceSVG("images/icons_nav/arrow_up_start.svg");
    public static final FlatSVGIcon ICON_ARROW_UP = loadResourceSVG("images/icons_nav/arrow_up.svg");
    public static final FlatSVGIcon ICON_ARROW_DOWN = loadResourceSVG("images/icons_nav/arrow_down.svg");
    public static final FlatSVGIcon ICON_ARROW_DOWN_END = loadResourceSVG("images/icons_nav/arrow_down_end.svg");

    // icons
    public static final FlatSVGIcon ICON_DELETE = loadResourceSVG("images/icons/delete.svg");

    private static final FlatSVGIcon[] ICONS_ONLY_ONE_COLOR = {
            ICON_PIN,
            ICON_TURN_RIGHT,
            ICON_TURN_LEFT,
            ICON_SCREENSHOT,
    };
    private static final FlatSVGIcon[] ICONS_DIFFERENT_COLORS = {
            // nav icons
            ICON_ARROW_LEFT_START,
            ICON_ARROW_LEFT,
            ICON_ARROW_RIGHT,
            ICON_ARROW_RIGHT_END,

            ICON_ARROW_UP_START,
            ICON_ARROW_UP,
            ICON_ARROW_DOWN,
            ICON_ARROW_DOWN_END,

            // icons
            ICON_DELETE,
    };

    public static final Image APP_IMAGE = loadResourceAppImage();

    private Icons() {
    }

    public static BufferedImage loadImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            logger.error("The image '" + file.getAbsolutePath() + "' could not be loaded.");
            return null;
        }
    }

    private static FlatSVGIcon loadResourceSVG(String svgFilePath) {
        String svgNameCleanedFileSeps = svgFilePath.replace("/", StringUtils.FILE_SEP);
        URL svgUrl = Icons.class.getClassLoader().getResource(svgNameCleanedFileSeps);

        try {
            if (svgUrl == null) throw new IOException();
            InputStream svgStream = svgUrl.openStream();
            return new FlatSVGIcon(svgStream);
        } catch (IOException e) {
            logger.error("The SVG image '" + svgNameCleanedFileSeps + "' could not be loaded.");
            return null;
        }
    }

    private static BufferedImage loadResourceAppImage() {
        String imageNameCleanedFileSeps = "images/4D.png".replace("/", StringUtils.FILE_SEP);
        URL imageUrl = Icons.class.getClassLoader().getResource(imageNameCleanedFileSeps);

        try {
            if (imageUrl == null) throw new IOException();
            return ImageIO.read(imageUrl);
        } catch (IOException e) {
            logger.error("The image '" + imageNameCleanedFileSeps + "' could not be loaded.");
            return null;
        }
    }

    public static void updateSVGIcons() {
        for (FlatSVGIcon svgIcon : ICONS_ONLY_ONE_COLOR) {
            // Tausche Farben aus
            updateSVGIconsColor(svgIcon, new FlatSVGIcon.ColorFilter(color -> {
                if (color.equals(Color.BLACK)) return GuiUtils.COLOR_BLUE;
                else return color;
            }));
        }

        for (FlatSVGIcon svgIcon : ICONS_DIFFERENT_COLORS) {
            // Tausche Farben aus
            updateSVGIconsColor(svgIcon, new FlatSVGIcon.ColorFilter(color -> {
                if (color.equals(Color.BLACK)) return config.getTheme().isLight() ? Color.GRAY : Color.WHITE;
                return color;
            }));
        }
    }

    // TODO: für menubar anpassen
    public static void updateSVGIconsColor(FlatSVGIcon svgIcon, FlatSVGIcon.ColorFilter colorFilter) {
        if (svgIcon == null) return;
        svgIcon.setColorFilter(colorFilter);
    }
}