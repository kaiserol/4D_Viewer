import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.uzk.handler.ImageHandler;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ImageHandlerTest {
    private ImageHandler imageHandler;

    @BeforeEach
    public void setUp() {
        imageHandler = new ImageHandler();
    }

    @Test
    void setImageFolder_WithValidFolder_ShouldSetImageFolder() {
        File folder = new File("");
        imageHandler.setImageFolder(folder);

        assertEquals(folder, imageHandler.getImageFolder());
    }

    @Test
    void setImageFolder_WithNullFolder_ShouldNotSetImageFolder() {
        imageHandler.setImageFolder(null);

        assertNull(imageHandler.getImageFolder());
    }

    @Test
    void hasImageFolder_WithNullFolder_ShouldReturnFalse() {
        assertFalse(imageHandler.hasImageFolder());
    }

    @Test
    void hasImageFolder_WithValidFolder_ShouldReturnTrue() {
        File folder = new File("");
        imageHandler.setImageFolder(folder);

        assertTrue(imageHandler.hasImageFolder());
    }
}