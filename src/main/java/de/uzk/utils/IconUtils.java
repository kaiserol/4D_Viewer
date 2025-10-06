package de.uzk.utils;

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
    // my icons
    public static final FlatSVGIcon PREV_IMAGE_ICON = loadResourceSVG("arrow_left.svg");
    public static final FlatSVGIcon NEXT_IMAGE_ICON = loadResourceSVG("arrow_right.svg");
    public static final FlatSVGIcon PREV_LEVEL_ICON = loadResourceSVG("arrow_up.svg");
    public static final FlatSVGIcon NEXT_LEVEL_ICON = loadResourceSVG("arrow_down.svg");
    public static final FlatSVGIcon FIRST_IMAGE_ICON = loadResourceSVG("arrow_left_start.svg");
    public static final FlatSVGIcon LAST_IMAGE_ICON = loadResourceSVG("arrow_right_end.svg");
    public static final FlatSVGIcon FIRST_LEVEL_ICON = loadResourceSVG("arrow_up_start.svg");
    public static final FlatSVGIcon LAST_LEVEL_ICON = loadResourceSVG("arrow_down_end.svg");
    public static final FlatSVGIcon FIRST_IMAGE_LEVEL_ICON = loadResourceSVG("images_start.svg");
    public static final FlatSVGIcon LAST_IMAGE_LEVEL_ICON = loadResourceSVG("images_end.svg");
    public static final FlatSVGIcon TURN_RIGHT_ICON = loadResourceSVG("turn_right.svg");
    public static final FlatSVGIcon TURN_LEFT_ICON = loadResourceSVG("turn_left.svg");
    public static final FlatSVGIcon SCREENSHOT_ICON = loadResourceSVG("screenshot.svg");
    public static final FlatSVGIcon PIN_ICON = loadResourceSVG("pin.svg");
    public static final FlatSVGIcon DELETE_ICON = loadResourceSVG("delete.svg");
    public static final FlatSVGIcon WARNING_ICON = loadResourceSVG("warning.svg");
    public static final Image APP_ICON = readResourcesImage("logo/4D.png");
    private static final FlatSVGIcon[] changeOnThemeIcons = {
            // next, prev
            PREV_IMAGE_ICON,
            NEXT_IMAGE_ICON,
            PREV_LEVEL_ICON,
            NEXT_LEVEL_ICON,
            // first, last
            FIRST_IMAGE_ICON,
            LAST_IMAGE_ICON,
            FIRST_LEVEL_ICON,
            LAST_LEVEL_ICON,
            // both
            FIRST_IMAGE_LEVEL_ICON,
            LAST_IMAGE_LEVEL_ICON,
            // others
            DELETE_ICON,
    };

    private IconUtils() {
    }

    public static BufferedImage loadImage(File file) {
        return loadImage(file, null);
    }

    private static BufferedImage readResourcesImage(String imageName) {
        URL url = IconUtils.class.getClassLoader().getResource(SystemConstants.ICONS_PATH + imageName);
        ImageExceptionListener onImageException = e -> {
            logger.error("The SVG image " + SystemConstants.ICONS_PATH + imageName + " could not be found.");
            logger.logException(e);
        };
        return loadImage(url, onImageException);
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


    private static FlatSVGIcon loadResourceSVG(String svgName) {
        try {
            URL svgUrl = IconUtils.class.getClassLoader().getResource(SystemConstants.ICONS_PATH + svgName);
            if (svgUrl != null) {
                InputStream svgStream = svgUrl.openStream();
                return new FlatSVGIcon(svgStream);
            } else {
                // Die Datei wurde nicht gefunden
                logger.error("The SVG image " + SystemConstants.ICONS_PATH + svgName + " could not be found.");
            }
        } catch (IOException e) {
            logger.logException(e);
        }
        return null;
    }

    public static void updateSVGIcons() {
        FlatSVGIcon.ColorFilter colorFilter = getThemeColorFilter();
        for (FlatSVGIcon svgIcon : changeOnThemeIcons) {
            svgIcon.setColorFilter(colorFilter);
        }
    }

    private static FlatSVGIcon.ColorFilter getThemeColorFilter() {
        return new FlatSVGIcon.ColorFilter(color -> {
            if (color.equals(Color.WHITE)) {
                return config.getTheme().isLight() ? Color.GRAY : Color.WHITE;
            }
            return color;
        });
    }

    public interface ImageExceptionListener {
        void onImageException(Exception e);
    }
}
