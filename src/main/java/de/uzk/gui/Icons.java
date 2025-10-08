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
    public static final FlatSVGIcon ICON_PREV_IMAGE = loadResourceSVG("images/icons_nav/arrow_left.svg");
    public static final FlatSVGIcon ICON_NEXT_IMAGE = loadResourceSVG("images/icons_nav/arrow_right.svg");
    public static final FlatSVGIcon ICON_FIRST_IMAGE = loadResourceSVG("images/icons_nav/arrow_left_start.svg");
    public static final FlatSVGIcon ICON_LAST_IMAGE = loadResourceSVG("images/icons_nav/arrow_right_end.svg");

    public static final FlatSVGIcon ICON_FIRST_LEVEL = loadResourceSVG("images/icons_nav/arrow_up_start.svg");
    public static final FlatSVGIcon ICON_PREV_LEVEL = loadResourceSVG("images/icons_nav/arrow_up.svg");
    public static final FlatSVGIcon ICON_NEXT_LEVEL = loadResourceSVG("images/icons_nav/arrow_down.svg");
    public static final FlatSVGIcon ICON_LAST_LEVEL = loadResourceSVG("images/icons_nav/arrow_down_end.svg");

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
            ICON_FIRST_IMAGE,
            ICON_PREV_IMAGE,
            ICON_NEXT_IMAGE,
            ICON_LAST_IMAGE,

            ICON_FIRST_LEVEL,
            ICON_PREV_LEVEL,
            ICON_NEXT_LEVEL,
            ICON_LAST_LEVEL,

            // icons
            ICON_DELETE,
    };

    public static final Image APP_IMAGE = readResourcesImage("images/4D.png");

    private Icons() {
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
        String imageNameCleanedFileSeps = imageName.replace("/", StringUtils.FILE_SEP);
        URL url = Icons.class.getClassLoader().getResource(imageNameCleanedFileSeps);
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
            String svgNameCleanedFileSeps = svgName.replace("/", StringUtils.FILE_SEP);
            URL svgUrl = Icons.class.getClassLoader().getResource(svgNameCleanedFileSeps);
            if (svgUrl != null) {
                InputStream svgStream = svgUrl.openStream();
                return new FlatSVGIcon(svgStream);
            } else {
                // Die Datei wurde nicht gefunden
                logger.error("The SVG image '" + svgNameCleanedFileSeps + "' could not be found.");
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

    private interface ImageExceptionListener {
        void onImageException(Exception e);
    }
}
