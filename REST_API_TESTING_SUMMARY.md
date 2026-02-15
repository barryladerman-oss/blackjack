# REST API Testing Application - Implementation Summary

## Overview

A complete REST API testing framework has been created for the Blackjack Spring Boot REST API. This framework provides multiple tools and utilities for comprehensive API testing and validation.

## What Was Created

### 1. **BlackjackRestClient** (`BlackjackRestClient.java`)
- Low-level HTTP client for all REST API endpoints
- Methods for all game operations (start, hit, stand, double, split, insurance, etc.)
- Automatic URL encoding and error handling
- Returns `ApiResponse<T>` wrapper with status code, data, and error messages

**Key Methods:**
- `startGame(int hands)` - Initialize a new game
- `getGameState(String gameId)` - Retrieve game state
- `hit(String gameId, int hand)` - Hit action
- `stand(String gameId, int hand)` - Stand action
- `doubleDown(String gameId, int hand)` - Double down action
- `split(String gameId, int hand)` - Split hand
- `insurance(String gameId, int hand)` - Insurance action
- `declineInsurance(String gameId, int hand)` - Decline insurance

### 2. **ApiResponse<T>** (`ApiResponse.java`)
- Generic response wrapper class
- Properties: `statusCode`, `data`, `errorMessage`, `success`
- Handles both successful and failed responses
- Includes toString() for easy debugging

### 3. **ApiAssertions** (`ApiAssertions.java`)
- Test assertion helpers for API response validation
- Methods for asserting success, status codes, data presence
- Game state validation assertions
- UUID format validation

**Key Methods:**
- `assertSuccess(ApiResponse<T> response)` - Verify successful response
- `assertStatusCode(ApiResponse<T> response, int code)` - Check HTTP status
- `assertHasData(ApiResponse<T> response)` - Verify response contains data
- `assertGameHasHands(GameState state, int count)` - Verify hand count
- `assertGamePhase(GameState state, String phase)` - Check game phase
- `assertValidGameId(GameState state)` - Validate UUID format

### 4. **BlackjackRestApiTest** (`BlackjackRestApiTest.java`)
- JUnit 5 integration test suite with 12 comprehensive test cases
- Tests all API operations and error scenarios
- Uses Spring Boot test context for actual HTTP testing
- Test cases include:
  - Starting games with single/multiple hands
  - Getting game state
  - Hit, stand, double down operations
  - Split and insurance operations
  - Error handling (invalid IDs, non-existent games, invalid hands)
  - Complete game sequences

**Running Tests:**
```bash
mvn test -Dtest=BlackjackRestApiTest
```

### 5. **BlackjackRestTestApplication** (`BlackjackRestTestApplication.java`)
- Interactive CLI application for manual REST API testing
- Menu-driven interface with 10 options
- Allows playing full games through REST API
- Pretty-prints JSON responses
- Tracks current game ID for sequential operations

**Running the Application:**
```bash
mvn exec:java -Dexec.mainClass="us.laderman.blackjack.restclient.BlackjackRestTestApplication" -Dexec.args="http://localhost:8080"
```

**Menu Options:**
1. Start new game
2. Get game state
3. Hit
4. Stand
5. Double down
6. Split
7. Take insurance
8. Decline insurance
9. Game history
0. Exit

### 6. **TestScenarioRunner** (`TestScenarioRunner.java`)
- Automated scenario runner for predefined test sequences
- Executes 10 different test scenarios
- Generates detailed test reports with pass/fail statistics
- Useful for regression testing and API validation

**Test Scenarios:**
1. Basic game flow
2. Multiple hands
3. Hit and stand
4. Invalid game ID handling
5. Invalid hand number
6. Double down
7. Split hand
8. Insurance operations
9. Game state retrieval
10. Sequential operations

**Running Scenarios:**
```bash
mvn exec:java -Dexec.mainClass="us.laderman.blackjack.restclient.TestScenarioRunner" -Dexec.args="http://localhost:8080"
```

**Output Example:**
```
=== Running Blackjack REST API Test Scenarios ===

Scenario 1: Basic Game Flow
--------------------------------------------------
✓ PASSED

Scenario 2: Multiple Hands
--------------------------------------------------
✓ PASSED

...

==================================================
=== TEST REPORT ===
==================================================
Total Scenarios: 10
Passed: 9
Failed: 1
Success Rate: 90%
==================================================
```

