import de.uzk.gui.tree.OBar;
import de.uzk.gui.tree.OBarItem;
import de.uzk.gui.tree.OBarMenu;
import de.uzk.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OBarTest {

    private OBar tree;

    @BeforeEach
    void setUp() {
        tree = new OBar();
    }

    @Test
    void testAddMenu() {
        OBarMenu menu = new OBarMenu("File");
        tree.add(menu);

        assertTrue(tree.toString().contains("File"), "Menu not added correctly");
    }

    @Test
    void testAddDuplicateMenu() {
        OBarMenu menu = new OBarMenu("File");
        tree.add(menu);

        assertDoesNotThrow(() -> tree.add(menu), "Duplicate menu was not added");
    }

    @Test
    void testAddMenuItemToMenu() {
        OBarMenu menu = new OBarMenu("File");
        OBarItem item = new OBarItem("Open");
        menu.add(item);
        tree.add(menu);

        assertTrue(tree.toString().contains("Open"), "Menu item not added correctly");
    }

    @Test
    void testToStringWithMultipleMenus() {
        OBarMenu fileMenu = new OBarMenu("File");
        fileMenu.add(new OBarItem("Open"));
        fileMenu.add(new OBarItem("Save"));
        fileMenu.addSeparator();

        OBarMenu editMenu = new OBarMenu("Edit");
        editMenu.add(new OBarItem("Cut"));
        editMenu.add(new OBarItem("Copy"));

        OBar tree = new OBar();
        tree.add(fileMenu, editMenu);

        String expectedOutput = "Root" + StringUtils.NEXT_LINE +
                "  File" + StringUtils.NEXT_LINE +
                "    > Open" + StringUtils.NEXT_LINE +
                "    > Save" + StringUtils.NEXT_LINE +
                "    SEP" + StringUtils.NEXT_LINE +
                "  Edit" + StringUtils.NEXT_LINE +
                "    > Cut" + StringUtils.NEXT_LINE +
                "    > Copy" + StringUtils.NEXT_LINE;

        assertEquals(expectedOutput, tree.toString(), "toString method does not produce the expected output");
    }
}
