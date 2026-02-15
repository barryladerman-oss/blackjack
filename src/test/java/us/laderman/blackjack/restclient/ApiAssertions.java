package us.laderman.blackjack.restclient;

import us.laderman.blackjack.model.GameState;
import org.junit.jupiter.api.Assertions;

/**
 * Assertion helpers for API response validation.
 */
public class ApiAssertions {

    /**
     * Assert that the API response was successful.
     */
    public static <T> void assertSuccess(ApiResponse<T> response) {
        Assertions.assertTrue(response.isSuccess(),
                "Expected successful response. Status: " + response.getStatusCode() +
                        ", Error: " + response.getErrorMessage());
    }

    /**
     * Assert that the API response has a specific status code.
     */
    public static <T> void assertStatusCode(ApiResponse<T> response, int expectedCode) {
        Assertions.assertEquals(expectedCode, response.getStatusCode(),
                "Expected status code " + expectedCode +
                        ", but got " + response.getStatusCode() +
                        ". Error: " + response.getErrorMessage());
    }

    /**
     * Assert that the API response contains data.
     */
    public static <T> void assertHasData(ApiResponse<T> response) {
        Assertions.assertNotNull(response.getData(),
                "Expected response to contain data, but got null");
    }

    /**
     * Assert that the API response failed.
     */
    public static <T> void assertFailure(ApiResponse<T> response) {
        Assertions.assertFalse(response.isSuccess(),
                "Expected failed response, but request succeeded");
    }

    /**
     * Assert that the game state has expected number of hands.
     */
    public static void assertGameHasHands(GameState gameState, int expectedCount) {
        Assertions.assertNotNull(gameState, "GameState is null");
        Assertions.assertNotNull(gameState.getHands(), "Hands list is null");
        Assertions.assertEquals(expectedCount, gameState.getHands().size(),
                "Expected " + expectedCount + " hands, but got " + gameState.getHands().size());
    }

    /**
     * Assert that the game is in a specific phase.
     */
    public static void assertGamePhase(GameState gameState, String expectedPhase) {
        Assertions.assertNotNull(gameState, "GameState is null");
        Assertions.assertEquals(expectedPhase, gameState.getPhase(),
                "Expected phase '" + expectedPhase + "', but got '" + gameState.getPhase() + "'");
    }

    /**
     * Assert that a game ID is valid (not null, non-empty UUID format).
     */
    public static void assertValidGameId(GameState gameState) {
        Assertions.assertNotNull(gameState, "GameState is null");
        Assertions.assertNotNull(gameState.getId(), "GameId is null");
        Assertions.assertTrue(gameState.getId().length() > 0, "GameId is empty");
        // Simple UUID format check
        Assertions.assertTrue(
                gameState.getId().matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"),
                "GameId does not match UUID format: " + gameState.getId());
    }

    /**
     * Assert that dealer state exists in game.
     */
    public static void assertDealerCards(GameState gameState) {
        Assertions.assertNotNull(gameState, "GameState is null");
        Assertions.assertNotNull(gameState.getDealer(), "Dealer state is null");
        Assertions.assertNotNull(gameState.getDealer().getCards(), "Dealer cards is null");
    }
}
