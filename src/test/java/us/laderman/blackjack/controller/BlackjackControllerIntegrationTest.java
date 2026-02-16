package us.laderman.blackjack.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import java.util.List;
import java.util.UUID;

@DisplayName("BlackjackController REST API Tests")
class BlackjackControllerTest {

    private BlackjackController controller;
    private BlackjackService mockService;

    @BeforeEach
    void setUp() {
        mockService = mock(BlackjackService.class);
        controller = new BlackjackController(mockService);
    }

    @Test
    @DisplayName("should handle start game endpoint")
    void testStartGameEndpoint() {
        // Arrange
        GameState mockState = createMockGameState();
        when(mockService.startGame(anyInt(), any())).thenReturn(mockState);

        // Act
        GameState result = controller.start(1, new java.util.HashMap<>());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        verify(mockService).startGame(eq(1), any());
    }

    @Test
    @DisplayName("should handle state retrieval endpoint")
    void testGetStateEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.getState(any(UUID.class))).thenReturn(mockState);

        // Act
        GameState result = controller.state(gameId);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).getState(any(UUID.class));
    }

    @Test
    @DisplayName("should handle hit endpoint")
    void testHitEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.hit(any(UUID.class), anyInt(), any(), any())).thenReturn(mockState);

        // Act
        GameState result = controller.hit(gameId, 0, null, null);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).hit(any(UUID.class), eq(0), any(), any());
    }

    @Test
    @DisplayName("should handle stand endpoint")
    void testStandEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.stand(any(UUID.class), anyInt())).thenReturn(mockState);

        // Act
        GameState result = controller.stand(gameId, 0);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).stand(any(UUID.class), eq(0));
    }

    @Test
    @DisplayName("should handle double down endpoint")
    void testDoubleDownEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.doubleDown(any(UUID.class), anyInt(), any(), any())).thenReturn(mockState);

        // Act
        GameState result = controller.doubleDown(gameId, 0, null, null);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).doubleDown(any(UUID.class), eq(0), any(), any());
    }

    @Test
    @DisplayName("should handle split endpoint")
    void testSplitEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        // Mock both split method signatures
        when(mockService.split(any(UUID.class), anyInt())).thenReturn(mockState);
        when(mockService.split(any(UUID.class), anyInt(), any(), any(), any(), any()))
                .thenReturn(mockState);

        // Act
        GameState result = controller.split(gameId, 0, null, null, null, null);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).split(any(UUID.class), eq(0));
    }

    @Test
    @DisplayName("should handle split endpoint with rank-only for both cards")
    void testSplitEndpointWithRankOnly() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.split(any(UUID.class), anyInt(), any(), any(), any(), any()))
                .thenReturn(mockState);

        // Act - passing rank without suit
        GameState result = controller.split(gameId, 0, "FOUR", null, "FIVE", null);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).split(any(UUID.class), eq(0), eq("FOUR"), eq(null), eq("FIVE"), eq(null));
    }

    @Test
    @DisplayName("should handle split endpoint with rank-only for first card")
    void testSplitEndpointWithRankOnlyFirstCard() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.split(any(UUID.class), anyInt(), any(), any(), any(), any()))
                .thenReturn(mockState);

        // Act
        GameState result = controller.split(gameId, 0, "THREE", null, null, null);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).split(any(UUID.class), eq(0), eq("THREE"), eq(null), eq(null), eq(null));
    }

    @Test
    @DisplayName("should handle split endpoint with rank-only for second card")
    void testSplitEndpointWithRankOnlySecondCard() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.split(any(UUID.class), anyInt(), any(), any(), any(), any()))
                .thenReturn(mockState);

        // Act
        GameState result = controller.split(gameId, 0, null, null, "ACE", null);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).split(any(UUID.class), eq(0), eq(null), eq(null), eq("ACE"), eq(null));
    }

    @Test
    @DisplayName("should handle insurance endpoint")
    void testInsuranceEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.insurance(any(UUID.class), anyInt())).thenReturn(mockState);

        // Act
        GameState result = controller.insurance(gameId, 0);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).insurance(any(UUID.class), eq(0));
    }

    @Test
    @DisplayName("should handle decline insurance endpoint")
    void testDeclineInsuranceEndpoint() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        GameState mockState = createMockGameState();
        when(mockService.declineInsurance(any(UUID.class), anyInt())).thenReturn(mockState);

        // Act
        GameState result = controller.declineInsurance(gameId, 0);

        // Assert
        assertThat(result).isNotNull();
        verify(mockService).declineInsurance(any(UUID.class), eq(0));
    }

    @Test
    @DisplayName("should support single hand game")
    void testSingleHandGame() {
        // Arrange
        GameState mockState = createMockGameState();
        when(mockService.startGame(eq(1), any())).thenReturn(mockState);

        // Act
        GameState result = controller.start(1, new java.util.HashMap<>());

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
                0, 0, 1.0, 0, 0, 1, "Dealer up card 6");
        when(mockService.startGame(eq(3), any())).thenReturn(mockState);

        // Act
        GameState result = controller.start(3, new java.util.HashMap<>());

        // Assert
        assertThat(result.getHands()).hasSize(3);
    }

    @Test
    @DisplayName("should return game details in response")
    void testGameDetailsInResponse() {
        // Arrange
        GameState mockState = createMockGameState();
        when(mockService.getState(any(UUID.class))).thenReturn(mockState);

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
        when(mockService.getState(any(UUID.class))).thenReturn(mockState);

        // Act
        GameState result = controller.state(UUID.randomUUID().toString());

        // Assert
        assertThat(result.getRunningCount()).isNotNull();
        assertThat(result.getTrueCount()).isNotNull();
        assertThat(result.getDecksRemaining()).isGreaterThan(0);
    }

    @Test
    @DisplayName("should include betting suggestion in response")
    void testBettingSuggestionInResponse() {
        // Arrange
        GameState mockState = createMockGameState();
        when(mockService.getState(any(UUID.class))).thenReturn(mockState);

        // Act
        GameState result = controller.state(UUID.randomUUID().toString());

        // Assert
        assertThat(result.getSuggestedBetUnits()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("should include strategy advice in response")
    void testStrategyAdviceInResponse() {
        // Arrange
        GameState mockState = createMockGameState();
        when(mockService.getState(any(UUID.class))).thenReturn(mockState);

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
                1.0,
                0.0,
                0.0,
                1,
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
                1);
    }
}