## File Structure

```
src/test/java/us/laderman/blackjack/restclient/
├── BlackjackRestClient.java           # HTTP client (217 lines)
├── ApiResponse.java                   # Response wrapper (36 lines)
├── ApiAssertions.java                 # Assertion helpers (70 lines)
├── BlackjackRestApiTest.java          # JUnit tests (196 lines)
├── BlackjackRestTestApplication.java  # Interactive CLI (272 lines)
├── TestScenarioRunner.java            # Scenario runner (338 lines)
└── README.md                          # Comprehensive documentation
```

## Key Features

✅ **Complete API Coverage** - All endpoints covered  
✅ **Multiple Testing Approaches** - Unit tests, integration tests, CLI, scenarios  
✅ **Error Handling** - Comprehensive error handling and validation  
✅ **Checkstyle Compliant** - Follows project code standards  
✅ **Well Documented** - Detailed README and inline documentation  
✅ **Extensible** - Easy to add new test cases or scenarios  
✅ **Production Ready** - Can be used for QA and regression testing  

## Usage Examples

### Example 1: Using the Client Programmatically
```java
BlackjackRestClient client = new BlackjackRestClient("http://localhost:8080");

// Start a game
ApiResponse<GameState> response = client.startGame(1);
if (response.isSuccess()) {
    String gameId = response.getData().getId();
    System.out.println("Game started: " + gameId);
    
    // Perform actions
    client.hit(gameId, 0);
    client.hit(gameId, 0);
    client.stand(gameId, 0);
}
```

### Example 2: Using Assertions
```java
ApiResponse<GameState> response = client.startGame(2);

ApiAssertions.assertSuccess(response);
ApiAssertions.assertStatusCode(response, 200);
ApiAssertions.assertGameHasHands(response.getData(), 2);
```

### Example 3: Error Handling
```java
ApiResponse<GameState> response = client.getGameState("invalid-id");

if (!response.isSuccess()) {
    System.out.println("Status: " + response.getStatusCode());
    System.out.println("Error: " + response.getErrorMessage());
}
```

## REST Endpoints Tested

| Endpoint | Method | Parameters | Response |
|----------|--------|-----------|----------|
| `/api/blackjack/start` | POST | hands (int) | GameState |
| `/api/blackjack/state/{id}` | POST | - | GameState |
| `/api/blackjack/hit/{id}` | POST | hand (int) | GameState |
| `/api/blackjack/stand/{id}` | POST | hand (int) | GameState |
| `/api/blackjack/double/{id}` | POST | hand (int) | GameState |
| `/api/blackjack/split/{id}` | POST | hand (int) | GameState |
| `/api/blackjack/insurance/{id}` | POST | hand (int) | GameState |
| `/api/blackjack/decline-insurance/{id}` | POST | hand (int) | GameState |

## HTTP Status Codes Handled

- **200** ✅ Success
- **400** ❌ Bad request (invalid parameters)
- **404** ❌ Not found (game doesn't exist)
- **409** ❌ Conflict (invalid operation for game state)
- **500** ❌ Server error

## Deployment

The testing application is fully integrated into the existing project:
1. All files are in `src/test/java`
2. No additional dependencies required (uses existing Jackson and JUnit)
3. Build status: **✅ Successfully compiles with Maven**
4. All checkstyle rules followed

## Next Steps / Future Enhancements

1. **Performance Testing** - Add load testing utilities
2. **Logging** - Add request/response logging
3. **Metrics** - Collect response time metrics
4. **WebSocket Support** - Real-time game updates
5. **Batch Operations** - Test multiple games simultaneously
6. **Coverage Reports** - Generate API coverage reports
7. **CI/CD Integration** - GitHub Actions workflow
8. **API Documentation** - Generate OpenAPI/Swagger docs

## Summary

A production-ready REST API testing framework has been created with:
- ✅ Complete HTTP client with all endpoints
- ✅ 12+ comprehensive test cases
- ✅ Interactive CLI for manual testing
- ✅ Automated scenario runner
- ✅ Assertion helpers for test validation
- ✅ Full documentation
- ✅ Proper code style compliance
- ✅ Easy extensibility for future enhancements

All code is ready to use and fully integrated into the Blackjack project.
