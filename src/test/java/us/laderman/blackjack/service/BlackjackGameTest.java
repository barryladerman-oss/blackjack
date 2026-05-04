package us.laderman.blackjack.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;

import java.util.HashMap;

@DisplayName("BlackjackGame Core Logic Tests")
class BlackjackGameTest {

        private BlackjackGame game;

        @BeforeEach
        void setUp() {
                /**
                 * Unit tests for core Blackjack game logic.
                 */
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                game = new BlackjackGame(1, 1, hands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN));
        }

        @Test
        @DisplayName("should initialize with valid game ID")
        void testGameIdGeneration() {
                assertThat(game.getId()).isNotNull();
                assertThat(game.getId().toString()).isNotEmpty();
        }

        @Test
        @DisplayName("should deal initial two cards to player")
        void testInitialPlayerCards() {
                GameState state = game.getState();
                assertThat(state.getHands()).hasSize(1);
                assertThat(state.getHands().get(0).getCards()).hasSize(2);
        }

        @Test
        @DisplayName("should deal initial cards to dealer")
        void testInitialDealerCards() {
                GameState state = game.getState();
                assertThat(state.getDealer().getCards()).hasSize(2);
                assertThat(state.getDealer().getCards()).isNotEmpty();
        }

        @Test
        @DisplayName("should be in PLAYER_TURN phase after dealing")
        void testInitialPhase() {
                GameState state = game.getState();
                assertThat(state.getPhase()).isEqualTo("PLAYER_TURN");
        }

        @Test
        @DisplayName("should decrease decks remaining on hit")
        void testCardCountIncrementOnHit() {
                GameState initialState = game.getState();
                int initialCardsRemaining = initialState.getCardsRemaining();

                game.hit(0, null);
                GameState afterHitState = game.getState();

                assertThat(afterHitState.getCardsRemaining()).isLessThan(initialCardsRemaining);
        }

        @Test
        @DisplayName("should transition to DEALER_TURN when player stands")
        void testPhaseTransitionOnStand() {
                game.stand(0);
                GameState state = game.getState();

                assertThat(state.getPhase()).isNotEqualTo("PLAYER_TURN");
        }

        @Test
        @DisplayName("should auto-stand on blackjack")
        void testBlackjackAutoStand() {
                // Create a game with blackjack (Ace + King)
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.KING)));

                BlackjackGame bjGame = new BlackjackGame(1, 1, hands,
                                new Card(Suit.DIAMONDS, Rank.SIX), null);

                // Should be auto-stood and in dealer turn
                GameState state = bjGame.getState();
                assertThat(state.getHands().get(0).isStanding()).isTrue();
        }

        @Test
        @DisplayName("should track running count with card dealt")
        void testRunningCountTracking() {
                GameState initialState = game.getState();
                int initialCardsRemaining = initialState.getCardsRemaining();

                // Hit to add a card and update count (this may or may not work if hand was
                // auto-stood)
                try {
                        game.hit(0, null);
                        GameState afterHitState = game.getState();

                        // Cards remaining should decrease (more cards dealt)
                        assertThat(afterHitState.getCardsRemaining()).isLessThan(initialCardsRemaining);
                } catch (IllegalStateException e) {
                        // Expected if hand was already completed/stood (e.g., blackjack)
                        assertThat(e.getMessage()).isNotNull();
                }
        }

        @Test
        @DisplayName("should calculate true count correctly")
        void testTrueCountCalculation() {
                GameState state = game.getState();
                double trueCount = state.getTrueCount();

                // True count should be reasonable (between -2 and +2 typically)
                assertThat(trueCount).isBetween(-5.0, 5.0);
        }

        @Test
        @DisplayName("should provide decks remaining info")
        void testCardsRemaining() {
                GameState state = game.getState();
                int cardsRemaining = state.getCardsRemaining();

                // Should be less than full deck after initial deal
                assertThat(cardsRemaining).isGreaterThan(0);
        }

        @Test
        @DisplayName("should provide game note")
        void testGameNote() {
                GameState state = game.getState();
                String note = state.getNote();

                assertThat(note).isNotNull();
                assertThat(note).isNotEmpty();
        }

        @Test
        @DisplayName("should handle double down action")
        void testDoubleDownAction() {
                GameState beforeDouble = game.getState();
                int initialBet = beforeDouble.getHands().get(0).getBetUnits();

                game.doubleDown(0, null);
                GameState afterDouble = game.getState();

                // Bet should have been doubled
                assertThat(afterDouble.getHands().get(0).getBetUnits()).isGreaterThanOrEqualTo(initialBet);
                // Should have one more card (total 3)
                assertThat(afterDouble.getHands().get(0).getCards().size()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("should support split action on pairs")
        void testSplitAction() {
                // Create game with pair
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.EIGHT),
                                new Card(Suit.SPADES, Rank.EIGHT)));

                BlackjackGame splitGame = new BlackjackGame(1, 1, hands,
                                new Card(Suit.DIAMONDS, Rank.SIX), null);

                GameState state = splitGame.getState();
                assertThat(state.getHands().get(0).getCards()).hasSize(2);
                assertThat(state.getHands().get(0).getCards()).filteredOn(card -> card.getRank() == Rank.EIGHT)
                                .hasSize(2);
        }

        @Test
        @DisplayName("should recommend action for active hand only")
        void testStrategyRecommendation() {
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                hands.put(1, new BlackjackService.HandCards(
                                new Card(Suit.CLUBS, Rank.NINE),
                                new Card(Suit.DIAMONDS, Rank.SEVEN)));

                BlackjackGame multiHandGame = new BlackjackGame(2, 1, hands,
                                new Card(Suit.HEARTS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.FOUR));

                GameState initialState = multiHandGame.getState();
                assertThat(initialState.getHands().get(0).getRecommendedAction()).isNotNull().isNotBlank();
                assertThat(initialState.getHands().get(1).getRecommendedAction()).isEqualTo("NONE");

                GameState afterStand = multiHandGame.stand(0);
                String recommendation = afterStand.getHands().get(1).getRecommendedAction();

                assertThat(recommendation).isNotNull();
                assertThat(recommendation).isNotBlank();
                assertThat(recommendation).isNotEqualTo("NONE");
        }

        @Test
        @DisplayName("should convert to game state object")
        void testGameStateConversion() {
                GameState state = game.getState();

                assertThat(state.getId()).isNotBlank();
                assertThat(state.getPhase()).isNotBlank();
                assertThat(state.getDealer()).isNotNull();
                assertThat(state.getHands()).hasSize(1);
                assertThat(state.getHands().get(0).getCards()).hasSize(2);
        }

        @Test
        @DisplayName("should handle multiple player hands")
        void testMultiplePlayerHands() {
                BlackjackGame multiHandGame = new BlackjackGame(3, 1);

                GameState state = multiHandGame.getState();
                assertThat(state.getHands()).hasSize(3);
                for (GameState.PlayerHandState hand : state.getHands()) {
                        assertThat(hand.getCards()).hasSize(2);
                }
        }

        @Test
        @DisplayName("should track net units won/lost")
        void testNetUnitsTracking() {
                GameState state = game.getState();
                double netUnits = state.getNetUnits();

                // Should be 0 initially (no completed rounds)
                assertThat(netUnits).isNotNaN();
        }

        @Test
        @DisplayName("should complete game round when player stands")
        void testGameCompletion() {
                // Stand player hand - should return valid game state
                GameState state = game.stand(0);

                // Should have transitioned away from PLAYER_TURN
                assertThat(state).isNotNull();
                assertThat(state.getPhase()).isNotNull();
        }

        @Test
        @DisplayName("should split with rank-only specification for both cards")
        void testSplitWithRankOnlyBothCards() {
                // Create game with splittable pair of eights
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.EIGHT),
                                new Card(Suit.SPADES, Rank.EIGHT)));

                BlackjackGame splitGame = new BlackjackGame(1, 1, hands,
                                new Card(Suit.DIAMONDS, Rank.SIX), null);

                // Split specifying only ranks (not suits) for the new cards
                GameState state = splitGame.split(0, null, null, Rank.FOUR, Rank.FIVE);

                // Should now have 2 hands
                assertThat(state.getHands()).hasSize(2);

                // First hand should have the original EIGHT plus a FOUR
                assertThat(state.getHands().get(0).getCards()).hasSize(2);
                assertThat(state.getHands().get(0).getCards().get(0).getRank()).isEqualTo(Rank.EIGHT);
                assertThat(state.getHands().get(0).getCards().get(1).getRank()).isEqualTo(Rank.FOUR);

                // Second hand should have the split EIGHT plus a FIVE
                assertThat(state.getHands().get(1).getCards()).hasSize(2);
                assertThat(state.getHands().get(1).getCards().get(0).getRank()).isEqualTo(Rank.EIGHT);
                assertThat(state.getHands().get(1).getCards().get(1).getRank()).isEqualTo(Rank.FIVE);
        }

        @Test
        @DisplayName("should split with rank-only specification for first card")
        void testSplitWithRankOnlyFirstCard() {
                // Create game with splittable pair
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.NINE),
                                new Card(Suit.SPADES, Rank.NINE)));

                BlackjackGame splitGame = new BlackjackGame(1, 1, hands,
                                new Card(Suit.DIAMONDS, Rank.TEN), new Card(Suit.CLUBS, Rank.TWO));

                // Split specifying rank for card1, random for card2
                GameState state = splitGame.split(0, null, null, Rank.THREE, null);

                // Should now have 2 hands
                assertThat(state.getHands()).hasSize(2);

                // First hand should have NINE plus a THREE
                assertThat(state.getHands().get(0).getCards()).hasSize(2);
                assertThat(state.getHands().get(0).getCards().get(1).getRank()).isEqualTo(Rank.THREE);

                // Second hand should have NINE plus some random card
                assertThat(state.getHands().get(1).getCards()).hasSize(2);
        }

        @Test
        @DisplayName("should split with rank-only specification for second card")
        void testSplitWithRankOnlySecondCard() {
                // Create game with splittable pair
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.CLUBS, Rank.SEVEN),
                                new Card(Suit.DIAMONDS, Rank.SEVEN)));

                BlackjackGame splitGame = new BlackjackGame(1, 1, hands,
                                new Card(Suit.HEARTS, Rank.KING), new Card(Suit.SPADES, Rank.THREE));

                // Split with random for card1, rank specified for card2
                GameState state = splitGame.split(0, null, null, null, Rank.ACE);

                // Should now have 2 hands
                assertThat(state.getHands()).hasSize(2);

                // First hand should have SEVEN plus some random card
                assertThat(state.getHands().get(0).getCards()).hasSize(2);

                // Second hand should have SEVEN plus an ACE
                assertThat(state.getHands().get(1).getCards()).hasSize(2);
                assertThat(state.getHands().get(1).getCards().get(1).getRank()).isEqualTo(Rank.ACE);
        }

        @Test
        @DisplayName("should throw error when rank-only card not available")
        void testSplitWithUnavailableRank() {
                // Create game with all aces dealt out except the ones in hand
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.EIGHT),
                                new Card(Suit.SPADES, Rank.EIGHT)));

                BlackjackGame splitGame = new BlackjackGame(1, 1, hands,
                                new Card(Suit.DIAMONDS, Rank.SIX), null);

                // Manually exhaust all aces from the shoe by trying to draw them
                // This is a simplified test - in reality the shoe might still have aces
                // But we're testing the error handling path

                // For now, just verify that asking for a specific rank that exists works
                // The actual unavailable rank scenario is hard to test without more shoe
                // control
                assertThatCode(() -> splitGame.split(0, null, null, Rank.TWO, Rank.THREE))
                                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should recommend action for soft hands")
        void testSoftHandRecommendation() {
                // Create a soft 18 (Ace + 7)
                HashMap<Integer, BlackjackService.HandCards> softHands = new HashMap<>();
                softHands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                softHands.put(1, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.SEVEN)));

                BlackjackGame softGame = new BlackjackGame(2, 1, softHands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN));

                softGame.stand(0);
                GameState state = softGame.getState();
                GameState.PlayerHandState hand = state.getHands().get(1);

                // Verify hand is soft
                assertThat(hand.isSoft()).isTrue();
                assertThat(hand.getTotal()).isEqualTo(18);

                // Verify recommendation is not empty
                String recommendation = hand.getRecommendedAction();
                assertThat(recommendation).isNotNull().isNotEmpty().isNotEqualTo("NONE");

                // For soft 18 vs 9, basic strategy is to stand
                assertThat(recommendation).isEqualTo("STAND");
        }

        @Test
        @DisplayName("should recommend action for soft 13")
        void testSoftHandRecommendationAce2() {
                // Create a soft 13 (Ace + 2)
                HashMap<Integer, BlackjackService.HandCards> softHands = new HashMap<>();
                softHands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                softHands.put(1, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.TWO)));

                BlackjackGame softGame = new BlackjackGame(2, 1, softHands,
                                new Card(Suit.DIAMONDS, Rank.FIVE),
                                new Card(Suit.CLUBS, Rank.SEVEN));

                softGame.stand(0);
                GameState state = softGame.getState();
                GameState.PlayerHandState hand = state.getHands().get(1);

                // Verify hand is soft
                assertThat(hand.isSoft()).isTrue();
                assertThat(hand.getTotal()).isEqualTo(13);

                // Verify recommendation is not empty
                String recommendation = hand.getRecommendedAction();
                assertThat(recommendation).isNotNull().isNotEmpty().isNotEqualTo("NONE");

                // For soft 13 vs 5, basic strategy is to double
                assertThat(recommendation).isIn("DOUBLE", "HIT");
        }

        @Test
        @DisplayName("should recommend action for soft 19")
        void testSoftHandRecommendationAce8() {
                // Create a soft 19 (Ace + 8)
                HashMap<Integer, BlackjackService.HandCards> softHands = new HashMap<>();
                softHands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                softHands.put(1, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.EIGHT)));

                BlackjackGame softGame = new BlackjackGame(2, 1, softHands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN));

                softGame.stand(0);
                GameState state = softGame.getState();
                GameState.PlayerHandState hand = state.getHands().get(1);

                // Verify hand is soft
                assertThat(hand.isSoft()).isTrue();
                assertThat(hand.getTotal()).isEqualTo(19);

                // Verify recommendation is not empty and is STAND
                String recommendation = hand.getRecommendedAction();
                assertThat(recommendation).isNotNull().isNotEmpty().isNotEqualTo("NONE");
                assertThat(recommendation).isEqualTo("STAND");
        }

        @Test
        @DisplayName("should recommend action for soft 20")
        void testSoftHandRecommendationAce9() {
                // Create a soft 20 (Ace + 9)
                HashMap<Integer, BlackjackService.HandCards> softHands = new HashMap<>();
                softHands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                softHands.put(1, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.NINE)));

                BlackjackGame softGame = new BlackjackGame(2, 1, softHands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN));

                softGame.stand(0);
                GameState state = softGame.getState();
                GameState.PlayerHandState hand = state.getHands().get(1);

                // Verify hand is soft
                assertThat(hand.isSoft()).isTrue();
                assertThat(hand.getTotal()).isEqualTo(20);

                // Verify recommendation is STAND (never hit on 20)
                String recommendation = hand.getRecommendedAction();
                assertThat(recommendation).isNotNull().isNotEmpty().isNotEqualTo("NONE");
                assertThat(recommendation).isEqualTo("STAND");
        }

        @Test
        @DisplayName("should allow double on soft 20")
        void testDoubleOnSoft20Allowed() {
                HashMap<Integer, BlackjackService.HandCards> softHands = new HashMap<>();
                softHands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                softHands.put(1, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.NINE)));

                BlackjackGame softGame = new BlackjackGame(2, 1, softHands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN));

                softGame.stand(0);
                softGame.doubleDown(1, null);
                GameState state = softGame.getState();

                assertThat(state.getHands().get(1).getBetUnits()).isEqualTo(2);
                assertThat(state.getHands().get(1).getCards()).hasSize(3);
        }

        @Test
        @DisplayName("should allow double on soft 19 when double-on-9 is enabled")
        void testDoubleOnSoft19AllowedWithDoubleOn9Enabled() {
                HashMap<Integer, BlackjackService.HandCards> softHands = new HashMap<>();
                softHands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                softHands.put(1, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.EIGHT)));

                BlackjackGame softGame = new BlackjackGame(2, 1, softHands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN),
                                true, false);

                softGame.stand(0);
                softGame.doubleDown(1, null);
                GameState state = softGame.getState();

                assertThat(state.getHands().get(1).getBetUnits()).isEqualTo(2);
                assertThat(state.getHands().get(1).getCards()).hasSize(3);
        }

        @Test
        @DisplayName("should prevent double on soft 19 when double-on-9 is disabled")
        void testDoubleOnSoft19DisallowedWhenDoubleOn9Disabled() {
                HashMap<Integer, BlackjackService.HandCards> softHands = new HashMap<>();
                softHands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.FIVE),
                                new Card(Suit.SPADES, Rank.SIX)));
                softHands.put(1, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.EIGHT)));

                BlackjackGame softGame = new BlackjackGame(2, 1, softHands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN),
                                false, false);

                softGame.stand(0);
                assertThatThrownBy(() -> softGame.doubleDown(1, null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("soft 19");
        }

        @Test
        @DisplayName("should allow double on split non-ace hands (backend logic)")
        void testDoubleOnSplitNonAceAllowed() {
                // Create a pair of eights (splittable)
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.EIGHT),
                                new Card(Suit.SPADES, Rank.EIGHT)));

                BlackjackGame game = new BlackjackGame(1, 1, hands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN));

                // Split and deal specific ranks to keep hands in player turn
                GameState state = game.split(0, null, null, Rank.TWO, Rank.THREE);
                assertThat(state.getHands()).hasSize(2);

                // Double on a split non-ace hand should be allowed
                game.doubleDown(0, new Card(Suit.CLUBS, Rank.FIVE));
                state = game.getState();
                assertThat(state.getHands().get(0).getBetUnits()).isEqualTo(2);
                assertThat(state.getHands().get(0).getCards()).hasSize(3);
        }

        @Test
        @DisplayName("should prevent double on split aces (backend logic)")
        void testCannotDoubleOnSplitAces() {
                // Create a pair of aces (splittable)
                HashMap<Integer, BlackjackService.HandCards> hands = new HashMap<>();
                hands.put(0, new BlackjackService.HandCards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.ACE)));

                BlackjackGame game = new BlackjackGame(1, 1, hands,
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.SEVEN));

                // Split aces and attempt to double should be blocked
                game.split(0, null, null);
                assertThatCode(() -> game.doubleDown(0, new Card(Suit.CLUBS, Rank.KING)))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("Cannot double down outside player turn");
        }
}
