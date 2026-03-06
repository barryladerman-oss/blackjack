package us.laderman.android.blackjack;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Basic instrumented test to verify the test framework is working.
 */
public class BasicTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testStringOperations() {
        String test = "Blackjack";
        assertEquals(9, test.length());
        assertTrue(test.startsWith("Black"));
        assertTrue(test.endsWith("jack"));
    }

    @Test
    public void testBasicLogic() {
        int handValue = 21;
        assertTrue("Blackjack value should be 21", handValue == 21);
        assertFalse("Hand should not be busted", handValue > 21);
    }
}
