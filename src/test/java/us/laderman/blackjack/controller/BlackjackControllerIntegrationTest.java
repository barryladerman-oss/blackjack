package us.laderman.blackjack.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.GameState.DealerState;
import us.laderman.blackjack.model.GameState.PlayerHandState;
import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;
import us.laderman.blackjack.service.BlackjackService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Integration tests for BlackjackController API endpoints.
 */
@DisplayName("BlackjackController REST API Tests")
class BlackjackControllerTest {

    private BlackjackController controller;
    private TestBlackjackService mockService;

    @BeforeEach
    void setUp() {
        mockService = new TestBlackjackService();
        controller = new BlackjackController(mockService);
    }

    @Test
    @DisplayName("should handle start game endpoint")
    /**
     * Tests the start game endpoint for correct game creation and service call.
     */
    void testStartGameEndpoint() {
        // Arrange
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.start(1, 6, false, false, new HashMap<>());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        assertThat(mockService.getLastStartHands()).isEqualTo(1);
    }

    @Test
    @DisplayName("should handle state retrieval endpoint")
    /**
     * Tests the state retrieval endpoint for correct game state and service call.
     */
    void testGetStateEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.state(gameId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
    }

    @Test
    @DisplayName("should handle hit endpoint")
    /**
     * Tests the hit endpoint for correct game state and service call.
     */
    void testHitEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.hit(gameId, 0, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle stand endpoint")
    /**
     * Tests the stand endpoint for correct game state and service call.
     */
    void testStandEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.stand(gameId, 0);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle double down endpoint")
    /**
     * Tests the double down endpoint for correct game state and service call.
     */
    void testDoubleDownEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.doubleDown(gameId, 0, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle split endpoint")
    void testSplitEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.split(gameId, 0, null, null, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle split endpoint with rank-only for both cards")
    void testSplitEndpointWithRankOnly() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act - passing rank without suit
        GameState result = controller.split(gameId, 0, "FOUR", null, "FIVE", null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle split endpoint with rank-only for first card")
    void testSplitEndpointWithRankOnlyFirstCard() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.split(gameId, 0, "THREE", null, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle split endpoint with rank-only for second card")
    void testSplitEndpointWithRankOnlySecondCard() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.split(gameId, 0, null, null, "ACE", null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle insurance endpoint")
    void testInsuranceEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.insurance(gameId, 0);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle decline insurance endpoint")
    void testDeclineInsuranceEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.declineInsurance(gameId, 0);

        // Assert
        assertThat(result).isNotNull();
        assertThat(mockService.getLastGameId()).isEqualTo(UUID.fromString(gameId));
        assertThat(mockService.getLastHandIndex()).isEqualTo(0);
    }

    @Test
    @DisplayName("should support single hand game")
    void testSingleHandGame() {
        // Arrange
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.start(1, 6, false, false, new HashMap<>());

        // Assert
        assertThat(result.getHands()).hasSize(1);
    }

    @Test
    @DisplayName("should support multiple hands game")
    void testMultipleHandsGame() {
        // Arrange
        GameState mockState = new GameState(
                UUID.randomUUID().toString(),
                "PLAYER_TURN",
                new DealerState(List.of(new Card(Suit.HEARTS, Rank.SIX)), 6, false, false, false),
                List.of(
                        createMockPlayerHandState(0),
                        createMockPlayerHandState(1),
                        createMockPlayerHandState(2)),
                0, 0, 52, 0, 0, "Dealer up card 6");
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.start(3, 6, false, false, new HashMap<>());

        // Assert
        assertThat(result.getHands()).hasSize(3);
    }

    @Test
    @DisplayName("should return game details in response")
    void testGameDetailsInResponse() {
        // Arrange
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.state(UUID.randomUUID().toString());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getPhase()).isNotBlank();
        assertThat(result.getDealer()).isNotNull();
        assertThat(result.getHands()).isNotEmpty();
    }

    @Test
    @DisplayName("should include card counting info in response")
    void testCardCountingInfoInResponse() {
        // Arrange
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.state(UUID.randomUUID().toString());

        // Assert
        assertThat(result.getRunningCount()).isNotNull();
        assertThat(result.getTrueCount()).isNotNull();
        assertThat(result.getCardsRemaining()).isGreaterThan(0);
    }

    @Test
    @DisplayName("should include strategy advice in response")
    void testStrategyAdviceInResponse() {
        // Arrange
        GameState mockState = createMockGameState();
        mockService.setNextState(mockState);

        // Act
        GameState result = controller.state(UUID.randomUUID().toString());

        // Assert
        assertThat(result.getNote()).isNotNull();
    }

    // Helper method to create mock game state
    private GameState createMockGameState() {
        List<Card> dealerCards = List.of(new Card(Suit.HEARTS, Rank.SIX));
        DealerState dealerState = new DealerState(dealerCards, 6, false, false, false);

        List<PlayerHandState> hands = List.of(createMockPlayerHandState(0));

        return new GameState(
                UUID.randomUUID().toString(),
                "PLAYER_TURN",
                dealerState,
                hands,
                0,
                0.0,
                52,
                0.0,
                0.0,
                "New game");
    }

    private PlayerHandState createMockPlayerHandState(int index) {
        List<Card> cards = List.of(
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.FIVE));
        return new PlayerHandState(
                index,
                cards,
                15,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                "HIT",
                false,
                true,
                1,
                0.0);
    }

    private static final class TestBlackjackService extends BlackjackService {
        private int lastStartHands;
        private UUID lastGameId;
        private int lastHandIndex;
        private GameState nextState = createMockGameState();

        private TestBlackjackService() {
            super((state, decks) -> {
            });
        }

        public void setNextState(GameState state) {
            this.nextState = state;
        }

        public int getLastStartHands() {
            return lastStartHands;
        }

        public UUID getLastGameId() {
            return lastGameId;
        }

        public int getLastHandIndex() {
            return lastHandIndex;
        }

        @Override
        public GameState startGame(int hands, Map<String, String> allParams) {
            this.lastStartHands = hands;
            return nextState;
        }

        @Override
        public GameState getState(UUID gameId) {
            this.lastGameId = gameId;
            return nextState;
        }

        @Override
        public GameState hit(UUID gameId, int handIndex, String rank, String suit) {
            this.lastGameId = gameId;
            this.lastHandIndex = handIndex;
            return nextState;
        }

        @Override
        public GameState stand(UUID gameId, int handIndex) {
            this.lastGameId = gameId;
            this.lastHandIndex = handIndex;
            return nextState;
        }

        @Override
        public GameState doubleDown(UUID gameId, int handIndex, String rank, String suit) {
            this.lastGameId = gameId;
            this.lastHandIndex = handIndex;
            return nextState;
        }

        @Override
        public GameState split(UUID gameId, int handIndex) {
            this.lastGameId = gameId;
            this.lastHandIndex = handIndex;
            return nextState;
        }

        @Override
        public GameState split(UUID gameId, int handIndex, String card1Rank, String card1Suit,
                String card2Rank, String card2Suit) {
            this.lastGameId = gameId;
            this.lastHandIndex = handIndex;
            return nextState;
        }

        @Override
        public GameState insurance(UUID gameId, int handIndex) {
            this.lastGameId = gameId;
            this.lastHandIndex = handIndex;
            return nextState;
        }

        @Override
        public GameState declineInsurance(UUID gameId, int handIndex) {
            this.lastGameId = gameId;
            this.lastHandIndex = handIndex;
            return nextState;
        }

        private static GameState createMockGameState() {
            List<Card> dealerCards = List.of(new Card(Suit.HEARTS, Rank.SIX));
            DealerState dealerState = new DealerState(dealerCards, 6, false, false, false);
            List<PlayerHandState> hands = List.of(new PlayerHandState(0, List.of(
                    new Card(Suit.HEARTS, Rank.TEN), new Card(Suit.DIAMONDS, Rank.FIVE)),
                    15, false, false, false, false, false, false, null, "HIT", false,
                    true, 1, 0.0));
            return new GameState(UUID.randomUUID().toString(), "PLAYER_TURN", dealerState,
                    hands, 0, 0.0, 52, 0.0, 0.0, "New game");
        }
    }
}
