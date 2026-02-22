package us.laderman.blackjack.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;

import java.util.HashMap;

@DisplayName("Dealer Ten Blackjack Logic Tests")
class DealerTenBlackjackTest {
    @Test
    @DisplayName("should resolve game if dealer has blackjack with ten up card")
    void testDealerBlackjackWithTenUpCard() {
        HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
        hands.put(0, new BlackjackService.HandCards(
                new Card(Suit.HEARTS, Rank.FIVE),
                new Card(Suit.SPADES, Rank.SIX)));
        // Dealer up card is TEN, hole card is ACE (blackjack)
        BlackjackGame game = new BlackjackGame(1, 1, hands,
                new Card(Suit.DIAMONDS, Rank.TEN),
                new Card(Suit.CLUBS, Rank.ACE));
        GameState state = game.getState();
        // Game should be resolved immediately (phase COMPLETE)
        assertThat(state.getPhase()).isEqualTo("COMPLETE");
        assertThat(state.getDealer().getCards()).hasSize(2);
        assertThat(state.getDealer().getTotal()).isEqualTo(21);
    }

    @Test
    @DisplayName("should not resolve game if dealer has ten up card but not blackjack")
    void testDealerTenNoBlackjack() {
        HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
        hands.put(0, new BlackjackService.HandCards(
                new Card(Suit.HEARTS, Rank.FIVE),
                new Card(Suit.SPADES, Rank.SIX)));
        // Dealer up card is TEN, hole card is NINE (not blackjack)
        BlackjackGame game = new BlackjackGame(1, 1, hands,
                new Card(Suit.DIAMONDS, Rank.TEN),
                new Card(Suit.CLUBS, Rank.NINE));
        GameState state = game.getState();
        // Game should be in PLAYER_TURN phase
        assertThat(state.getPhase()).isEqualTo("PLAYER_TURN");
        assertThat(state.getDealer().getCards()).hasSize(2);
        // During PLAYER_TURN, only the up card value is shown as dealer total
        assertThat(state.getDealer().getTotal()).isEqualTo(10);
    }
}
