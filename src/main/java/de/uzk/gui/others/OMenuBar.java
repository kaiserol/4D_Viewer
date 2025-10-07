package de.uzk.gui.others;

import de.uzk.gui.Gui;
import de.uzk.gui.InteractiveContainer;
import de.uzk.actions.ActionHandler;
import de.uzk.config.ConfigHandler;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.IconUtils;
import de.uzk.utils.StringUtils;
import de.uzk.config.LanguageHandler.Language;
import de.uzk.gui.tree.OBar;
import de.uzk.gui.tree.OBarItem;
import de.uzk.gui.tree.OBarMenu;
import de.uzk.gui.tree.OBarNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static de.uzk.Main.config;
import static de.uzk.actions.ActionUtils.*;
import static de.uzk.config.LanguageHandler.getWord;

public class OMenuBar extends InteractiveContainer<JMenuBar> {
    private OBar tree;

    public OMenuBar(Gui gui, ActionHandler actionHandler) {
        super(new JMenuBar(), gui);
        init(actionHandler);
    }

    private void init(ActionHandler actionHandler) {
        this.tree = new OBar(this.container);
        this.tree.add(getEditMenu(actionHandler));
        this.tree.add(getNavMenu(actionHandler));
        this.tree.add(getOptMenu());

        for (OBarNode nodes : this.tree.getNodes()) {
            this.container.add(nodes.getComponent());
        }

        setMnemonics(this.container.getComponents());
    }

    private void setMnemonics(Component[] children) {
        if (children == null) return;

        Map<String, Integer> usedWords = new TreeMap<>(String::compareTo);
        for (Component component : children) {
            if (component instanceof JMenuItem item) StringUtils.updateUsedWords(item.getText(), usedWords);
        }

        List<Character> mnemonics = new ArrayList<>();
        for (Component component : children) {
            if (component instanceof JMenuItem item) {
                List<String> allowedWords = StringUtils.getAllowedWords(item.getText(), usedWords);
                setMnemonic(item, allowedWords, mnemonics);
            }
            if (component instanceof JMenu menu) setMnemonics(menu.getMenuComponents());
        }
    }

    private void setMnemonic(JMenuItem item, List<String> allowedWords, List<Character> mnemonics) {
        if (!allowedWords.isEmpty()) {
            String lower = item.getText().toLowerCase();
            char[] chars = lower.toCharArray();

            for (String word : allowedWords) {
                int index = lower.indexOf(word);
                int max = index + word.length();

                for (int i = index; i < max; i++) {
                    char c = chars[i];
                    if (StringUtils.isAsciiLetter(c) && !mnemonics.contains(c)) {
                        item.setMnemonic(c);
                        item.setDisplayedMnemonicIndex(i);
                        mnemonics.add(c);
                        return;
                    }
                }
            }
        }
    }

    private OBarMenu getEditMenu(ActionHandler actionHandler) {
        OBarMenu editTreeMenu = new OBarMenu(getWord("items.edit"), true);
        editTreeMenu.add(new OBarItem(getWord("items.edit.turnImageLeft"), IconUtils.TURN_LEFT_ICON, a -> actionHandler.executeEdit(TURN_IMAGE_LEFT_ACTION), TURN_IMAGE_LEFT_ACTION),
                new OBarItem(getWord("items.edit.turnImageRight"), IconUtils.TURN_RIGHT_ICON, a -> actionHandler.executeEdit(TURN_IMAGE_RIGHT_ACTION), TURN_IMAGE_RIGHT_ACTION),
                new OBarItem(getWord("items.edit.pinTime"), IconUtils.PIN_ICON, a -> actionHandler.executeEdit(PIN_TIME_ACTION), PIN_TIME_ACTION));
        editTreeMenu.addSeparator();
        editTreeMenu.add(new OBarItem(getWord("items.edit.screenshot"), IconUtils.SCREENSHOT_ICON, a -> actionHandler.executeEdit(SCREENSHOT_ACTION), SCREENSHOT_ACTION));
        return editTreeMenu;
    }

