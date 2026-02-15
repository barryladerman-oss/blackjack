package us.laderman.blackjack.restclient;

import us.laderman.blackjack.model.GameState;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs predefined test scenarios against the Blackjack REST API.
 * Useful for validating API behavior across different game flows.
 */
public class TestScenarioRunner {
    private final BlackjackRestClient client;
    private final List<TestResult> results;
    private int scenarioCount;
    private int passedCount;
    private int failedCount;

    public TestScenarioRunner(String baseUrl) {
        this.client = new BlackjackRestClient(baseUrl);
        this.results = new ArrayList<>();
        this.scenarioCount = 0;
        this.passedCount = 0;
        this.failedCount = 0;
    }

    public static void main(String[] args) {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        TestScenarioRunner runner = new TestScenarioRunner(baseUrl);
        runner.runAllScenarios();
        runner.printReport();
    }

    public void runAllScenarios() {
        System.out.println("=== Running Blackjack REST API Test Scenarios ===\n");

        runScenario("Scenario 1: Basic Game Flow", this::testBasicGameFlow);
        runScenario("Scenario 2: Multiple Hands", this::testMultipleHands);
        runScenario("Scenario 3: Hit and Stand", this::testHitAndStand);
        runScenario("Scenario 4: Invalid Game ID", this::testInvalidGameId);
        runScenario("Scenario 5: Invalid Hand Number", this::testInvalidHandNumber);
        runScenario("Scenario 6: Double Down", this::testDoubleDown);
        runScenario("Scenario 7: Split Hand", this::testSplit);
        runScenario("Scenario 8: Insurance Operations", this::testInsurance);
        runScenario("Scenario 9: Get Game State", this::testGameState);
        runScenario("Scenario 10: Sequential Operations", this::testSequentialOperations);
    }

    private void runScenario(String name, Runnable test) {
        scenarioCount++;
        System.out.println(name);
        System.out.println("-".repeat(50));

        try {
            test.run();
            passedCount++;
            results.add(new TestResult(name, true, null));
            System.out.println("✓ PASSED\n");
        } catch (AssertionError e) {
            failedCount++;
            results.add(new TestResult(name, false, e.getMessage()));
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
        } catch (Exception e) {
            failedCount++;
            results.add(new TestResult(name, false, e.toString()));
            System.out.println("✗ ERROR: " + e.toString() + "\n");
        }
    }

    // Test Scenarios
    private void testBasicGameFlow() {
        ApiResponse<GameState> start = client.startGame(1);
        assertTrue(start.isSuccess(), "Failed to start game");
        assertTrue(start.getStatusCode() == 200, "Expected status 200");

        String gameId = start.getData().getId();
        assertNotNull(gameId, "Game ID is null");

        ApiResponse<GameState> state = client.getGameState(gameId);
        assertTrue(state.isSuccess(), "Failed to get game state");
        assertEquals(gameId, state.getData().getId(), "Game IDs don't match");
    }

    private void testMultipleHands() {
        ApiResponse<GameState> response = client.startGame(3);
        assertTrue(response.isSuccess(), "Failed to start game with 3 hands");

        GameState state = response.getData();
        assertEquals(3, state.getHands().size(), "Expected 3 hands");
    }

    private void testHitAndStand() {
        ApiResponse<GameState> start = client.startGame(1);
        String gameId = start.getData().getId();

        ApiResponse<GameState> hit = client.hit(gameId, 0);
        assertTrue(hit.isSuccess(), "Failed to hit");

        ApiResponse<GameState> stand = client.stand(gameId, 0);
        assertTrue(stand.isSuccess(), "Failed to stand");
    }

    private void testInvalidGameId() {
        ApiResponse<GameState> response = client.getGameState("invalid-uuid");
        assertTrue(!response.isSuccess(), "Should fail with invalid UUID");
        assertEquals(400, response.getStatusCode(), "Expected 400 status");
    }

    private void testInvalidHandNumber() {
        ApiResponse<GameState> start = client.startGame(1);
        String gameId = start.getData().getId();

        ApiResponse<GameState> response = client.hit(gameId, 999);
        assertFalse(response.isSuccess(), "Should fail with invalid hand number");
    }

    private void testDoubleDown() {
        ApiResponse<GameState> start = client.startGame(1);
        String gameId = start.getData().getId();

        ApiResponse<GameState> response = client.doubleDown(gameId, 0);
        // May succeed or fail depending on game state, but shouldn't error
        assertTrue(response.getStatusCode() != 500, "Server error on double down");
    }

    private void testSplit() {
        ApiResponse<GameState> start = client.startGame(1);
        String gameId = start.getData().getId();

        ApiResponse<GameState> response = client.split(gameId, 0);
        // May succeed or fail (409), but shouldn't be 500
        assertTrue(response.getStatusCode() != 500, "Server error on split");
    }

    private void testInsurance() {
        ApiResponse<GameState> start = client.startGame(1);
        String gameId = start.getData().getId();

        ApiResponse<GameState> insResponse = client.insurance(gameId, 0);
        // May succeed or fail depending on game state
        assertTrue(insResponse.getStatusCode() != 500, "Server error on insurance");

        ApiResponse<GameState> declineResponse = client.declineInsurance(gameId, 0);
        assertTrue(declineResponse.getStatusCode() != 500, "Server error on decline insurance");
    }

    private void testGameState() {
        ApiResponse<GameState> start = client.startGame(2);
        GameState initial = start.getData();
        String gameId = initial.getId();

        ApiResponse<GameState> state = client.getGameState(gameId);
        assertTrue(state.isSuccess(), "Failed to get game state");

        GameState retrieved = state.getData();
        assertEquals(gameId, retrieved.getId(), "Game IDs don't match");
        assertEquals(2, retrieved.getHands().size(), "Expected 2 hands");
    }

    private void testSequentialOperations() {
        ApiResponse<GameState> start = client.startGame(1);
        String gameId = start.getData().getId();

        // Sequence of operations
        ApiResponse<GameState> s1 = client.hit(gameId, 0);
        assertTrue(s1.isSuccess(), "First hit failed");

        ApiResponse<GameState> s2 = client.getGameState(gameId);
        assertTrue(s2.isSuccess(), "Get state failed");

        ApiResponse<GameState> s3 = client.hit(gameId, 0);
        assertTrue(s3.isSuccess(), "Second hit failed");

        ApiResponse<GameState> s4 = client.stand(gameId, 0);
        assertTrue(s4.isSuccess(), "Stand failed");

        ApiResponse<GameState> s5 = client.getGameState(gameId);
        assertTrue(s5.isSuccess(), "Final state retrieval failed");
    }

    // Helper assertions
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    private void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    private void printReport() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("=== TEST REPORT ===");
        System.out.println("=".repeat(50));
        System.out.println("Total Scenarios: " + scenarioCount);
        System.out.println("Passed: " + passedCount);
        System.out.println("Failed: " + failedCount);
        System.out.println("Success Rate: " + (scenarioCount > 0 ? (passedCount * 100 / scenarioCount) : 0) + "%");
        System.out.println("=".repeat(50));

        if (failedCount > 0) {
            System.out.println("\nFailed Tests:");
            results.stream()
                    .filter(r -> !r.passed)
                    .forEach(r -> System.out.println("  - " + r.name + ": " + r.errorMessage));
        }
    }

    private static class TestResult {
        String name;
        boolean passed;
        String errorMessage;

        TestResult(String name, boolean passed, String errorMessage) {
            this.name = name;
            this.passed = passed;
            this.errorMessage = errorMessage;
        }
    }
}
