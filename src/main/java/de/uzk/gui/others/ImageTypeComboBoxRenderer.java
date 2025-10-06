package de.uzk.gui.others;

import de.uzk.handler.ImageType;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.config;

public class ImageTypeComboBoxRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DefaultListCellRenderer render = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof ImageType imageType) {
            Color themeColor = config.getTheme().isLight() ? Color.GRAY : Color.LIGHT_GRAY;
            Color color = isSelected ? list.getSelectionForeground() : themeColor;
            render.setText("<html>" + imageType.getTypeDescription() + " <font color=" +
                    StringUtils.colorToHex(color) + ">" + imageType.getFileExtensionsDescription() + "</font></html>");
        }
        return render;
    }
}
