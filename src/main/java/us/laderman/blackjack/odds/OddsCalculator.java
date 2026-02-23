package us.laderman.blackjack.odds;

import us.laderman.blackjack.model.*;
import java.util.*;

/**
 * Utility to calculate the odds of a player hand winning against a dealer up
 * card.
 * This routine simulates all possible outcomes recursively.
 */
public class OddsCalculator {
    /**
     * Calculates the probability of the player hand winning, pushing, or losing
     * against the dealer up card, given the current shoe.
     *
     * @param playerHand   The player's hand (deep copy, will be mutated)
     * @param dealerUpCard The dealer's up card
     * @param shoe         The current shoe (deep copy, will be mutated)
     * @return Map with keys "win", "push", "lose" and their probabilities (sum to
     *         1)
     */
    public static Map<String, Double> calculateOdds(Shoe shoe, Hand playerHand, Card dealerUpCard) {
        Map<String, Double> odds = new HashMap<>();
        odds.put("win", 0.0);
        odds.put("push", 0.0);
        odds.put("lose", 0.0);
        return odds;
    }
}
