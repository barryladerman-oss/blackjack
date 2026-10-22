# Blackjack Server

Spring Boot REST API that runs a minimal blackjack game with multiple player hands and Hi-Lo card counting. The API returns the running count, true count, and a suggested bet size.

## Clients

This project includes:
- **Web Client**: Browser-based interface (in `src/main/resources/static/`)
- **Android Client**: Native Android app (in `android-client/`)

See the [Android Client README](android-client/README.md) for setup and installation instructions.

## Run Server

```bash
mvn spring-boot:run
```

Default base URL: `http://localhost:8080`

## Database Access

Completed games are persisted transactionally to the `game`, `hand`, `card`,
and `result` tables through the MyBatis mappers in
`src/main/java/us/laderman/blackjack/persistence/mapper/`. Their mapped records
are in `src/main/java/us/laderman/blackjack/persistence/model/`.

The default connection is `jdbc:mysql://localhost:3306/blackjack` with user
`root` and an empty password. Override it with `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD` environment variables. Select the backend with
`BLACKJACK_DATABASE_MODE`:

- `mysql` (default): use the configured MySQL database.
- `h2`: use an in-memory H2 database initialized with the project schema.
- `none`: disable database connections and completed-game persistence.

Run the read-only mapper integration test against the configured local database:

```bash
mvn -Dtest=MySqlMapperIntegrationTest -Dblackjack.mysql.integration=true test
```

Run the H2 mapper integration test without connecting to MySQL:

```bash
mvn -Dtest=H2MapperIntegrationTest -Dblackjack.database.mode=h2 test
```

Set `BLACKJACK_DATABASE_MODE=h2` to run the application with H2, or
`BLACKJACK_DATABASE_MODE=none` to run it without a database.

## API

### Start a game

```bash
curl -X POST "http://localhost:8080/api/blackjack/start?hands=2"
```

### Get game state

```bash
curl "http://localhost:8080/api/blackjack/state/{gameId}"
```

### Hit a hand

```bash
curl -X POST "http://localhost:8080/api/blackjack/hit/{gameId}?hand=0"
```

### Stand a hand

```bash
curl -X POST "http://localhost:8080/api/blackjack/stand/{gameId}?hand=0"
```

## Notes

- Dealer stands on all 17s.
- Single-deck shoe with up to 5 hands.
- Recommended actions are basic-strategy style hints using the dealer up card.
- The dealer hand is fully revealed in state responses for simplicity.
