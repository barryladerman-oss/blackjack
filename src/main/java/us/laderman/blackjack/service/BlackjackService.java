package us.laderman.blackjack.service;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlackjackService {
    private static final int DEFAULT_DECKS = 1;
    private static final int MAX_HANDS = 5;

    private final Map<UUID, BlackjackGame> games = new ConcurrentHashMap<>();

    public GameState startGame(int hands, Map<String, String> allParams) {
        if (hands < 1 || hands > MAX_HANDS) {
            throw new IllegalArgumentException("Hands must be between 1 and " + MAX_HANDS);
        }

        // Parse dealer card
        Card dealerCard1 = parseCard(allParams.get("dealerCard1Rank"), allParams.get("dealerCard1Suit"));

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
        if (dealerCard1 != null)
            allCards.add(dealerCard1);
        for (HandCards hc : playerHandCards.values()) {
            if (hc.card1 != null)
                allCards.add(hc.card1);
            if (hc.card2 != null)
                allCards.add(hc.card2);
        }
        validateNoDuplicates(allCards.toArray(new Card[0]));

        BlackjackGame game = new BlackjackGame(hands, DEFAULT_DECKS, playerHandCards, dealerCard1, null);
        games.put(game.getId(), game);
        return game.getState();
    }

    public static class HandCards {
        public final Card card1;
        public final Card card2;

        public HandCards(Card card1, Card card2) {
            this.card1 = card1;
            this.card2 = card2;
        }
    }

    private void validateNoDuplicates(Card... cards) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Card card : cards) {
            if (card != null) {
                String key = card.getRank() + "_" + card.getSuit();
                if (seen.contains(key)) {
                    throw new IllegalArgumentException(
                            "Duplicate card selected: " + card.getRank().getLabel() + " of "
                                    + card.getSuit().getLabel());
                }
                seen.add(key);
            }
        }
    }

    private Card parseCard(String rankStr, String suitStr) {
        if (rankStr == null || rankStr.isEmpty() || suitStr == null || suitStr.isEmpty()) {
            return null;
        }
        try {
            Rank rank = Rank.valueOf(rankStr.toUpperCase());
            Suit suit = Suit.valueOf(suitStr.toUpperCase());
            return new Card(suit, rank);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid card: " + rankStr + " of " + suitStr);
        }
    }

    public GameState hit(UUID gameId, int handIndex) {
        return getGame(gameId).hit(handIndex);
    }

    public GameState stand(UUID gameId, int handIndex) {
        return getGame(gameId).stand(handIndex);
    }

    public GameState doubleDown(UUID gameId, int handIndex) {
        return getGame(gameId).doubleDown(handIndex);
    }

    public GameState split(UUID gameId, int handIndex) {
        return getGame(gameId).split(handIndex);
    }

    public GameState insurance(UUID gameId, int handIndex) {
        return getGame(gameId).insurance(handIndex);
    }

    public GameState declineInsurance(UUID gameId, int handIndex) {
        return getGame(gameId).declineInsurance(handIndex);
    }

    public GameState getState(UUID gameId) {
        return getGame(gameId).getState();
    }

    private BlackjackGame getGame(UUID gameId) {
        BlackjackGame game = games.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        return game;
    }
}
