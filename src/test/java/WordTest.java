import org.junit.Before;
import org.junit.Test;
import de.uzk.utils.language.Word;

import static org.junit.Assert.*;

public class WordTest {

    @Before
    public void setUp() {
    }

    // test are words equals
    @Test
    public void testNormalizeWordErstes() {
        assertEquals("erst", Word.normalizeWord("Erstes"));
    }

    @Test
    public void testNormalizeWordErster() {
        assertEquals("erst", Word.normalizeWord("erster"));
    }

    @Test
    public void testNormalizeWordEsse() {
        assertEquals("ess", Word.normalizeWord("esse"));
    }

    @Test
    public void testNormalizeWordHaus() {
        assertEquals("haus", Word.normalizeWord("Haus"));
    }

    @Test
    public void testNormalizeWordTest() {
        assertEquals("test", Word.normalizeWord("Test"));
    }

    @Test
    public void testNormalizeWordWort() {
        assertEquals("wort", Word.normalizeWord("Wort"));
    }

    @Test
    public void testNormalizeWordTee() {
        assertEquals("tee", Word.normalizeWord("Tee"));
    }

    @Test
    public void testNormalizeWordSeen() {
        assertEquals("see", Word.normalizeWord("seen"));
    }

    @Test
    public void testNormalizeWordSick() {
        assertEquals("sick", Word.normalizeWord("SICK"));
    }

    // test are words similar
    @Test
    public void testAreWordsSimilarErstesErste() {
        assertTrue(Word.areWordsSimilar("Erstes", "erste"));
    }

    @Test
    public void testAreWordsSimilarErsterErste() {
        assertTrue(Word.areWordsSimilar("erster", "Erste"));
    }

    @Test
    public void testAreWordsSimilarHausHauser() {
        assertTrue(Word.areWordsSimilar("Haus", "Häuser"));
    }

    @Test
    public void testAreWordsSimilarTestTests() {
        assertTrue(Word.areWordsSimilar("Test", "Tests"));
    }

    @Test
    public void testAreWordsSimilarWortWort() {
        assertTrue(Word.areWordsSimilar("Wort", "wort"));
    }

    @Test
    public void testAreWordsSimilarApfelApfelsaft() {
        assertFalse(Word.areWordsSimilar("Apfel", "Apfelsaft"));
    }

    @Test
    public void testAreWordsSimilarTeeTees() {
        assertTrue(Word.areWordsSimilar("Tee", "Tees"));
    }

    @Test
    public void testAreWordsSimilarSeenSee() {
        assertTrue(Word.areWordsSimilar("seen", "see"));
    }

    @Test
    public void testAreWordsSimilarFuhren() {
        assertTrue(Word.areWordsSimilar("führ", "führen"));
    }
}