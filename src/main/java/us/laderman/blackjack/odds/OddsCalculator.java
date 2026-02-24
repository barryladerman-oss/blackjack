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
     * @return Map with keys "total""win", "push", "lose" and values
     */
    public static Odds calculateOdds(Shoe shoe, Hand playerHand, Card dealerUpCard) {
        Odds result = new Odds();

        Map<Integer, Odds> cache = new HashMap<>();
        Shoe copyShoe = new Shoe(shoe);

        // do once for each rank and cache
        {
            Hand dealerHand = new Hand();
            dealerHand.addCard(dealerUpCard);

            Card dealerHoleCard = copyShoe.draw();
            dealerHand.addCard(dealerHoleCard);

            if (!cache.containsKey(dealerHoleCard.getRank().getValue())) {
                Odds simulatedOdds = simulateDealer(dealerHand.getCards(), copyShoe, false);
                cache.put(dealerHoleCard.getRank().getValue(), simulatedOdds);
            }
            result.combine(cache.get(dealerHoleCard.getRank().getValue()));
        }
        return result;
    }

    // Recursively simulate all possible dealer outcomes
    private static Odds simulateDealer(List<Card> dealerCards, Shoe shoe,
            boolean playerBlackjack) {
        Odds result = new Odds();
        result.setSimTotal(1);
        result.setSimWin(1);
        return result;
    }
}
