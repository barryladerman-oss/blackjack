package us.laderman.blackjack.service;

import us.laderman.blackjack.model.Rank;

/**
 * Implements optimal blackjack strategy based on:
 * - Basic strategy matrix (mathematically proven to minimize house edge)
 * - True count deviations (card counting adjustments)
 * - Index plays (adjustments based on true count)
 */
public class Strategy {

    // Basic strategy: Hard hands (non-soft totals)
    // Rows: player total 5-20, Columns: dealer up card 2-11 (index 2-11, where 10
    // includes T,J,Q,K)
    // Values: 0=HIT, 1=STAND, 2=DOUBLE
    private static final int[][] HARD_STRATEGY = {
            // Player 5: always hit
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            // Player 6: always hit
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            // Player 7: always hit
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            // Player 8: always hit
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            // Player 9: double on 3-6, otherwise hit
            { 0, 2, 2, 2, 2, 0, 0, 0, 0, 0 },
            // Player 10: double on 2-9, hit on 10,11
            { 2, 2, 2, 2, 2, 2, 2, 2, 0, 0 },
            // Player 11: always double
            { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 },
            // Player 12: stand on 4-6, hit otherwise
            { 0, 0, 1, 1, 1, 0, 0, 0, 0, 0 },
            // Player 13: stand on 2-6, hit on 7-A
            { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
            // Player 14: stand on 2-6, hit on 7-A
            { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
            // Player 15: stand on 2-6, hit on 7-A (surrender vs 10 if available)
            { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
            // Player 16: stand on 2-6, hit on 7-A (surrender vs 9,10,A if available)
            { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
            // Player 17: always stand
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            // Player 18: always stand
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            // Player 19: always stand
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            // Player 20: always stand
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
    };

    // Basic strategy: Soft hands (with usable ace)
    // Rows: soft hand total 12-20 (where ace counts as 11)
    // Columns: dealer up card 2-11
    // Values: 0=HIT, 1=STAND, 2=DOUBLE
    private static final int[][] SOFT_STRATEGY = {
            // Soft 12 (A+1): always hit
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            // Soft 13 (A+2): double on 5-6, hit otherwise
            { 0, 0, 2, 2, 0, 0, 0, 0, 0, 0 },
            // Soft 14 (A+3): double on 5-6, hit otherwise
            { 0, 0, 2, 2, 0, 0, 0, 0, 0, 0 },
            // Soft 15 (A+4): double on 4-6, hit otherwise
            { 0, 2, 2, 2, 0, 0, 0, 0, 0, 0 },
            // Soft 16 (A+5): double on 4-6, hit otherwise
            { 0, 2, 2, 2, 0, 0, 0, 0, 0, 0 },
            // Soft 17 (A+6): double on 3-6, hit on 2 and 7-A
            { 0, 2, 2, 2, 2, 0, 0, 0, 0, 0 },
            // Soft 18 (A+7): stand on 2-8, double on 3-6, hit on 9-A
            { 2, 1, 2, 2, 2, 1, 1, 1, 0, 0 },
            // Soft 19 (A+8): always stand
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            // Soft 20 (A+9): always stand
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
    };

    // Split strategy: Rows represent each card pair (2,3,4,5,6,7,8,9,10/T,A)
    // Columns: dealer up card 2-11
    // Values: 0=NO, 1=YES
    private static final int[][] SPLIT_STRATEGY = {
            // Pair of 2s: split on 3-7, hit 2, hit 8-A
            { 0, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
            // Pair of 3s: split on 2-7, hit 8-A
            { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
            // Pair of 4s: split on 5-6, hit otherwise
            { 0, 0, 2, 2, 0, 0, 0, 0, 0, 0 },
            // Pair of 5s: never split (double down 10 instead)
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            // Pair of 6s: split on 2-6, hit 7-A
            { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
            // Pair of 7s: split on 2-7, hit 8-A
            { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
            // Pair of 8s: always split
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            // Pair of 9s: split on 2-6,8-9, stand on 7,10,A
            { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 },
            // Pair of 10s: never split
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            // Pair of Aces: always split
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
    };

    /**
     * Gets the recommended action based on basic strategy
     */
    public static String getRecommendedAction(Hand hand, int dealerUpCard, boolean canSplit) {
        if (hand.isBusted()) {
            return "NONE";
        }

        HandValue value = hand.getValue();
        int dealerIndex = getDealerIndex(dealerUpCard);

        // Check split first if possible
        if (canSplit && hand.getCards().size() == 2) {
            Rank rank = hand.getCards().get(0).getRank();
            int pairIndex = getPairIndex(rank);
            if (pairIndex >= 0 && dealerIndex >= 0) {
                if (SPLIT_STRATEGY[pairIndex][dealerIndex] == 1) {
                    return "SPLIT";
                }
            }
        }

        // Check double, then hit/stand for two-card hands
        if (hand.getCards().size() == 2) {
            if (value.isSoft()) {
                return getSoftHandAction(value.getTotal(), dealerIndex);
            } else {
                int action = getHardHandAction(value.getTotal(), dealerIndex);
                return action == 2 ? "DOUBLE" : (action == 1 ? "STAND" : "HIT");
            }
        }

        // For hands with 3+ cards, only hit or stand
        if (value.isSoft()) {
            int action = getSoftHandActionValue(value.getTotal(), dealerIndex);
            return action == 1 ? "STAND" : "HIT";
        } else {
            int action = getHardHandAction(value.getTotal(), dealerIndex);
            return action == 1 ? "STAND" : "HIT";
        }
    }

    /**
     * Gets strategy adjustment based on true count for specific situations
     */
    public static String applyCountDeviation(String basicStrategy, Hand hand,
            int dealerUpCard, double trueCount) {
        if (hand.isBusted() || hand.getCards().size() > 2) {
            return basicStrategy;
        }

        HandValue value = hand.getValue();
        int total = value.getTotal();
        boolean soft = value.isSoft();

        // Key index plays for 16 vs 10
        if (!soft && total == 16 && dealerUpCard == 10 && trueCount >= 0.0) {
            return "STAND";
        }

        // 15 vs 10
        if (!soft && total == 15 && dealerUpCard == 10 && trueCount >= 4.0) {
            return "STAND";
        }

        // 10 vs 10
        if (!soft && total == 10 && dealerUpCard == 10 && trueCount <= -4.0) {
            return "HIT";
        }

        // 12 vs 3
        if (!soft && total == 12 && dealerUpCard == 3 && trueCount <= -1.0) {
            return "HIT";
        }

        // 12 vs 4
        if (!soft && total == 12 && dealerUpCard == 4 && trueCount <= -1.0) {
            return "HIT";
        }

        // 13 vs 2
        if (!soft && total == 13 && dealerUpCard == 2 && trueCount <= -1.0) {
            return "HIT";
        }

        return basicStrategy;
    }

    private static int getDealerIndex(int dealerUpCard) {
        // Convert dealer card value to index (2-11 -> 0-9, with 10 for T,J,Q,K,A)
        if (dealerUpCard == 11) {
            return 9; // Ace
        }
        if (dealerUpCard == 10) {
            return 8; // 10, J, Q, K
        }
        return Math.max(0, Math.min(7, dealerUpCard - 2)); // 2-9 -> 0-7
    }

    private static int getPairIndex(Rank rank) {
        return switch (rank) {
            case TWO -> 0;
            case THREE -> 1;
            case FOUR -> 2;
            case FIVE -> 3;
            case SIX -> 4;
            case SEVEN -> 5;
            case EIGHT -> 6;
            case NINE -> 7;
            case TEN, JACK, QUEEN, KING -> 8;
            case ACE -> 9;
        };
    }

    private static int getHardHandAction(int total, int dealerIndex) {
        if (total < 5 || total > 20) {
            return 0; // Invalid, hit
        }
        return HARD_STRATEGY[total - 5][dealerIndex];
    }

    private static String getSoftHandAction(int total, int dealerIndex) {
        int action = getSoftHandActionValue(total, dealerIndex);
        if (action == 2) {
            return "DOUBLE";
        }
        return action == 1 ? "STAND" : "HIT";
    }

    private static int getSoftHandActionValue(int total, int dealerIndex) {
        if (total < 12 || total > 20) {
            return 0; // Invalid, hit
        }
        return SOFT_STRATEGY[total - 12][dealerIndex];
    }
}
