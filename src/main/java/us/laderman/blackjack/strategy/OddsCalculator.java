package us.laderman.blackjack.strategy;

import us.laderman.blackjack.model.*;
import java.util.*;

/**
 * Utility to calculate the odds of a player hand winning against a dealer up
 * card.
 */
public class OddsCalculator {

    public Odds getOdds(Shoe shoe, Hand playerHand, Card dealerUpCard) {
        Shoe testShoe = new Shoe(shoe);

        if (dealerUpCard.getRank() == Rank.TEN ||
                dealerUpCard.getRank() == Rank.JACK ||
                dealerUpCard.getRank() == Rank.QUEEN ||
                dealerUpCard.getRank() == Rank.KING) {
            for (Card card : testShoe.getCards()) {
                if (card.getRank() == Rank.ACE) {
                    testShoe.removeSpecificCard(card);
                }
            }
        }
        if (dealerUpCard.getRank() == Rank.ACE) {
            for (Card card : testShoe.getCards()) {
                if (card.getRank() == Rank.TEN) {
                    testShoe.removeSpecificCard(card);
                }
            }
        }
        Odds odds = calculateOdds(testShoe, playerHand, dealerUpCard);
        return odds;
    }

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

        for (Card card : shoe.getCards()) {
            Shoe copyShoe = new Shoe(shoe);
            copyShoe.removeSpecificCard(card);

            Hand dealerHand = new Hand();
            dealerHand.addCard(dealerUpCard);

            Card dealerHoleCard = card;
            dealerHand.addCard(dealerHoleCard);

            if (!cache.containsKey(dealerHoleCard.getRank().getValue())) {
                Odds simulatedOdds = simulateDealer(copyShoe, dealerHand, playerHand);
                cache.put(dealerHoleCard.getRank().getValue(), simulatedOdds);
            }
            result.combine(cache.get(dealerHoleCard.getRank().getValue()));
        }
        return result;
    }

    // Recursively simulate all possible dealer outcomes
    private static Odds simulateDealer(Shoe shoe, Hand dealerHand, Hand playerHand) {
        Odds result = new Odds();

        // Dealer stands if total >= 17 (does NOT hit on soft 17)
        if (dealerHand.getValue() < 17) {
            // Dealer must hit: try every possible card
            for (Card shoeCard : shoe.getCards()) {
                Shoe nextShoe = new Shoe(shoe);
                nextShoe.removeSpecificCard(shoeCard);

                Hand nextHand = new Hand(dealerHand);
                nextHand.addCard(shoeCard);

                Odds branchOdds = simulateDealer(nextShoe, nextHand, playerHand);
                result.combine(branchOdds);
            }
            return result;
        }

        // Dealer stands: resolve outcome
        result.setSimTotal(result.getSimTotal() + 1);
        if (playerHand.isBusted()) {
            result.setSimLose(result.getSimLose() + 1);
        } else if (dealerHand.isBusted()) {
            result.setSimWin(result.getSimWin() + 1);
        } else if (playerHand.getValue() > dealerHand.getValue()) {
            result.setSimWin(result.getSimWin() + 1);
        } else if (playerHand.getValue() < dealerHand.getValue()) {
            result.setSimLose(result.getSimLose() + 1);
        } else {
            result.setSimPush(result.getSimPush() + 1);
        }
        return result;
    }

    /**
     * Calculates the probability that the player hand will bust on the next card.
     *
     * @param shoe       The current shoe
     * @param playerHand The player's hand
     * @return Probability of busting (0.0 to 1.0)
     */
    public static double calculateBustProbability(Shoe shoe, Hand playerHand) {
        // If already busted or standing, return 0
        if (playerHand.isBusted() || playerHand.isStanding()) {
            return 0.0;
        }

        int currentValue = playerHand.getValue();

        // If 21, cannot bust
        if (currentValue >= 21) {
            return 0.0;
        }

        List<Card> availableCards = shoe.getCards();
        if (availableCards.isEmpty()) {
            return 0.0;
        }

        int bustCards = 0;
        int totalCards = availableCards.size();

        for (Card card : availableCards) {
            // Create a test hand to check if this card would bust
            Hand testHand = new Hand(playerHand);
            testHand.addCard(card);

            if (testHand.isBusted()) {
                bustCards++;
            }
        }

        return (double) bustCards / totalCards;
    }
}
