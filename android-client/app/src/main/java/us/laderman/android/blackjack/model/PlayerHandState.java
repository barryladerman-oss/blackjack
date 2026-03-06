package us.laderman.android.blackjack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlayerHandState {
    @SerializedName("cards")
    private List<Card> cards;

    @SerializedName("total")
    private int total;

    @SerializedName("soft")
    private boolean soft;

    @SerializedName("busted")
    private boolean busted;

    @SerializedName("standing")
    private boolean standing;

    @SerializedName("blackjack")
    private boolean blackjack;

    @SerializedName("outcome")
    private String outcome;

    @SerializedName("recommendation")
    private String recommendation;

    @SerializedName("betUnits")
    private int betUnits;

    @SerializedName("won")
    private boolean won;

    @SerializedName("push")
    private boolean push;

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

    public boolean isBlackjack() {
        return blackjack;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public int getBetUnits() {
        return betUnits;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isPush() {
        return push;
    }
}
