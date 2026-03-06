package us.laderman.android.blackjack.model;

import com.google.gson.annotations.SerializedName;

public class PlayerHandState extends DealerState {
    @SerializedName("standing")
    private boolean standing;

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

    @SerializedName("insured")
    private boolean insured;

    @SerializedName("insuranceDeclined")
    private boolean insuranceDeclined;

    @SerializedName("insuranceWon")
    private boolean insuranceWon;

    public boolean isStanding() {
        return standing;
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

    public boolean isInsured() {
        return insured;
    }

    public boolean isInsuranceDeclined() {
        return insuranceDeclined;
    }

    public boolean isInsuranceWon() {
        return insuranceWon;
    }
}
