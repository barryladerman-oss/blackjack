package us.laderman.blackjack.service;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
/**
 * Service layer for managing Blackjack game logic and state transitions.
 */
public class BlackjackService {
    private static final int DEFAULT_DECKS = 6;
    private static final int MAX_HANDS = 5;

    private static final Duration GAME_TTL = Duration.ofMinutes(30);

    private final Map<UUID, GameEntry> games = new ConcurrentHashMap<>();

    /**
     * Starts a new Blackjack game with the specified number of hands and
     * parameters.
     * 
     * @param hands     number of player hands
     * @param allParams additional parameters for game setup
     * @return the initial game state
     */
    public GameState startGame(int hands, Map<String, String> allParams) {
        if (hands < 1 || hands > MAX_HANDS) {
            throw new IllegalArgumentException("Hands must be between 1 and " + MAX_HANDS);
        }

        int decks = DEFAULT_DECKS;
        if (allParams.containsKey("decks")) {
            try {
                decks = Integer.parseInt(allParams.get("decks"));
            } catch (NumberFormatException e) {
                // fallback to default
            }
        }

        // Parse dealer cards
        Card dealerCard1 = parseCard(allParams.get("dealerCard1Rank"), allParams.get("dealerCard1Suit"));
        Card dealerCard2 = parseCard(allParams.get("dealerCard2Rank"), allParams.get("dealerCard2Suit"));

        // Parse player hand cards
        Map<Integer, HandCards> playerHandCards = new java.util.HashMap<>();
        for (int i = 1; i <= hands; i++) {
            String card1Rank = allParams.get("playerHand" + i + "Card1Rank");
            String card1Suit = allParams.get("playerHand" + i + "Card1Suit");
            String card2Rank = allParams.get("playerHand" + i + "Card2Rank");
            String card2Suit = allParams.get("playerHand" + i + "Card2Suit");

            Card c1 = parseCard(card1Rank, card1Suit);
            Card c2 = parseCard(card2Rank, card2Suit);

            if (c1 != null || c2 != null) {
                playerHandCards.put(i - 1, new HandCards(c1, c2)); // 0-indexed
            }
        }

        // Collect all cards for duplicate validation
        java.util.List<Card> allCards = new java.util.ArrayList<>();
        if (dealerCard1 != null) {
            allCards.add(dealerCard1);
        }
        for (HandCards hc : playerHandCards.values()) {
            if (hc.card1 != null) {
                allCards.add(hc.card1);
            }
            if (hc.card2 != null) {
                allCards.add(hc.card2);
            }
        }
        validateNoDuplicates(allCards.toArray(Card[]::new), decks);

        // Parse rule settings
        boolean allowDoubleOn9 = Boolean.parseBoolean(allParams.get("doubleOn9"));
        boolean dealerHitSoft17 = Boolean.parseBoolean(allParams.get("hitSoft17"));
        boolean allowDoubleOnSplit = Boolean.parseBoolean(allParams.get("allowDoubleOnSplit"));

        BlackjackGame game = new BlackjackGame(
                hands,
                decks,
                playerHandCards,
                dealerCard1,
                dealerCard2,
                allowDoubleOn9,
                dealerHitSoft17,
                allowDoubleOnSplit);
        games.put(game.getId(), new GameEntry(game));
        return game.getState();
    }

    private static class GameEntry {
        private final BlackjackGame game;
        private volatile long lastAccessMillis;

        private GameEntry(BlackjackGame game) {
            this.game = game;
            touch();
        }

        private void touch() {
            lastAccessMillis = System.currentTimeMillis();
        }

        private boolean isExpired(long nowMillis) {
            return nowMillis - lastAccessMillis > GAME_TTL.toMillis();
        }
    }

    public static class HandCards {
        public final Card card1;
        public final Card card2;

        public HandCards(Card card1, Card card2) {
            this.card1 = card1;
            this.card2 = card2;
        }
    }

    // Overloaded method to validate against deck count
    private void validateNoDuplicates(Card[] cards, int decks) {
        java.util.Map<String, Integer> cardCounts = new java.util.HashMap<>();
        for (Card card : cards) {
            if (card == null) {
                continue;
            }

            String key = card.getRank() + "_" + card.getSuit();
            cardCounts.put(key, cardCounts.getOrDefault(key, 0) + 1);
            if (cardCounts.get(key) > decks) {
                throw new IllegalArgumentException(
                        "Card selected too many times: " + card.getRank().getLabel() + " of "
                                + card.getSuit().getLabel() +
                                " (selected " + cardCounts.get(key) + " times, but only " + decks
                                + " available in the shoe)");
            }

        }
    }

    /**
     * Parses a card from rank and suit strings.
     * 
     * @param rankStr rank string
     * @param suitStr suit string
     * @return Card object or null if input is invalid
     * @throws IllegalArgumentException if the card is invalid
     */
    private Card parseCard(String rankStr, String suitStr) {
        if (rankStr == null || rankStr.isEmpty() || suitStr == null || suitStr.isEmpty()) {
            return null;
        }
        try {
            Rank rank = Rank.fromString(rankStr);
            Suit suit = Suit.valueOf(suitStr.toUpperCase());
            return new Card(suit, rank);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid card: " + rankStr + " of " + suitStr);
        }
    }

    /**
     * Performs a hit action for the specified hand in the game.
     * 
     * @param gameId    unique game identifier
     * @param handIndex index of the hand to hit
     * @return the updated game state
     */
    public GameState hit(UUID gameId, int handIndex) {
        return getGame(gameId).hit(handIndex, null);
    }

