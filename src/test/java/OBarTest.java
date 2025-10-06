import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.uzk.utils.SystemConstants;
import de.uzk.utils.tree.OBar;
import de.uzk.utils.tree.OBarItem;
import de.uzk.utils.tree.OBarMenu;

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
        fileMenu.add(new OBarItem("Open"),
                new OBarItem("Save"));
        fileMenu.addSeparator();

        OBarMenu editMenu = new OBarMenu("Edit");
        editMenu.add(new OBarItem("Cut"),
                new OBarItem("Copy"));

        OBar tree = new OBar();
        tree.add(fileMenu, editMenu);

        String expectedOutput = "Root" + SystemConstants.NEXT_LINE +
                "  File" + SystemConstants.NEXT_LINE +
                "    > Open" + SystemConstants.NEXT_LINE +
                "    > Save" + SystemConstants.NEXT_LINE +
                "    SEP" + SystemConstants.NEXT_LINE +
                "  Edit" + SystemConstants.NEXT_LINE +
                "    > Cut" + SystemConstants.NEXT_LINE +
                "    > Copy" + SystemConstants.NEXT_LINE;

        assertEquals(expectedOutput, tree.toString(), "toString method does not produce the expected output");
    }
}
