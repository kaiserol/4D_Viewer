package de.uzk.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static de.uzk.Main.config;
import static de.uzk.Main.logger;

public final class IconUtils {
    // edit icons
    public static final FlatSVGIcon PIN_ICON = loadResourceSVG("images/icons_edit/pin.svg");
    public static final FlatSVGIcon TURN_RIGHT_ICON = loadResourceSVG("images/icons_edit/turn_right.svg");
    public static final FlatSVGIcon TURN_LEFT_ICON = loadResourceSVG("images/icons_edit/turn_left.svg");
    public static final FlatSVGIcon SCREENSHOT_ICON = loadResourceSVG("images/icons_edit/screenshot.svg");

    // nav icons
    public static final FlatSVGIcon PREV_IMAGE_ICON = loadResourceSVG("images/icons_nav/arrow_left.svg");
    public static final FlatSVGIcon NEXT_IMAGE_ICON = loadResourceSVG("images/icons_nav/arrow_right.svg");
    public static final FlatSVGIcon FIRST_IMAGE_ICON = loadResourceSVG("images/icons_nav/arrow_left_start.svg");
    public static final FlatSVGIcon LAST_IMAGE_ICON = loadResourceSVG("images/icons_nav/arrow_right_end.svg");

    public static final FlatSVGIcon FIRST_LEVEL_ICON = loadResourceSVG("images/icons_nav/arrow_up_start.svg");
    public static final FlatSVGIcon PREV_LEVEL_ICON = loadResourceSVG("images/icons_nav/arrow_up.svg");
    public static final FlatSVGIcon NEXT_LEVEL_ICON = loadResourceSVG("images/icons_nav/arrow_down.svg");
    public static final FlatSVGIcon LAST_LEVEL_ICON = loadResourceSVG("images/icons_nav/arrow_down_end.svg");

    // option icons
    public static final FlatSVGIcon DELETE_ICON = loadResourceSVG("images/icons/delete.svg");

    private static final FlatSVGIcon[] ICONS_ONLY_ONE_COLOR = {
            PIN_ICON,
            TURN_RIGHT_ICON,
            TURN_LEFT_ICON,
            SCREENSHOT_ICON,
    };
    private static final FlatSVGIcon[] ICONS_DIFFERENT_COLORS = {
            // time layer
            FIRST_IMAGE_ICON,
            PREV_IMAGE_ICON,
            NEXT_IMAGE_ICON,
            LAST_IMAGE_ICON,

            // level layer
            FIRST_LEVEL_ICON,
            PREV_LEVEL_ICON,
            NEXT_LEVEL_ICON,
            LAST_LEVEL_ICON,

            // options
            DELETE_ICON,
    };

    public static final Image APP_IMAGE = readResourcesImage("images/4D.png");

    private IconUtils() {
    }

    private static BufferedImage loadImage(Object image, ImageExceptionListener onImageException) {
        try {
            if (image instanceof File file) {
                return ImageIO.read(file);
            } else if (image instanceof URL url) {
                return ImageIO.read(url);
            }
        } catch (Exception e) {
            if (onImageException != null) onImageException.onImageException(e);
        }
        return null;
    }

    private static BufferedImage readResourcesImage(String imageName) {
        URL url = IconUtils.class.getClassLoader().getResource(imageName);
        ImageExceptionListener onImageException = e -> {
            logger.error("The SVG image '" + imageName + "' could not be found.");
            logger.logException(e);
        };
        return loadImage(url, onImageException);
    }

    public static BufferedImage loadImage(File file) {
        return loadImage(file, null);
    }

    private static FlatSVGIcon loadResourceSVG(String svgName) {
        try {
            URL svgUrl = IconUtils.class.getClassLoader().getResource(svgName);
            if (svgUrl != null) {
                InputStream svgStream = svgUrl.openStream();
                return new FlatSVGIcon(svgStream);
            } else {
                // Die Datei wurde nicht gefunden
                logger.error("The SVG image '" + svgName + "' could not be found.");
            }
        } catch (IOException e) {
            logger.logException(e);
        }
        return null;
    }

    public static void updateSVGIcons() {
        for (FlatSVGIcon svgIcon : ICONS_ONLY_ONE_COLOR) {
            FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter(color -> {
                // Replace black color on the theme
                if (color.equals(Color.BLACK)) return new Color(0, 122, 255);
                else return color;
            });
            if (svgIcon != null) svgIcon.setColorFilter(colorFilter);
        }

        for (FlatSVGIcon svgIcon : ICONS_DIFFERENT_COLORS) {
            FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter(color -> {
                // Replace black color on the theme
                if (color.equals(Color.BLACK)) {
                    return config.getTheme().isLight() ? Color.GRAY : Color.WHITE;
                }
                return color;
            });
            if (svgIcon != null) svgIcon.setColorFilter(colorFilter);
        }
    }

    public interface ImageExceptionListener {
        void onImageException(Exception e);
    }
}