    /**
     * Performs a hit action with a specific card for the specified hand.
     * 
     * @param gameId    unique game identifier
     * @param handIndex index of the hand to hit
     * @param rankStr   rank of the card (optional)
     * @param suitStr   suit of the card (optional)
     * @return the updated game state
     */
    public GameState hit(UUID gameId, int handIndex, String rankStr, String suitStr) {
        Card selectedCard = parseCard(rankStr, suitStr);
        return getGame(gameId).hit(handIndex, selectedCard);
    }

    /**
     * Performs a stand action for the specified hand in the game.
     * 
     * @param gameId    unique game identifier
     * @param handIndex index of the hand to stand
     * @return the updated game state
     */
    public GameState stand(UUID gameId, int handIndex) {
        return getGame(gameId).stand(handIndex);
    }

    /**
     * Performs a double down action for the specified hand in the game.
     * 
     * @param gameId    unique game identifier
     * @param handIndex index of the hand to double down
     * @param rank      rank of the card (optional)
     * @param suit      suit of the card (optional)
     * @return the updated game state
     */
    public GameState doubleDown(UUID gameId, int handIndex, String rank, String suit) {
        Card selectedCard = parseCard(rank, suit);
        return getGame(gameId).doubleDown(handIndex, selectedCard);
    }

    /**
     * Splits the specified hand into two hands in the game.
     * 
     * @param gameId    unique game identifier
     * @param handIndex index of the hand to split
     * @return the updated game state
     */
    public GameState split(UUID gameId, int handIndex) {
        return getGame(gameId).split(handIndex, null, null);
    }

    /**
     * Splits the specified hand with specific cards in the game.
     * 
     * @param gameId    unique game identifier
     * @param handIndex index of the hand to split
     * @param card1Rank rank of the first card (optional)
     * @param card1Suit suit of the first card (optional)
     * @param card2Rank rank of the second card (optional)
     * @param card2Suit suit of the second card (optional)
     * @return the updated game state
     */
    public GameState split(UUID gameId, int handIndex, String card1Rank, String card1Suit,
            String card2Rank, String card2Suit) {
        System.out.println("DEBUG: split called with card1Rank=" + card1Rank + ", card1Suit=" + card1Suit
                + ", card2Rank=" + card2Rank + ", card2Suit=" + card2Suit);

        // Normalize empty strings to null
        card1Rank = (card1Rank != null && card1Rank.isEmpty()) ? null : card1Rank;
        card1Suit = (card1Suit != null && card1Suit.isEmpty()) ? null : card1Suit;
        card2Rank = (card2Rank != null && card2Rank.isEmpty()) ? null : card2Rank;
        card2Suit = (card2Suit != null && card2Suit.isEmpty()) ? null : card2Suit;

        Card selectedCard1 = parseCard(card1Rank, card1Suit);
        Card selectedCard2 = parseCard(card2Rank, card2Suit);

        // Handle rank-only specifications
        Rank rank1 = null;
        Rank rank2 = null;
        if (selectedCard1 == null && card1Rank != null) {
            try {
                rank1 = Rank.fromString(card1Rank);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid rank: " + card1Rank);
            }
        }
        if (selectedCard2 == null && card2Rank != null) {
            try {
                rank2 = Rank.fromString(card2Rank);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid rank: " + card2Rank);
            }
        }

        System.out.println("DEBUG: Parsed cards - card1=" + selectedCard1 + ", card2=" + selectedCard2
                + ", rank1=" + rank1 + ", rank2=" + rank2);
        return getGame(gameId).split(handIndex, selectedCard1, selectedCard2, rank1, rank2);
    }

    /**
     * Performs an insurance action for the specified hand in the game.
     * 
     * @param gameId    unique game identifier
     * @param handIndex index of the hand for insurance
     * @return the updated game state
     */
    public GameState insurance(UUID gameId, int handIndex) {
        return getGame(gameId).insurance(handIndex);
    }

    /**
     * Declines insurance for the specified hand in the game.
     * 
     * @param gameId    unique game identifier
     * @param handIndex index of the hand to decline insurance
     * @return the updated game state
     */
    public GameState declineInsurance(UUID gameId, int handIndex) {
        return getGame(gameId).declineInsurance(handIndex);
    }

    /**
     * Gets the current state of the specified game.
     * 
     * @param gameId unique game identifier
     * @return the current game state
     */
    public GameState getState(UUID gameId) {
        return getGame(gameId).getState();
    }

    /**
     * Retrieves the BlackjackGame instance for the given gameId.
     * 
     * @param gameId unique game identifier
     * @return BlackjackGame instance
     * @throws IllegalArgumentException if game is not found or expired
     */
    private BlackjackGame getGame(UUID gameId) {
        GameEntry entry = games.get(gameId);
        if (entry == null) {
            throw new IllegalArgumentException("Game not found");
        }
        long now = System.currentTimeMillis();
        if (entry.isExpired(now)) {
            games.remove(gameId);
            throw new IllegalArgumentException("Game expired");
        }
        entry.touch();
        return entry.game;
    }

    /**
     * Cleans up expired games from the game map. Runs on a fixed schedule.
     */
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).ofMinutes(5).toMillis()}")
    public void cleanupExpiredGames() {
        long now = System.currentTimeMillis();
        games.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }
}
