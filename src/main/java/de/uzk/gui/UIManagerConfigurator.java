package de.uzk.gui;

import de.uzk.utils.ColorUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static de.uzk.Main.settings;

public final class UIManagerConfigurator {

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private UIManagerConfigurator() {
        // Verhindert Instanziierung dieser Klasse
    }

    public static void setupMisc() {
        // Komponente
        UIManager.put("Component.arrowType", UIEnvironment.ARROW_TYPE);
        UIManager.put("Component.hideMnemonics", false);
        UIManager.put("Component.arc", UIEnvironment.ARC_DEFAULT);

        // Icon
        UIManager.put("Dialog.showIcon", true);
        UIManager.put("OptionPane.showIcon", true);

        // Button
        UIManager.put("Button.arc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("Button.margin", UIEnvironment.INSETS_MEDIUM);

        // Checkbox
        UIManager.put("CheckBox.margin", UIEnvironment.INSETS_NONE);

        // Textfeld
        UIManager.put("TextComponent.arc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("TextField.arc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("TextArea.margin", UIEnvironment.INSETS_MEDIUM);
        UIManager.put("TextField.margin", UIEnvironment.INSETS_MEDIUM);
        UIManager.put("TextPane.margin", UIEnvironment.INSETS_MEDIUM);

        // Sonstige Komponenten
        UIManager.put("List.selectionArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("MenuItem.selectionArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("ProgressBar.arc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("TitlePane.buttonArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("ToolBar.hoverButtonGroupArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("Tree.selectionArc", UIEnvironment.ARC_DEFAULT);
    }

    public static void setupComboBox() {
        Color arrowColor = Color.LIGHT_GRAY;
        Color textFieldBg = UIManager.getColor("TextField.background");
        Color defaultButtonBg = UIManager.getColor("Button.default.background");

        UIManager.put("ComboBox.arc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("ComboBox.buttonArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("ComboBox.selectionArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("ComboBox.buttonStyle", "button");
        UIManager.put("ComboBox.padding", UIEnvironment.INSETS_MEDIUM);

        // Pfeilfarben
        UIManager.put("ComboBox.buttonArrowType", UIEnvironment.ARROW_TYPE);
        UIManager.put("ComboBox.buttonArrowColor", ColorUtils.adjustColor(arrowColor, 0.0f, false));
        UIManager.put("ComboBox.buttonHoverArrowColor", ColorUtils.adjustColor(arrowColor, 0.1f, true));
        UIManager.put("ComboBox.buttonPressedArrowColor", ColorUtils.adjustColor(arrowColor, 0.3f, true));

        // Hintergrundfarbe
        UIManager.put("ComboBox.background", textFieldBg);
        UIManager.put("ComboBox.popupBackground", textFieldBg);
        UIManager.put("ComboBox.buttonBackground", defaultButtonBg);
        UIManager.put("ComboBox.buttonSeparatorColor", defaultButtonBg);
        UIManager.put("ComboBox.buttonEditableBackground", defaultButtonBg);
    }

    public static void setupSpinner() {
        Color arrowColor = Color.LIGHT_GRAY;
        Color textFieldBg = UIManager.getColor("TextField.background");
        Color defaultButtonBg = UIManager.getColor("Button.default.background");

        UIManager.put("Spinner.arc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("Spinner.buttonArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("Spinner.selectionArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("Spinner.buttonStyle", "roundRect");
        UIManager.put("Spinner.padding", UIEnvironment.INSETS_MEDIUM);
        UIManager.put("Spinner.editorAlignment", SwingConstants.LEFT);

        // Pfeilfarben
        UIManager.put("Spinner.buttonArrowType", UIEnvironment.ARROW_TYPE);
        UIManager.put("Spinner.buttonArrowColor", ColorUtils.adjustColor(arrowColor, 0.0f, false));
        UIManager.put("Spinner.buttonHoverArrowColor", ColorUtils.adjustColor(arrowColor, 0.1f, true));
        UIManager.put("Spinner.buttonPressedArrowColor", ColorUtils.adjustColor(arrowColor, 0.3f, true));

        // Hintergrundfarbe
        UIManager.put("Spinner.background", textFieldBg);
        UIManager.put("Spinner.buttonBackground", defaultButtonBg);
        UIManager.put("Spinner.buttonSeparatorColor", defaultButtonBg);
    }

    public static void setupTabbedPane() {
        UIManager.put("TabbedPane.buttonArc ", UIEnvironment.ARC_DEFAULT);
        UIManager.put("TabbedPane.closeArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("TabbedPane.tabArc", 0);
        UIManager.put("TabbedPane.tabSelectionArc", 0);
        UIManager.put("TabbedPane.tabSelectionHeight", 2);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.arrowType", UIEnvironment.ARROW_TYPE);

        // Linienfarbe
        UIManager.put("TabbedPane.underlineColor", ColorUtils.COLOR_BLUE);
        UIManager.put("TabbedPane.inactiveUnderlineColor", settings.getTheme().isLightMode() ? Color.GRAY : Color.WHITE);

        // Scroll-Eigenschaften
        UIManager.put("TabbedPane.tabsPopupPolicy", "never");
        UIManager.put("TabbedPane.scrollButtonsPolicy", "asNeeded");
    }

    public static void setupScrollBar() {
        Color trackColor = UIManager.getColor("ScrollBar.background");
        Color thumbColor = ColorUtils.COLOR_BLUE;

        UIManager.put("ScrollBar.trackArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("ScrollBar.thumbArc", UIEnvironment.ARC_DEFAULT);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.trackInsets", UIEnvironment.INSETS_NONE);
        UIManager.put("ScrollBar.thumbInsets", UIEnvironment.BORDER_EMPTY_NONE);
        UIManager.put("ScrollBar.showButtons", true);

        // Hinter- und Vordergrundfarbe
        UIManager.put("ScrollBar.track", trackColor);
        UIManager.put("ScrollBar.hoverTrackColor", trackColor);
        UIManager.put("ScrollBar.thumb", thumbColor);
        UIManager.put("ScrollBar.hoverThumbColor", thumbColor.darker());
        UIManager.put("ScrollBar.pressedThumbColor", thumbColor.darker());
    }

    public static void setupSplitPane() {
        Color arrowColor = UIManager.getColor("Label.foreground");

        UIManager.put("SplitPaneDivider.gripDotCount", 0);
        UIManager.put("SplitPaneDivider.gripDotSize", 3);
        UIManager.put("SplitPaneDivider.gripGap", 3);
        UIManager.put("SplitPane.dividerSize", 10);
        UIManager.put("SplitPane.supportsOneTouchButtons", true);

        // Pfeilfarben
        boolean isLight = settings.getTheme().isLightMode();
        UIManager.put("SplitPaneDivider.oneTouchArrowColor", ColorUtils.adjustColor(arrowColor, 0.3f, isLight));
        UIManager.put("SplitPaneDivider.oneTouchHoverArrowColor", ColorUtils.adjustColor(arrowColor, 0.2f, isLight));
        UIManager.put("SplitPaneDivider.oneTouchPressedArrowColor", ColorUtils.adjustColor(arrowColor, 0.0f, !isLight));
    }

    public static String getAllUIManagerProperties() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== UIManager Properties ===\n");

        // Alle Keys holen und alphabetisch sortieren
        List<Object> keys = new ArrayList<>(UIManager.getDefaults().keySet());
        keys.sort(Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER));

        for (Object key : keys) {
            Object value = UIManager.get(key).toString().replaceAll("\\n", " ");
            sb.append(String.format("%-40s : %s%n", key, value));
        }

        return sb.toString();
    }
}
