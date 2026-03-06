package us.laderman.android.blackjack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GameState {
    @SerializedName("id")
    private String id;

    @SerializedName("phase")
    private String phase;

    @SerializedName("dealer")
    private DealerState dealer;

    @SerializedName("hands")
    private List<PlayerHandState> hands;

    @SerializedName("runningCount")
    private int runningCount;

    @SerializedName("trueCount")
    private double trueCount;

    @SerializedName("cardsRemaining")
    private int cardsRemaining;

    @SerializedName("netUnits")
    private double netUnits;

    @SerializedName("insuranceTotal")
    private double insuranceTotal;

    @SerializedName("note")
    private String note;

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

    public int getCardsRemaining() {
        return cardsRemaining;
    }

    public double getNetUnits() {
        return netUnits;
    }

    public double getInsuranceTotal() {
        return insuranceTotal;
    }

    public String getNote() {
        return note;
    }
}
