import de.uzk.gui.menubar.CustomMenu;
import de.uzk.gui.menubar.CustomMenuBar;
import de.uzk.gui.menubar.CustomMenuItem;
import de.uzk.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomMenuBarTest {

    private CustomMenuBar menuBar;

    @BeforeEach
    void setUp() {
        menuBar = new CustomMenuBar();
    }

    @Test
    void testAddMenu() {
        CustomMenu fileMenu = new CustomMenu("File");
        menuBar.add(fileMenu);

        assertTrue(menuBar.toString().contains("File"), "Menu not added correctly");
    }

    @Test
    void testAddDuplicateMenu() {
        CustomMenu fileMenu = new CustomMenu("File");
        menuBar.add(fileMenu);

        assertDoesNotThrow(() -> menuBar.add(fileMenu), "Duplicate menu was not added");
    }

    @Test
    void testAddMenuItemToMenu() {
        CustomMenu fileMenu = new CustomMenu("File");
        fileMenu.add(new CustomMenuItem("Open"));
        menuBar.add(fileMenu);

        assertTrue(menuBar.toString().contains("Open"), "Menu item not added correctly");
    }

    @Test
    void testToStringWithMultipleMenus() {
        CustomMenu fileMenu = new CustomMenu("File");
        fileMenu.add(new CustomMenuItem("Open"));
        fileMenu.add(new CustomMenuItem("Save"));
        fileMenu.addSeparator();

        CustomMenu editMenu = new CustomMenu("Edit");
        editMenu.add(new CustomMenuItem("Cut"));
        editMenu.add(new CustomMenuItem("Copy"));

        CustomMenuBar menuBar = new CustomMenuBar();
        menuBar.add(fileMenu, editMenu);

        String expectedOutput = "Root:" + StringUtils.NEXT_LINE +
                "File" + StringUtils.NEXT_LINE +
                "  > Open" + StringUtils.NEXT_LINE +
                "  > Save" + StringUtils.NEXT_LINE +
                "  SEP" + StringUtils.NEXT_LINE +
                "Edit" + StringUtils.NEXT_LINE +
                "  > Cut" + StringUtils.NEXT_LINE +
                "  > Copy" + StringUtils.NEXT_LINE;

        assertEquals(expectedOutput, menuBar.toString(), "toString method does not produce the expected output");
    }
}
