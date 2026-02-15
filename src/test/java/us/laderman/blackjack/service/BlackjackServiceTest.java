package us.laderman.blackjack.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import us.laderman.blackjack.model.GameState;

import java.util.HashMap;
import java.util.UUID;

@DisplayName("BlackjackService Unit Tests")
class BlackjackServiceTest {

    private BlackjackService service;

    @BeforeEach
    void setUp() {
        service = new BlackjackService();
    }

    @Test
    @DisplayName("should start a game with single hand")
    void testStartGameSingleHand() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state).isNotNull();
        assertThat(state.getHands()).hasSize(1);
        assertThat(state.getId()).isNotBlank();
    }

    @Test
    @DisplayName("should start a game with multiple hands")
    void testStartGameMultipleHands() {
        GameState state = service.startGame(3, new HashMap<>());

        assertThat(state).isNotNull();
        assertThat(state.getHands()).hasSize(3);
    }

    @Test
    @DisplayName("should have dealer state after starting game")
    void testGameHasDealerState() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state.getDealer()).isNotNull();
        assertThat(state.getDealer().getCards()).isNotEmpty();
    }

    @Test
    @DisplayName("should track running and true count")
    void testCountTracking() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state.getRunningCount()).isNotNull();
        assertThat(state.getTrueCount()).isNotNull();
    }

    @Test
    @DisplayName("should initially be in PLAYER_TURN phase")
    void testInitialPhase() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state.getPhase()).isEqualTo("PLAYER_TURN");
    }

    @Test
    @DisplayName("should provide game ID as UUID string")
    void testGameIdFormat() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state.getId()).isNotBlank();
        // Verify it's a valid UUID format
        UUID.fromString(state.getId());
    }

    @Test
    @DisplayName("should provide decks remaining info")
    void testDecksRemainingInfo() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state.getDecksRemaining()).isGreaterThan(0);
    }

    @Test
    @DisplayName("should suggest a bet")
    void testSuggestedBet() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state.getSuggestedBetUnits()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("should provide game note")
    void testGameNote() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state.getNote()).isNotNull();
    }

    @Test
    @DisplayName("should hit on a hand and return updated state")
    void testHitAction() {
        GameState startedState = service.startGame(1, new HashMap<>());
        UUID gameId = UUID.fromString(startedState.getId());
        int initialCardCount = startedState.getHands().get(0).getCards().size();

        GameState hitState = service.hit(gameId, 0);

        assertThat(hitState.getHands().get(0).getCards())
                .hasSizeGreaterThan(initialCardCount);
    }

    @Test
    @DisplayName("should stand on a hand")
    void testStandAction() {
        GameState startedState = service.startGame(1, new HashMap<>());
        UUID gameId = UUID.fromString(startedState.getId());

        try {
            GameState standState = service.stand(gameId, 0);

            assertThat(standState).isNotNull();
            assertThat(standState.getPhase()).isNotNull();
        } catch (IllegalStateException e) {
            // Hand was auto-stood (e.g., blackjack), so standing again fails - this is acceptable
            assertThat(e.getMessage()).contains("Player actions are closed");
        }
    }

    @Test
    @DisplayName("should provide initial net units")
    void testNetUnits() {
        GameState state = service.startGame(1, new HashMap<>());

        assertThat(state.getNetUnits()).isNotNaN();
    }
}
