# Blackjack REST API Testing Application

A comprehensive REST API testing framework for the Blackjack Spring Boot REST API. This application provides multiple ways to test and validate the Blackjack REST endpoints.

## Components

### 1. **BlackjackRestClient**
Low-level HTTP client for making REST API calls.

**Methods:**
- `startGame(int hands)` - Start a new game
- `getGameState(String gameId)` - Retrieve current game state
- `hit(String gameId, int hand)` - Hit on a hand
- `stand(String gameId, int hand)` - Stand on a hand
- `doubleDown(String gameId, int hand)` - Double down
- `split(String gameId, int hand)` - Split a hand
- `insurance(String gameId, int hand)` - Take insurance
- `declineInsurance(String gameId, int hand)` - Decline insurance

**Example Usage:**
```java
BlackjackRestClient client = new BlackjackRestClient("http://localhost:8080");
ApiResponse<GameState> response = client.startGame(1);
if (response.isSuccess()) {
    GameState state = response.getData();
    System.out.println("Game started: " + state.getId());
}
```

### 2. **ApiResponse<T>**
Generic wrapper for API responses containing status code, data, and error information.

**Properties:**
- `statusCode` - HTTP status code
- `data` - Response payload
- `errorMessage` - Error details if applicable
- `success` - Boolean flag indicating success/failure

### 3. **ApiAssertions**
Helper class for validating API responses in tests.

**Methods:**
- `assertSuccess(ApiResponse<T> response)` - Assert successful response
- `assertStatusCode(ApiResponse<T> response, int expectedCode)` - Assert specific status code
- `assertHasData(ApiResponse<T> response)` - Assert response contains data
- `assertGameHasHands(GameState state, int expectedCount)` - Assert hand count
- `assertGamePhase(GameState state, String expectedPhase)` - Assert game phase
- `assertValidGameId(GameState state)` - Assert valid UUID format

### 4. **BlackjackRestApiTest**
JUnit 5 integration test class with comprehensive test cases.

**Test Cases:**
- Start game with single/multiple hands
- Get game state
- Hit, stand, double down operations
- Split and insurance operations
- Error handling (invalid IDs, non-existent games)
- Complete game sequences

**Running Tests:**
```bash
mvn test -Dtest=BlackjackRestApiTest
```

### 5. **BlackjackRestTestApplication**
Interactive CLI application for manual testing.

**Running:**
```bash
mvn exec:java -Dexec.mainClass="us.laderman.blackjack.restclient.BlackjackRestTestApplication" -Dexec.args="http://localhost:8080"
```

**Features:**
- Menu-driven interface
- Start new games
- Execute game actions (hit, stand, double, split)
- View game state
- Game history

### 6. **TestScenarioRunner**
Automated scenario runner that executes predefined test sequences.

**Test Scenarios:**
1. Basic game flow
2. Multiple hands
3. Hit and stand
4. Invalid inputs
5. Double down
6. Split hand
7. Insurance operations
8. Game state retrieval
9. Sequential operations

**Running:**
```bash
mvn exec:java -Dexec.mainClass="us.laderman.blackjack.restclient.TestScenarioRunner" -Dexec.args="http://localhost:8080"
```

**Output:** Detailed test report with pass/fail statistics

## API Endpoints

### Start Game
```
POST /api/blackjack/start?hands=1
Response: GameState
```

### Get Game State
```
POST /api/blackjack/state/{id}
Response: GameState
```

### Hit
```
POST /api/blackjack/hit/{id}?hand=0
Response: GameState
```

### Stand
```
POST /api/blackjack/stand/{id}?hand=0
Response: GameState
```

### Double Down
```
POST /api/blackjack/double/{id}?hand=0
Response: GameState
```

### Split
```
POST /api/blackjack/split/{id}?hand=0
Response: GameState
```

### Insurance
```
POST /api/blackjack/insurance/{id}?hand=0
Response: GameState
```

### Decline Insurance
```
POST /api/blackjack/decline-insurance/{id}?hand=0
Response: GameState
```

## Error Handling

The REST client handles various HTTP status codes:

- **200** - Success
- **400** - Bad request (invalid parameters, invalid UUID)
- **404** - Game not found
- **409** - Conflict (invalid operation for current game state)
- **500** - Server error

## Usage Examples

### Example 1: Play a Complete Game
```java
BlackjackRestClient client = new BlackjackRestClient("http://localhost:8080");

// Start game
ApiResponse<GameState> startResp = client.startGame(1);
String gameId = startResp.getData().getId();

// Hit
client.hit(gameId, 0);

// Hit again
client.hit(gameId, 0);

// Stand
ApiResponse<GameState> finalResp = client.stand(gameId, 0);
GameState finalState = finalResp.getData();
```

### Example 2: Validate API Response
```java
ApiResponse<GameState> response = client.startGame(1);

// Using assertions
ApiAssertions.assertSuccess(response);
ApiAssertions.assertStatusCode(response, 200);
ApiAssertions.assertHasData(response);
ApiAssertions.assertGameHasHands(response.getData(), 1);
```

### Example 3: Handle Errors
```java
ApiResponse<GameState> response = client.getGameState("invalid-id");

if (!response.isSuccess()) {
    System.out.println("Status: " + response.getStatusCode());
    System.out.println("Error: " + response.getErrorMessage());
}
```

## Dependencies

- Java 11+
- Spring Boot (for testing with `@SpringBootTest`)
- JUnit 5
- Jackson (for JSON serialization)
- Java HTTP Client

## File Structure

```
src/test/java/us/laderman/blackjack/restclient/
├── BlackjackRestClient.java          # HTTP client
├── ApiResponse.java                  # Response wrapper
├── ApiAssertions.java                # Test assertions
├── BlackjackRestApiTest.java         # Integration tests
├── BlackjackRestTestApplication.java # Interactive CLI
└── TestScenarioRunner.java           # Scenario runner
```

## Notes

- All endpoints use POST method
- Game IDs are UUIDs
- Hand indices are 0-based
- Optional card parameters (rank, suit) are available for testing with specific cards
- The client automatically handles URL encoding and error responses

## Future Enhancements

- HTTP request/response logging
- Performance metrics collection
- Load testing utilities
- Response caching
- Batch operation support
- WebSocket support for real-time updates
