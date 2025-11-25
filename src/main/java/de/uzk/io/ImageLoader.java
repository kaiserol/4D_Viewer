package de.uzk.io;

import com.formdev.flatlaf.extras.FlatSVGIcon;
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
    // App Image
    public static final Image APP_IMAGE = Objects.requireNonNull(openSvgFromResources("images/4D.svg")).getImage();

    // Bearbeiten Icons
    public static final FlatSVGIcon ICON_EDIT = openSvgFromResources("images/icons/edit.svg");
    public static final FlatSVGIcon ICON_PIN = openSvgFromResources("images/icons/pin.svg");
    public static final FlatSVGIcon ICON_ARROW_LEFT_TURN = openSvgFromResources("images/icons/arrow_left_turn.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT_TURN = openSvgFromResources("images/icons/arrow_right_turn.svg");
    public static final FlatSVGIcon ICON_DELETE = openSvgFromResources("images/icons/delete.svg");

    // Navigieren Icons
    public static final FlatSVGIcon ICON_ARROW_LEFT_START = openSvgFromResources("images/icons/arrow_left_start.svg");
    public static final FlatSVGIcon ICON_ARROW_LEFT = openSvgFromResources("images/icons/arrow_left.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT = openSvgFromResources("images/icons/arrow_right.svg");
    public static final FlatSVGIcon ICON_ARROW_RIGHT_END = openSvgFromResources("images/icons/arrow_right_end.svg");

    public static final FlatSVGIcon ICON_ARROW_UP_START = openSvgFromResources("images/icons/arrow_up_start.svg");
    public static final FlatSVGIcon ICON_ARROW_UP = openSvgFromResources("images/icons/arrow_up.svg");
    public static final FlatSVGIcon ICON_ARROW_DOWN = openSvgFromResources("images/icons/arrow_down.svg");
    public static final FlatSVGIcon ICON_ARROW_DOWN_END = openSvgFromResources("images/icons/arrow_down_end.svg");

    /**
     * Icons, deren Farbe in der Methode {@link #updateSVGIcons} geändert wird.
     */
    private static final Map<FlatSVGIcon, Color> COLORED_ICONS = new HashMap<>();

    /**
     * Icons, deren Farbe in der Methode {@link #updateSVGIcons} geändert wird.
     * Die Farbauswahl wird auf Basis des Themes festgelegt.
     */
    private static final FlatSVGIcon[] ICONS_COLOR_ON_THEME_SWITCH = {
        // Bearbeiten Icons
        ICON_ARROW_LEFT_TURN,
        ICON_ARROW_RIGHT_TURN,

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

    // Statische Initialisierung
    static {
        COLORED_ICONS.put(ICON_PIN, ColorUtils.COLOR_BLUE);
        COLORED_ICONS.put(ICON_EDIT, ColorUtils.COLOR_BLUE);
        COLORED_ICONS.put(ICON_DELETE, Color.RED);
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

    private static FlatSVGIcon openSvgFromResources(String relativePath) {
        String relativePathCleaned = relativePath.replace("/", StringUtils.FILE_SEP);
        URL svgUrl = ImageLoader.class.getClassLoader().getResource(relativePathCleaned);

        try {
            if (svgUrl == null) throw new IOException();
            InputStream svgStream = svgUrl.openStream();
            return new FlatSVGIcon(svgStream);
        } catch (IOException e) {
            logger.warn("Could not open the svg-file '%s' in the resources-directory.".formatted(relativePathCleaned));
            return null;
        }
    }

    public static void updateSVGIcons() {
        for (Map.Entry<FlatSVGIcon, Color> entry : COLORED_ICONS.entrySet()) {
            Color newColor = entry.getValue();
            // Tausche Farben aus
            updateSVGIconsColor(entry.getKey(), new FlatSVGIcon.ColorFilter(color -> {
                if (Objects.equals(color, Color.BLACK)) return newColor;
                else return color;
            }));
        }

        for (FlatSVGIcon svgIcon : ICONS_COLOR_ON_THEME_SWITCH) {
            Color newColor = settings.getTheme().isLightMode() ? Color.GRAY : Color.WHITE;
            // Tausche Farben aus
            updateSVGIconsColor(svgIcon, new FlatSVGIcon.ColorFilter(color -> {
                if (Objects.equals(color, Color.BLACK)) return newColor;
                return color;
            }));
        }
    }

    public static void updateSVGIconsColor(FlatSVGIcon svgIcon, FlatSVGIcon.ColorFilter colorFilter) {
        if (svgIcon == null) return;
        svgIcon.setColorFilter(colorFilter);
    }
}