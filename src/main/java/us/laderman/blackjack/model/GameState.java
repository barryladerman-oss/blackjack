package us.laderman.blackjack.model;

import java.util.List;

/**
 * Represents the state of a Blackjack game, including player and dealer hands,
 * game phase, and outcome.
 */
public class GameState {
    private final String id;
    private final String phase;
    private final DealerState dealer;
    private final List<PlayerHandState> hands;
    private final int runningCount;
    private final double trueCount;
    private final int cardsRemaining;
    private final double netUnits;
    private final double insuranceTotal;
    private final String note;

    public GameState(String id,
            String phase,
            DealerState dealer,
            List<PlayerHandState> hands,
            int runningCount,
            double trueCount,
            int cardsRemaining,
            double netUnits,
            double insuranceTotal,
            String note) {
        this.id = id;
        this.phase = phase;
        this.dealer = dealer;
        this.hands = hands;
        this.runningCount = runningCount;
        this.trueCount = trueCount;
        this.cardsRemaining = cardsRemaining;
        this.netUnits = netUnits;
        this.insuranceTotal = insuranceTotal;
        this.note = note;
    }

    /**
     * Gets the unique game identifier.
     * 
     * @return game id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the current game phase.
     * 
     * @return game phase
     */
    public String getPhase() {
        return phase;
    }

    /**
     * Gets the dealer's state.
     * 
     * @return dealer state
     */
    public DealerState getDealer() {
        return dealer;
    }

    /**
     * Gets the list of player hands.
     * 
     * @return list of player hand states
     */
    public List<PlayerHandState> getHands() {
        return hands;
    }

    /**
     * Gets the running count for card counting.
     * 
     * @return running count
     */
    public int getRunningCount() {
        return runningCount;
    }

    /**
     * Gets the true count for card counting.
     * 
     * @return true count
     */
    public double getTrueCount() {
        return trueCount;
    }

    /**
     * Gets the number of decks remaining in the shoe.
     * 
     * @return decks remaining
     */
    public int getCardsRemaining() {
        return cardsRemaining;
    }

    /**
     * Gets the net units won or lost.
     * 
     * @return net units
     */
    public double getNetUnits() {
        return netUnits;
    }

    /**
     * Gets the total insurance bet amount.
     * 
     * @return insurance total
     */
    public double getInsuranceTotal() {
        return insuranceTotal;
    }

    /**
     * Gets the note or message for the game state.
     * 
     * @return note
     */
    public String getNote() {
        return note;
    }

    /**
     * Represents the dealer's state in a Blackjack game.
     */
    public static class DealerState {
        private final List<Card> cards;
        private final int total;
        private final boolean soft;
        private final boolean busted;
        private final boolean standing;

        public DealerState(List<Card> cards, int total, boolean soft, boolean busted, boolean standing) {
            this.cards = cards;
            this.total = total;
            this.soft = soft;
            this.busted = busted;
            this.standing = standing;
        }

        /**
         * Gets the dealer's cards.
         * 
         * @return list of dealer cards
         */
        public List<Card> getCards() {
            return cards;
        }

        /**
         * Gets the dealer's hand total value.
         * 
         * @return dealer hand total
         */
        public int getTotal() {
            return total;
        }

        /**
         * Checks if the dealer's hand is soft (contains an Ace counted as 11).
         * 
         * @return true if soft, false otherwise
         */
        public boolean isSoft() {
            return soft;
        }

        /**
         * Checks if the dealer's hand is busted.
         * 
         * @return true if busted, false otherwise
         */
        public boolean isBusted() {
            return busted;
        }

        public boolean isStanding() {
            return standing;
        }

        @Override
        public String toString() {
            return "DealerState{" +
                    "cards=" + cards +
                    ", total=" + total +
                    ", soft=" + soft +
                    ", busted=" + busted +
                    ", standing=" + standing +
                    '}';
        }
    }

    public static class PlayerHandState {
        private final int index;
        private final List<Card> cards;
        private final int total;
        private final boolean soft;
        private final boolean busted;
        private final boolean standing;
        private final boolean insured;
        private final boolean insuranceDeclined;
        private final boolean insuranceWon;
        private final String result;
        private final String recommendedAction;
        private final boolean isFromSplit;
        private final boolean canHitAfterSplitAces;
        private final int betUnits;
        private final double bustProbability;

        public PlayerHandState(int index,
                List<Card> cards,
                int total,
                boolean soft,
                boolean busted,
                boolean standing,
                boolean insured,
                boolean insuranceDeclined,
                boolean insuranceWon,
                String result,
                String recommendedAction,
                boolean isFromSplit,
                boolean canHitAfterSplitAces,
                int betUnits,
                double bustProbability) {
            this.index = index;
            this.cards = cards;
            this.total = total;
            this.soft = soft;
            this.busted = busted;
            this.standing = standing;
            this.insured = insured;
            this.insuranceDeclined = insuranceDeclined;
            this.insuranceWon = insuranceWon;
            this.result = result;
            this.recommendedAction = recommendedAction;
            this.isFromSplit = isFromSplit;
            this.canHitAfterSplitAces = canHitAfterSplitAces;
            this.betUnits = betUnits;
            this.bustProbability = bustProbability;
        }

        public int getIndex() {
            return index;
        }

        public List<Card> getCards() {
            return cards;
        }

        public int getTotal() {
            return total;
        }

        public boolean isSoft() {
            return soft;
        }

        public boolean isBusted() {
            return busted;
        }

        public boolean isStanding() {
            return standing;
        }

        public boolean isInsured() {
            return insured;
        }

        public boolean isInsuranceDeclined() {
            return insuranceDeclined;
        }

        public boolean isInsuranceWon() {
            return insuranceWon;
        }

        public String getResult() {
            return result;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        public boolean isFromSplit() {
            return isFromSplit;
        }

        public boolean isCanHitAfterSplitAces() {
            return canHitAfterSplitAces;
        }

        public boolean getCanHitAfterSplitAces() {
            return canHitAfterSplitAces;
        }

        public int getBetUnits() {
            return betUnits;
        }

        /**
         * Gets the probability of busting on the next card.
         * 
         * @return bust probability (0.0 to 1.0)
         */
        public double getBustProbability() {
            return bustProbability;
        }

        @Override
        public String toString() {
            return "PlayerHandState{" +
                    "index=" + index +
                    ", cards=" + cards +
                    ", total=" + total +
                    ", soft=" + soft +
                    ", busted=" + busted +
                    ", standing=" + standing +
                    ", insured=" + insured +
                    ", insuranceDeclined=" + insuranceDeclined +
                    ", insuranceWon=" + insuranceWon +
                    ", result='" + result + '\'' +
                    ", recommendedAction='" + recommendedAction + '\'' +
                    ", isFromSplit=" + isFromSplit +
                    ", canHitAfterSplitAces=" + canHitAfterSplitAces +
                    ", betUnits=" + betUnits +
                    ", bustProbability=" + bustProbability +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GameState{" +
                "id='" + id + '\'' +
                ", phase='" + phase + '\'' +
                ", dealer=" + dealer +
                ", hands=" + hands +
                ", runningCount=" + runningCount +
                ", trueCount=" + trueCount +
                ", cardsRemaining=" + cardsRemaining +
                ", netUnits=" + netUnits +
                ", insuranceTotal=" + insuranceTotal +
                ", note='" + note + '\'' +
                '}';
    }
}
