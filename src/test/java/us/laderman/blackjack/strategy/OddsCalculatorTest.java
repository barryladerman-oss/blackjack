package us.laderman.blackjack.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.laderman.blackjack.model.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OddsCalculator class.
 */
@DisplayName("OddsCalculator Tests")
class OddsCalculatorTest {

    @Test
    @DisplayName("should calculate bust probability for hand value 12")
    void testBustProbabilityFor12() {
        // Hand with value 12 (10 + 2)
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.TEN));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.TWO));

        // Create a shoe with one deck
        Shoe shoe = new Shoe(1);

        double bustProb = OddsCalculator.calculateBustProbability(shoe, hand);

        // For hand value 12, cards 10, J, Q, K cause bust (16 cards in a 52-card deck)
        // Probability should be around 16/50 = 0.32 (we removed 2 cards from the deck)
        assertThat(bustProb).isGreaterThan(0.0).isLessThan(1.0);
    }

    @Test
    @DisplayName("should return 0 for busted hand")
    void testBustProbabilityForBustedHand() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.TEN));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.QUEEN));
        hand.addCard(new Card(Suit.CLUBS, Rank.FIVE));

        Shoe shoe = new Shoe(1);

        double bustProb = OddsCalculator.calculateBustProbability(shoe, hand);

        assertThat(bustProb).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should return 0 for standing hand")
    void testBustProbabilityForStandingHand() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.TEN));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.SEVEN));
        hand.setStanding(true);

        Shoe shoe = new Shoe(1);

        double bustProb = OddsCalculator.calculateBustProbability(shoe, hand);

        assertThat(bustProb).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should return 0 for hand value 21")
    void testBustProbabilityFor21() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.KING));

        Shoe shoe = new Shoe(1);

        double bustProb = OddsCalculator.calculateBustProbability(shoe, hand);

        assertThat(bustProb).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should calculate higher bust probability for hand value 20")
    void testBustProbabilityFor20() {
        // Hand with value 20 (10 + 10)
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.TEN));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.QUEEN));

        Shoe shoe = new Shoe(1);

        double bustProb = OddsCalculator.calculateBustProbability(shoe, hand);

        // Only Ace (value 1 or 11, but counted as 1 here) won't bust, so bust
        // probability should be very high
        // Approximately 48/50 = 0.96
        assertThat(bustProb).isGreaterThan(0.9);
    }

    @Test
    @DisplayName("should calculate low bust probability for hand value 11 or less")
    void testBustProbabilityFor11() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.SIX));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.FIVE));

        Shoe shoe = new Shoe(1);

        double bustProb = OddsCalculator.calculateBustProbability(shoe, hand);

        // For hand value 11, no card can cause bust
        assertThat(bustProb).isEqualTo(0.0);
    }
}
