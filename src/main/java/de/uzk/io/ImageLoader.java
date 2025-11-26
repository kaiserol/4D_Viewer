package de.uzk.io;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import de.uzk.config.ThemeColor;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static de.uzk.Main.logger;
import static de.uzk.Main.settings;

public final class ImageLoader {
    // App Icon
    public static final Image APP_IMAGE = openAppImage();

    // Bearbeiten Icons
    public static final FlatSVGIcon ICON_EDIT = openFlatSVGIcon("images/icons/edit.svg");
    public static final FlatSVGIcon ICON_PIN = openFlatSVGIcon("images/icons/pin.svg");
    public static final FlatSVGIcon ICON_ARROW_LEFT_TURN = openFlatSVGIcon("images/icons/arrow_left_turn.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT_TURN = openFlatSVGIcon("images/icons/arrow_right_turn.svg");
    public static final FlatSVGIcon ICON_DELETE = openFlatSVGIcon("images/icons/delete.svg");

    // Navigieren Icons
    public static final FlatSVGIcon ICON_ARROW_LEFT_START = openFlatSVGIcon("images/icons/arrow_left_start.svg");
    public static final FlatSVGIcon ICON_ARROW_LEFT = openFlatSVGIcon("images/icons/arrow_left.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT = openFlatSVGIcon("images/icons/arrow_right.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT_END = openFlatSVGIcon("images/icons/arrow_right_end.svg");

    public static final FlatSVGIcon ICON_ARROW_UP_START = openFlatSVGIcon("images/icons/arrow_up_start.svg");
    public static final FlatSVGIcon ICON_ARROW_UP = openFlatSVGIcon("images/icons/arrow_up.svg");
    public static final FlatSVGIcon ICON_ARROW_DOWN = openFlatSVGIcon("images/icons/arrow_down.svg");
    public static final FlatSVGIcon ICON_ARROW_DOWN_END = openFlatSVGIcon("images/icons/arrow_down_end.svg");

    private static final Map<FlatSVGIcon, ThemeColor> THEME_COLORS = new HashMap<>();
    private static final int ICON_SIZE = 16;

    // Statische Initialisierung
    static {
        ThemeColor defaultThemeColor = new ThemeColor(Color.WHITE, Color.GRAY);

        // Bearbeiten Icons
        THEME_COLORS.put(ICON_PIN, new ThemeColor(ColorUtils.COLOR_BLUE));
        THEME_COLORS.put(ICON_ARROW_LEFT_TURN, defaultThemeColor);
        THEME_COLORS.put(ICON_ARROW_RIGHT_TURN, defaultThemeColor);
        THEME_COLORS.put(ICON_EDIT, new ThemeColor(Color.WHITE, Color.DARK_GRAY));
        THEME_COLORS.put(ICON_DELETE, null);

        // Navigieren Icons
        THEME_COLORS.put(ICON_ARROW_LEFT_START, defaultThemeColor);
        THEME_COLORS.put(ICON_ARROW_LEFT, defaultThemeColor);
        THEME_COLORS.put(ICON_ARROW_RIGHT, defaultThemeColor);
        THEME_COLORS.put(ICON_ARROW_RIGHT_END, defaultThemeColor);

        THEME_COLORS.put(ICON_ARROW_UP_START, defaultThemeColor);
        THEME_COLORS.put(ICON_ARROW_UP, defaultThemeColor);
        THEME_COLORS.put(ICON_ARROW_DOWN, defaultThemeColor);
        THEME_COLORS.put(ICON_ARROW_DOWN_END, defaultThemeColor);
    }

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private ImageLoader() {
        // Verhindert die Instanziierung dieser Klasse
    }

    public static BufferedImage openImage(Path imagePath, boolean showErrorIfNotFound) {
        if (imagePath != null) {
            try {
                return ImageIO.read(imagePath.toFile());
            } catch (Exception e) {
                if (showErrorIfNotFound) {
                    logger.warn("Could not open the image-file '%s'.".formatted(imagePath.toAbsolutePath()));
                }
            }
        }
        return null;
    }

    private static Image openAppImage() {
        FlatSVGIcon svgIcon = openFlatSVGIcon("images/4D.svg");
        return svgIcon == null ? null : svgIcon.getImage();
    }

    private static FlatSVGIcon openFlatSVGIcon(String resourcesPath) {
        String resourcesPathCleaned = resourcesPath.replace("/", StringUtils.FILE_SEP);
        URL svgUrl = ImageLoader.class.getClassLoader().getResource(resourcesPathCleaned);

        try {
            if (svgUrl == null) throw new IOException();
            InputStream svgStream = svgUrl.openStream();
            return new FlatSVGIcon(svgStream).derive(ICON_SIZE, ICON_SIZE);
        } catch (IOException e) {
            logger.warn("Could not open the svg-file '%s' in the resources-directory.".formatted(resourcesPathCleaned));
            return null;
        }
    }

    public static void updateIconThemeColors() {
        for (Map.Entry<FlatSVGIcon, ThemeColor> entry : THEME_COLORS.entrySet()) {
            FlatSVGIcon svgIcon = entry.getKey();
            ThemeColor themeColor = entry.getValue();
            if (svgIcon == null || themeColor == null) continue;

            // Tausche Farben aus
            Color svgColor = themeColor.getThemeColor(!settings.getTheme().isLightMode());
            svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> {
                if (Objects.equals(color, Color.BLACK)) return svgColor;
                else return color;
            }));
        }
    }
}