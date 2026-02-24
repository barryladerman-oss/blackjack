
package us.laderman.blackjack.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.Hand;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;

/**
 * Unit tests for Hand class logic in Blackjack.
 */
/**
 * Unit tests for Hand class logic in Blackjack.
 */
@DisplayName("Hand Unit Tests")
class HandTest {

    private Hand hand;

    @BeforeEach
    void setUp() {
        hand = new Hand();
    }

    @Test
    @DisplayName("should add card to hand")
    void testAddCard() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        hand.addCard(card);

        assertThat(hand.getCards()).hasSize(1);
        assertThat(hand.getCards().get(0)).isEqualTo(card);
    }

    @Test
    @DisplayName("should calculate hand value correctly")
    /**
     * Tests correct calculation of hand value.
     */
    void testHandValue() {
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.FIVE));

        assertThat(hand.getValue()).isEqualTo(15);
    }

    @Test
    @DisplayName("should handle soft ace (11)")
    /**
     * Tests handling of soft ace (value 11).
     */
    void testSoftAceValue() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.FIVE));

        assertThat(hand.getValue()).isEqualTo(16);
        assertThat(hand.isSoft()).isTrue();
    }

    @Test
    @DisplayName("should convert soft ace to hard (1) when bust would occur")
    /**
     * Tests conversion of soft ace to hard when bust would occur.
     */
    void testSoftAceConversion() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.NINE));
        hand.addCard(new Card(Suit.CLUBS, Rank.EIGHT));

        assertThat(hand.getValue()).isEqualTo(18);
        assertThat(hand.isSoft()).isFalse();
    }

    @Test
    @DisplayName("should detect bust")
    /**
     * Tests detection of bust state in hand.
     */
    void testBustDetection() {
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.QUEEN));
        hand.addCard(new Card(Suit.CLUBS, Rank.FIVE));

        assertThat(hand.isBusted()).isTrue();
    }

    @Test
    @DisplayName("should set standing state")
    /**
     * Tests setting and checking standing state.
     */
    void testStandingState() {
        hand.addCard(new Card(Suit.HEARTS, Rank.TEN));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.FIVE));

        assertThat(hand.isStanding()).isFalse();

        hand.setStanding(true);

        assertThat(hand.isStanding()).isTrue();
    }

    @Test
    @DisplayName("should detect splittable hand")
    void testCanSplit() {
        hand.addCard(new Card(Suit.HEARTS, Rank.EIGHT));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.EIGHT));

        assertThat(hand.canSplit()).isTrue();
    }

    @Test
    @DisplayName("should not split when cards do not match")
    void testCannotSplitDifferentRanks() {
        hand.addCard(new Card(Suit.HEARTS, Rank.EIGHT));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.NINE));

        assertThat(hand.canSplit()).isFalse();
    }

    @Test
    @DisplayName("should not split when more than two cards")
    void testCannotSplitWithThreeCards() {
        hand.addCard(new Card(Suit.HEARTS, Rank.EIGHT));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.EIGHT));
        hand.addCard(new Card(Suit.CLUBS, Rank.FIVE));

        assertThat(hand.canSplit()).isFalse();
    }

    @Test
    @DisplayName("should track bet units")
    void testBetUnits() {
        assertThat(hand.getBetUnits()).isEqualTo(1);

        hand.setBetUnits(2);

        assertThat(hand.getBetUnits()).isEqualTo(2);
    }

    @Test
    @DisplayName("should track insurance state")
    void testInsuranceState() {
        assertThat(hand.isInsured()).isFalse();
        assertThat(hand.isInsuranceWon()).isFalse();

        hand.setInsured(true);
        hand.setInsuranceWon(true);

        assertThat(hand.isInsured()).isTrue();
        assertThat(hand.isInsuranceWon()).isTrue();
    }

    @Test
    @DisplayName("should track split origin")
    void testSplitOrigin() {
        hand.setIsFromSplit(true);

        assertThat(hand.isFromSplit()).isTrue();
    }

    @Test
    @DisplayName("should split hand and get second card")
    void testSplitOffCard() {
        Card card1 = new Card(Suit.HEARTS, Rank.SEVEN);
        Card card2 = new Card(Suit.DIAMONDS, Rank.SEVEN);

        hand.addCard(card1);
        hand.addCard(card2);

        assertThat(hand.getCards()).hasSize(2);

        Card removed = hand.splitOff();

        assertThat(removed).isEqualTo(card2);
        assertThat(hand.getCards()).hasSize(1);
        assertThat(hand.getCards().get(0)).isEqualTo(card1);
    }

    @Test
    @DisplayName("should calculate 21 correctly")
    void testBlackjack() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.KING));

        assertThat(hand.getValue()).isEqualTo(21);
    }

    @Test
    @DisplayName("should handle multiple aces")
    void testMultipleAces() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.ACE));
        hand.addCard(new Card(Suit.CLUBS, Rank.NINE));

        assertThat(hand.getValue()).isEqualTo(21);
    }

    @Test
    @DisplayName("should handle edge cases and invalid input")
    void testEdgeCases() {
        // Empty hand
        Hand emptyHand = new Hand();
        assertThat(emptyHand.getCards()).isEmpty();
        assertThat(emptyHand.getValue()).isEqualTo(0);

        // Duplicate cards
        Card card = new Card(Suit.HEARTS, Rank.TEN);
        hand.addCard(card);
        hand.addCard(card);
        assertThat(hand.getCards()).hasSize(2);
        // Overflow (simulate adding many cards)
        Hand overflowHand = new Hand();
        for (int i = 0; i < 20; i++) {
            overflowHand.addCard(new Card(Suit.CLUBS, Rank.TWO));
        }
        assertThat(overflowHand.getCards()).hasSize(20);
        assertThat(overflowHand.getValue()).isGreaterThan(21);
        // Invalid card values (simulate with null)
        overflowHand.addCard(null);
        assertThat(overflowHand.getCards()).containsNull();
    }
}
