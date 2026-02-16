package us.laderman.blackjack.restclient;

import us.laderman.blackjack.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * REST API integration tests for the Blackjack REST endpoints.
 * Tests basic game flow and error scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Blackjack REST API Tests")
public class BlackjackRestApiTest {

    @LocalServerPort
    private int port;

    private BlackjackRestClient client;
    private String baseUrl;

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port;
        client = new BlackjackRestClient(baseUrl);
    }

    @Test
    @DisplayName("Should start a new game with one hand")
    public void testStartGame() {
        ApiResponse<GameState> response = client.startGame(1);

        ApiAssertions.assertSuccess(response);
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertHasData(response);

        GameState state = response.getData();
        ApiAssertions.assertValidGameId(state);
        ApiAssertions.assertGameHasHands(state, 1);
    }

    @Test
    @DisplayName("Should start a game with multiple hands")
    public void testStartGameMultipleHands() {
        ApiResponse<GameState> response = client.startGame(3);

        ApiAssertions.assertSuccess(response);
        GameState state = response.getData();
        ApiAssertions.assertGameHasHands(state, 3);
    }

    @Test
    @DisplayName("Should get game state")
    public void testGetGameState() {
        // Start a game
        ApiResponse<GameState> startResponse = client.startGame(1);
        ApiAssertions.assertSuccess(startResponse);
        String gameId = startResponse.getData().getId();

        // Get the state
        ApiResponse<GameState> stateResponse = client.getGameState(gameId);

        ApiAssertions.assertSuccess(stateResponse);
        ApiAssertions.assertHasData(stateResponse);
        GameState state = stateResponse.getData();
        org.junit.jupiter.api.Assertions.assertEquals(gameId, state.getId());
    }

    @Test
    @DisplayName("Should handle invalid game ID with 400 error")
    public void testInvalidGameId() {
        ApiResponse<GameState> response = client.getGameState("invalid-id");

        ApiAssertions.assertFailure(response);
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @DisplayName("Should handle non-existent game with 404 error")
    public void testNonExistentGameId() {
        String validUuid = "00000000-0000-0000-0000-000000000000";
        ApiResponse<GameState> response = client.getGameState(validUuid);

        ApiAssertions.assertFailure(response);
        ApiAssertions.assertStatusCode(response, 404);
    }

    @Test
    @DisplayName("Should allow hitting on a hand")
    public void testHit() {
        ApiResponse<GameState> startResponse = client.startGame(1);
        ApiAssertions.assertSuccess(startResponse);
        String gameId = startResponse.getData().getId();

        ApiResponse<GameState> hitResponse = client.hit(gameId, 0);

        ApiAssertions.assertSuccess(hitResponse);
        ApiAssertions.assertHasData(hitResponse);
    }

    @Test
    @DisplayName("Should allow standing on a hand")
    public void testStand() {
        ApiResponse<GameState> startResponse = client.startGame(1);
        ApiAssertions.assertSuccess(startResponse);
        String gameId = startResponse.getData().getId();

        // Check if insurance needs to be resolved first
        GameState state = startResponse.getData();
        if (state.getDealer() != null && state.getDealer().getCards() != null
                && !state.getDealer().getCards().isEmpty()) {
            var firstCard = state.getDealer().getCards().get(0);
            boolean dealerShowsAce = firstCard.getRank().name().equals("ACE");

            if (dealerShowsAce) {
                // Decline insurance before attempting stand
                client.declineInsurance(gameId, 0);
            }
        }

        ApiResponse<GameState> standResponse = client.stand(gameId, 0);

        ApiAssertions.assertSuccess(standResponse);
    }

    @Test
    @DisplayName("Should allow doubling down on a hand")
    public void testDoubleDown() {
        ApiResponse<GameState> startResponse = client.startGame(1);
        ApiAssertions.assertSuccess(startResponse);
        String gameId = startResponse.getData().getId();

        ApiResponse<GameState> doubleResponse = client.doubleDown(gameId, 0);

        ApiAssertions.assertSuccess(doubleResponse);
    }

    @Test
    @DisplayName("Should handle split operations")
    public void testSplit() {
        ApiResponse<GameState> startResponse = client.startGame(1);
        ApiAssertions.assertSuccess(startResponse);
        String gameId = startResponse.getData().getId();

        // Check if insurance needs to be resolved first
        GameState state = startResponse.getData();
        if (state.getDealer() != null && state.getDealer().getCards() != null
                && !state.getDealer().getCards().isEmpty()) {
            var firstCard = state.getDealer().getCards().get(0);
            boolean dealerShowsAce = firstCard.getRank().name().equals("ACE");

            if (dealerShowsAce) {
                // Decline insurance before attempting split
                client.declineInsurance(gameId, 0);
            }
        }

        // Attempt split (may fail if hand can't be split, but should not error)
        ApiResponse<GameState> splitResponse = client.split(gameId, 0);

        // Either success, conflict (can't split), or bad request (not a pair)
        org.junit.jupiter.api.Assertions.assertTrue(
                splitResponse.isSuccess() || splitResponse.getStatusCode() == 409
                        || splitResponse.getStatusCode() == 400,
                "Expected success, CONFLICT, or BAD_REQUEST status, got: " + splitResponse.getStatusCode());
    }

    @Test
    @DisplayName("Should handle invalid hand number with error")
    public void testInvalidHandNumber() {
        ApiResponse<GameState> startResponse = client.startGame(1);
        ApiAssertions.assertSuccess(startResponse);
        String gameId = startResponse.getData().getId();

        ApiResponse<GameState> hitResponse = client.hit(gameId, 999);

        ApiAssertions.assertFailure(hitResponse);
    }

    @Test
    @DisplayName("Should complete a simple game sequence")
    public void testGameSequence() {
        // Start game
        ApiResponse<GameState> startResponse = client.startGame(1);
        ApiAssertions.assertSuccess(startResponse);
        GameState initial = startResponse.getData();
        String gameId = initial.getId();

        // Check if insurance needs to be resolved first
        if (initial.getDealer() != null && initial.getDealer().getCards() != null
                && !initial.getDealer().getCards().isEmpty()) {
            var firstCard = initial.getDealer().getCards().get(0);
            boolean dealerShowsAce = firstCard.getRank().name().equals("ACE");

            if (dealerShowsAce) {
                // Decline insurance before hit
                client.declineInsurance(gameId, 0);
            }
        }

        // Hit
        ApiResponse<GameState> hitResponse = client.hit(gameId, 0);
        ApiAssertions.assertSuccess(hitResponse);

        // Check again if insurance needs to be resolved after hit (in case state
        // changed)
        GameState afterHit = hitResponse.getData();
        if (afterHit.getDealer() != null && afterHit.getDealer().getCards() != null
                && !afterHit.getDealer().getCards().isEmpty() && afterHit.getHands() != null) {
            var firstCard = afterHit.getDealer().getCards().get(0);
            boolean dealerShowsAce = firstCard.getRank().name().equals("ACE");

            if (dealerShowsAce) {
                // Check if any hands still need insurance decision
                for (int i = 0; i < afterHit.getHands().size(); i++) {
                    var hand = afterHit.getHands().get(i);
                    if (hand.getCards().size() == 2 && !hand.isInsured() && !hand.isInsuranceDeclined()) {
                        client.declineInsurance(gameId, i);
                    }
                }
            }
        }

        // Stand
        ApiResponse<GameState> standResponse = client.stand(gameId, 0);
        ApiAssertions.assertSuccess(standResponse);

        // Get final state
        ApiResponse<GameState> finalResponse = client.getGameState(gameId);
        ApiAssertions.assertSuccess(finalResponse);
        GameState finalState = finalResponse.getData();
        org.junit.jupiter.api.Assertions.assertNotNull(finalState);
    }
}
