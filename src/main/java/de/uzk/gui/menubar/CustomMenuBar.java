package de.uzk.gui.menubar;

import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class CustomMenuBar {
    private final JMenuBar menuBar;
    private final String name;
    private final List<CustomMenu> menus;

    public CustomMenuBar(JMenuBar menuBar, String name) {
        if (menuBar == null) throw new NullPointerException("MenuBar is null.");
        this.menuBar = menuBar;
        this.name = name;
        this.menus = new ArrayList<>();
    }

    public CustomMenuBar() {
        this(new JMenuBar(), "Root");
    }

    public String getName() {
        return name;
    }

    public List<CustomMenu> getMenus() {
        return this.menus;
    }

    public void add(CustomMenu menu) {
        if (menu == null) throw new NullPointerException("Menu is null.");
        this.menuBar.add(menu.getComponent());
        this.menus.add(menu);
    }

    public void add(CustomMenu... menus) {
        if (null == menus) throw new NullPointerException("Menus is null.");
        for (CustomMenu menu : menus) add(menu);
    }

    private void appendNode(StringBuilder result, CustomMenuBarNode node, int depth) {
        final String indent = "  ".repeat(Math.max(0, depth));

        if (node instanceof CustomMenu menu) {
            result.append(indent).
                append(menu.getText()).append(StringUtils.NEXT_LINE);
            for (CustomMenuBarNode child : menu.getNodes()) {
                appendNode(result, child, depth + 1);
            }
        } else if (node instanceof CustomMenuItem item) {
            result.append(indent).
                append("> ").append(item.getText()).append(StringUtils.NEXT_LINE);
        } else if (node instanceof CustomMenuSeparator) {
            result.append(indent).
                append("-".repeat(10)).append(StringUtils.NEXT_LINE);
        } else {
            // Fallback für unbekannte Knoten
            result.append(indent).append(node.getText()).append(StringUtils.NEXT_LINE);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (name != null && !name.isBlank()) {
            result.append(name).append(":").append(StringUtils.NEXT_LINE);
        }

        if (menus != null) {
            for (CustomMenu menu : menus) {
                appendNode(result, menu, 0);
            }
        }
        return result.toString();
    }
}