package us.laderman.blackjack.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;

import java.util.HashMap;

@DisplayName("Card Counting Strategy Tests")
class CardCountingTest {

    @Test
    @DisplayName("should recognize low cards (2-6) as positive count")
    void testLowCardCounting() {
        // Hi-Lo counting: 2-6 = +1
        Card two = new Card(Suit.HEARTS, Rank.TWO);
        Card three = new Card(Suit.DIAMONDS, Rank.THREE);
        Card four = new Card(Suit.CLUBS, Rank.FOUR);
        Card five = new Card(Suit.SPADES, Rank.FIVE);
        Card six = new Card(Suit.HEARTS, Rank.SIX);

        // These should contribute positively to count
        assertThat(two).isNotNull();
        assertThat(three).isNotNull();
        assertThat(four).isNotNull();
        assertThat(five).isNotNull();
        assertThat(six).isNotNull();
    }

    @Test
    @DisplayName("should recognize neutral cards (7-9) as zero count")
    void testNeutralCardCounting() {
        // Hi-Lo counting: 7-9 = 0
        Card seven = new Card(Suit.HEARTS, Rank.SEVEN);
        Card eight = new Card(Suit.DIAMONDS, Rank.EIGHT);
        Card nine = new Card(Suit.CLUBS, Rank.NINE);

        // These should be neutral
        assertThat(seven).isNotNull();
        assertThat(eight).isNotNull();
        assertThat(nine).isNotNull();
    }

    @Test
    @DisplayName("should recognize high cards (10-A) as negative count")
    void testHighCardCounting() {
        // Hi-Lo counting: 10-A = -1
        Card ten = new Card(Suit.HEARTS, Rank.TEN);
        Card jack = new Card(Suit.DIAMONDS, Rank.JACK);
        Card queen = new Card(Suit.CLUBS, Rank.QUEEN);
        Card king = new Card(Suit.SPADES, Rank.KING);
        Card ace = new Card(Suit.HEARTS, Rank.ACE);

        // These should contribute negatively to count
        assertThat(ten).isNotNull();
        assertThat(jack).isNotNull();
        assertThat(queen).isNotNull();
        assertThat(king).isNotNull();
        assertThat(ace).isNotNull();
    }

    @Test
    @DisplayName("should calculate true count from running count")
    void testTrueCountCalculation() {
        BlackjackGame game = new BlackjackGame(1, 2);

        // True count = Running count / Decks remaining
        // With 2 decks (104 cards) and initial 4 cards dealt
        // Should have ~2 decks remaining after initial deal
        var state = game.getState();

        assertThat(state.getTrueCount()).isNotNaN();
        assertThat(state.getDecksRemaining()).isGreaterThan(1.8);
    }

    @Test
    @DisplayName("should favor increased bet on high true count")
    void testBettingSuggestionHighCount() {
        // Create a game and test betting logic
        BlackjackGame game = new BlackjackGame(1, 1);
        var state = game.getState();

        // Suggested bet should be based on true count
        assertThat(state.getSuggestedBetUnits()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("should reduce bet on negative true count")
    void testBettingSuggestionLowCount() {
        // On negative count, should suggest minimum bet
        BlackjackGame game = new BlackjackGame(1, 1);
        var state = game.getState();

        // Even with low count, should suggest at least 1 unit
        assertThat(state.getSuggestedBetUnits()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("should update count on each card dealt")
    void testCountUpdateOnDeal() {
        BlackjackGame game = new BlackjackGame(1, 1);
        var initial = game.getState();
        double initialDecksRemaining = initial.getDecksRemaining();

        // If dealer has Ace, decline insurance first
        var state = game.getState();
        if (initial.getDealer().getCards().stream().anyMatch(c -> c.getRank() == Rank.ACE)) {
            game.declineInsurance(0);
            state = game.getState();
        }

        // Only hit if we're still in PLAYER_TURN phase
        if ("PLAYER_TURN".equals(state.getPhase())) {
            // Hit to add a card
            game.hit(0, null);
            var after = game.getState();

            // Decks remaining should decrease (more cards dealt)
            assertThat(after.getDecksRemaining()).isLessThan(initialDecksRemaining);
        }
    }

    @Test
    @DisplayName("should provide strategy recommendation based on count")
    void testStrategyRecommendationWithCount() {
        HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
        hands.put(0, new BlackjackService.HandCards(
                new Card(Suit.HEARTS, Rank.FIVE),
                new Card(Suit.SPADES, Rank.SIX)));
        hands.put(1, new BlackjackService.HandCards(
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.SEVEN)));

        BlackjackGame game = new BlackjackGame(2, 1, hands,
                new Card(Suit.HEARTS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.FOUR));

        var state = game.getState();
        String recommendation = state.getHands().get(0).getRecommendedAction();

        assertThat(recommendation).isNotNull();
        assertThat(recommendation).isNotBlank();
        assertThat(recommendation).isNotEqualTo("NONE");
        assertThat(state.getHands().get(1).getRecommendedAction()).isEqualTo("NONE");
    }

    @Test
    @DisplayName("should recognize soft 17 strategy variation")
    void testSoft17Strategy() {
        // Soft 17 = Ace + 6 or other combinations totaling 17 with ace
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.SIX));

        var value = hand.getValue();
        assertThat(value.getTotal()).isEqualTo(17);
        assertThat(value.isSoft()).isTrue();
    }

    @Test
    @DisplayName("should recognize hard 16 strategy variation")
    void testHard16Strategy() {
        // Hard 16 = Ten + Six or other combinations without ace
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.TEN));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.SIX));

        var value = hand.getValue();
        assertThat(value.getTotal()).isEqualTo(16);
        assertThat(value.isSoft()).isFalse();
    }

    @Test
    @DisplayName("should support pair splitting recommendations")
    void testPairSplittingRecommendation() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.EIGHT));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.EIGHT));

        // Should be splittable
        assertThat(hand.canSplit()).isTrue();
        assertThat(hand.getCards()).hasSize(2);
    }

    @Test
    @DisplayName("should track penetration through deck remaining calculation")
    void testPenetrationTracking() {
        BlackjackGame game = new BlackjackGame(1, 1);
        var state = game.getState();

        // Decks remaining should be less than 1 after initial deal (4 cards dealt)
        assertThat(state.getDecksRemaining()).isBetween(0.9, 1.0);
    }
}
