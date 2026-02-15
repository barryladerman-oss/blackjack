package us.laderman.blackjack.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;

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
    void testHandValue() {
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.FIVE));

        HandValue value = hand.getValue();

        assertThat(value.getTotal()).isEqualTo(15);
    }

    @Test
    @DisplayName("should handle soft ace (11)")
    void testSoftAceValue() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.FIVE));

        HandValue value = hand.getValue();

        assertThat(value.getTotal()).isEqualTo(16);
        assertThat(value.isSoft()).isTrue();
    }

    @Test
    @DisplayName("should convert soft ace to hard (1) when bust would occur")
    void testSoftAceConversion() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.NINE));
        hand.addCard(new Card(Suit.CLUBS, Rank.EIGHT));

        HandValue value = hand.getValue();

        assertThat(value.getTotal()).isEqualTo(18);
        assertThat(value.isSoft()).isFalse();
    }

    @Test
    @DisplayName("should detect bust")
    void testBustDetection() {
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.QUEEN));
        hand.addCard(new Card(Suit.CLUBS, Rank.FIVE));

        assertThat(hand.isBusted()).isTrue();
    }

    @Test
    @DisplayName("should set standing state")
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

        HandValue value = hand.getValue();

        assertThat(value.getTotal()).isEqualTo(21);
    }

    @Test
    @DisplayName("should handle multiple aces")
    void testMultipleAces() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.ACE));
        hand.addCard(new Card(Suit.CLUBS, Rank.NINE));

        HandValue value = hand.getValue();

        assertThat(value.getTotal()).isEqualTo(21);
    }
}
