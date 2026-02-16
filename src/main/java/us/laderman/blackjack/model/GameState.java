package us.laderman.blackjack.model;

import java.util.List;

public class GameState {
    private final String id;
    private final String phase;
    private final DealerState dealer;
    private final List<PlayerHandState> hands;
    private final int runningCount;
    private final double trueCount;
    private final double decksRemaining;
    private final double netUnits;
    private final double insuranceTotal;
    private final int suggestedBetUnits;
    private final String note;

    public GameState(String id,
            String phase,
            DealerState dealer,
            List<PlayerHandState> hands,
            int runningCount,
            double trueCount,
            double decksRemaining,
            double netUnits,
            double insuranceTotal,
            int suggestedBetUnits,
            String note) {
        this.id = id;
        this.phase = phase;
        this.dealer = dealer;
        this.hands = hands;
        this.runningCount = runningCount;
        this.trueCount = trueCount;
        this.decksRemaining = decksRemaining;
        this.netUnits = netUnits;
        this.insuranceTotal = insuranceTotal;
        this.suggestedBetUnits = suggestedBetUnits;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public String getPhase() {
        return phase;
    }

    public DealerState getDealer() {
        return dealer;
    }

    public List<PlayerHandState> getHands() {
        return hands;
    }

    public int getRunningCount() {
        return runningCount;
    }

    public double getTrueCount() {
        return trueCount;
    }

    public double getDecksRemaining() {
        return decksRemaining;
    }

    public double getNetUnits() {
        return netUnits;
    }

    public double getInsuranceTotal() {
        return insuranceTotal;
    }

    public int getSuggestedBetUnits() {
        return suggestedBetUnits;
    }

    public String getNote() {
        return note;
    }

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
                int betUnits) {
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
                ", decksRemaining=" + decksRemaining +
                ", netUnits=" + netUnits +
                ", insuranceTotal=" + insuranceTotal +
                ", suggestedBetUnits=" + suggestedBetUnits +
                ", note='" + note + '\'' +
                '}';
    }
}
