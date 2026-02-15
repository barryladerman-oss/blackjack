package us.laderman.blackjack.restclient;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson deserializer for GameState that handles its complex nested
 * structure.
 */
public class GameStateDeserializer extends JsonDeserializer<GameState> {

    @Override
    public GameState deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JsonNode node = mapper.readTree(p);

        String id = node.get("id").asText();
        String phase = node.get("phase").asText();

        // Deserialize dealer state
        GameState.DealerState dealer = deserializeDealerState(node.get("dealer"), ctxt);

        // Deserialize hands
        List<GameState.PlayerHandState> hands = deserializeHands(node.get("hands"), ctxt);

        int runningCount = node.has("runningCount") ? node.get("runningCount").asInt() : 0;
        double trueCount = node.has("trueCount") ? node.get("trueCount").asDouble() : 0.0;
        double decksRemaining = node.has("decksRemaining") ? node.get("decksRemaining").asDouble() : 0.0;
        double netUnits = node.has("netUnits") ? node.get("netUnits").asDouble() : 0.0;
        double insuranceTotal = node.has("insuranceTotal") ? node.get("insuranceTotal").asDouble() : 0.0;
        int suggestedBetUnits = node.has("suggestedBetUnits") ? node.get("suggestedBetUnits").asInt() : 0;
        String note = node.has("note") ? node.get("note").asText() : "";

        return new GameState(id, phase, dealer, hands, runningCount, trueCount,
                decksRemaining, netUnits, insuranceTotal, suggestedBetUnits, note);
    }

    private GameState.DealerState deserializeDealerState(JsonNode node, DeserializationContext ctxt)
            throws IOException {
        if (node == null) {
            return null;
        }

        List<Card> cards = deserializeCards(node.get("cards"), ctxt);
        int total = node.has("total") ? node.get("total").asInt() : 0;
        boolean soft = node.has("soft") ? node.get("soft").asBoolean() : false;
        boolean busted = node.has("busted") ? node.get("busted").asBoolean() : false;
        boolean standing = node.has("standing") ? node.get("standing").asBoolean() : false;

        return new GameState.DealerState(cards, total, soft, busted, standing);
    }

    private List<GameState.PlayerHandState> deserializeHands(JsonNode node, DeserializationContext ctxt)
            throws IOException {
        List<GameState.PlayerHandState> hands = new ArrayList<>();

        if (node == null || !node.isArray()) {
            return hands;
        }

        for (JsonNode handNode : node) {
            GameState.PlayerHandState hand = deserializePlayerHandState(handNode, ctxt);
            if (hand != null) {
                hands.add(hand);
            }
        }

        return hands;
    }

    private GameState.PlayerHandState deserializePlayerHandState(JsonNode node, DeserializationContext ctxt)
            throws IOException {
        int index = node.has("index") ? node.get("index").asInt() : 0;
        List<Card> cards = deserializeCards(node.get("cards"), ctxt);
        int total = node.has("total") ? node.get("total").asInt() : 0;
        boolean soft = node.has("soft") ? node.get("soft").asBoolean() : false;
        boolean busted = node.has("busted") ? node.get("busted").asBoolean() : false;
        boolean standing = node.has("standing") ? node.get("standing").asBoolean() : false;
        boolean insured = node.has("insured") ? node.get("insured").asBoolean() : false;
        boolean insuranceDeclined = node.has("insuranceDeclined") ? node.get("insuranceDeclined").asBoolean() : false;
        boolean insuranceWon = node.has("insuranceWon") ? node.get("insuranceWon").asBoolean() : false;
        String result = node.has("result") ? node.get("result").asText("") : "";
        String recommendedAction = node.has("recommendedAction") ? node.get("recommendedAction").asText("") : "";
        boolean isFromSplit = node.has("isFromSplit") ? node.get("isFromSplit").asBoolean() : false;
        boolean canHitAfterSplitAces = node.has("canHitAfterSplitAces") ? node.get("canHitAfterSplitAces").asBoolean()
                : false;
        int betUnits = node.has("betUnits") ? node.get("betUnits").asInt() : 0;

        return new GameState.PlayerHandState(index, cards, total, soft, busted, standing,
                insured, insuranceDeclined, insuranceWon, result, recommendedAction,
                isFromSplit, canHitAfterSplitAces, betUnits);
    }

    private List<Card> deserializeCards(JsonNode node, DeserializationContext ctxt)
            throws IOException {
        List<Card> cards = new ArrayList<>();

        if (node == null || !node.isArray()) {
            return cards;
        }

        for (JsonNode cardNode : node) {
            Card card = deserializeCard(cardNode, ctxt);
            if (card != null) {
                cards.add(card);
            }
        }

        return cards;
    }

    private Card deserializeCard(JsonNode node, DeserializationContext ctxt)
            throws IOException {
        if (node == null) {
            return null;
        }

        try {
            // Extract suit and rank from JSON
            JsonNode suitNode = node.get("suit");
            JsonNode rankNode = node.get("rank");

            if (suitNode == null || rankNode == null) {
                return null;
            }

            // Parse suit and rank as enums
            String suitStr = suitNode.asText();
            String rankStr = rankNode.asText();

            Suit suit = Suit.valueOf(suitStr);
            Rank rank = Rank.valueOf(rankStr);

            return new Card(suit, rank);
        } catch (IllegalArgumentException e) {
            // Return null if enum parsing fails
            System.err.println("Failed to parse Card enum values: " + e.getMessage());
            return null;
        }
    }
}
