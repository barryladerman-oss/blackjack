package us.laderman.android.blackjack;

import org.junit.Test;
import static org.junit.Assert.*;

import us.laderman.android.blackjack.model.Card;

/**
 * Unit tests for the Card model class.
 */
public class CardTest {

    @Test
    public void testCardDisplayText() {
        Card card = new Card();
        // Note: In a real test, we'd need to set the fields or use a builder
        // This is a placeholder test structure
        assertNotNull("Card should not be null", card);
    }

    @Test
    public void testCardGetters() {
        Card card = new Card();
        // Test that getters don't throw exceptions
        card.getSuit();
        card.getRank();
        card.getValue();
        card.getLabel();
        card.getDisplayText();
    }
}