    private OBarMenu getNavMenu(ActionHandler actionHandler) {
        OBarMenu navTreeMenu = new OBarMenu(getWord("items.nav"), true);
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.first"), IconUtils.FIRST_IMAGE_ICON, actionHandler, FIRST_IMAGE_ACTION));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.prev"), IconUtils.PREV_IMAGE_ICON, actionHandler, PREV_IMAGE_ACTION));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.next"), IconUtils.NEXT_IMAGE_ICON, actionHandler, NEXT_IMAGE_ACTION));
        navTreeMenu.add(new OBarItem(getWord("items.nav.image.last"), IconUtils.LAST_IMAGE_ICON, actionHandler, LAST_IMAGE_ACTION));
        navTreeMenu.addSeparator();
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.first"), IconUtils.FIRST_LEVEL_ICON, actionHandler, FIRST_LEVEL_ACTION));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.prev"), IconUtils.PREV_LEVEL_ICON, actionHandler, PREV_LEVEL_ACTION));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.next"), IconUtils.NEXT_LEVEL_ICON, actionHandler, NEXT_LEVEL_ACTION));
        navTreeMenu.add(new OBarItem(getWord("items.nav.level.last"), IconUtils.LAST_LEVEL_ICON, actionHandler, LAST_LEVEL_ACTION));
        return navTreeMenu;
    }

    private OBarMenu getOptMenu() {
        OBarMenu optTreeMenu = new OBarMenu(getWord("items.opt"));
        optTreeMenu.add(new OBarItem(getWord("items.opt.toggleTheme"), a -> GuiUtils.switchThemes(gui), TOGGLE_THEME_ACTION));
        optTreeMenu.addSeparator();

        OBarItem incrFontItem = new OBarItem(getWord("items.opt.fontSizeIncr"));
        OBarItem decrFontItem = new OBarItem(getWord("items.opt.fontSizeDecr"));
        OBarItem restoreFontItem = new OBarItem(getWord("items.opt.fontSizeRestore"));

        // set Actions
        incrFontItem.setAction(updateOptionsMenu(GuiUtils::incrFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), INCREASE_PLUS_FONT_ACTION, INCREASE_ADD_FONT_ACTION);
        decrFontItem.setAction(updateOptionsMenu(GuiUtils::decrFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), DECREASE_MINUS_FONT_ACTION, DECREASE_SUBTRACT_FONT_ACTION);
        restoreFontItem.setAction(updateOptionsMenu(GuiUtils::restoreFont,
                incrFontItem.getComponent(), decrFontItem.getComponent(), restoreFontItem.getComponent()), RESTORE_FONT_ACTION);

        // add and update font
        optTreeMenu.add(incrFontItem, decrFontItem, restoreFontItem);
        updateOptionsMenu(null,
                incrFontItem.getComponent(),
                decrFontItem.getComponent(),
                restoreFontItem.getComponent()).actionPerformed(null);

        optTreeMenu.addSeparator();

        OBarItem changeLanguageItem = new OBarItem(getWord("items.opt.changeLanguage"));
        changeLanguageItem.setAction(changeLanguageListener());
        optTreeMenu.add(changeLanguageItem);

        return optTreeMenu;
    }

    private ActionListener changeLanguageListener() {
        return a -> {
            JComboBox<Language> selectBox = new JComboBox<>(Language.values());
            JOptionPane.showConfirmDialog(null, selectBox, getWord("items.opt.changeLanguage"), JOptionPane.DEFAULT_OPTION);
            Language lang = (Language) selectBox.getSelectedItem();
            if (lang != null) {
                config.setLanguage(lang);
                config.saveConfig();
                int choice = JOptionPane.showConfirmDialog(null, getWord("items.opt.languageChanged.body"), getWord("items.opt.languageChanged.title"), JOptionPane.YES_NO_OPTION);
                if(choice == JOptionPane.YES_OPTION) {
                    gui.rebuild();
                }
            }
        };
    }

    private ActionListener updateOptionsMenu(Runnable runnable, JComponent incrFontItem, JComponent decrFontItem, JComponent restoreFontItem) {
        return a -> {
            if (runnable != null) runnable.run();
            int fontSize = config.getFontSize();
            incrFontItem.setEnabled(fontSize != ConfigHandler.MAX_FONT_SIZE);
            decrFontItem.setEnabled(fontSize != ConfigHandler.MIN_FONT_SIZE);
            restoreFontItem.setEnabled(fontSize != ConfigHandler.DEFAULT_FONT_SIZE);
        };
    }

    @Override
    public void toggleOn() {
        enableMenus(this.tree, true);
    }

    @Override
    public void toggleOff() {
        enableMenus(this.tree, false);
    }

    private void enableMenus(OBarNode parent, boolean enabled) {
        for (OBarNode node : parent.getNodes()) {
            if (parent.isToggleable()) {
                node.getComponent().setEnabled(enabled);
            }
            enableMenus(node, enabled);
        }
    }
}