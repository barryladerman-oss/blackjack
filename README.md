# Blackjack Server

Spring Boot REST API that runs a minimal blackjack game with multiple player hands and Hi-Lo card counting. The API returns the running count, true count, and a suggested bet size.

## Run

```bash
mvn spring-boot:run
```

Default base URL: `http://localhost:8080`

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
