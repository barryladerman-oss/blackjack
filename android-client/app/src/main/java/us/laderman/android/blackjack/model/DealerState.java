package us.laderman.android.blackjack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DealerState {
    @SerializedName("cards")
    private List<Card> cards;

    @SerializedName("total")
    private int total;

    @SerializedName("soft")
    private boolean soft;

    @SerializedName("busted")
    private boolean busted;

    @SerializedName("blackjack")
    private boolean blackjack;

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

    public boolean isBlackjack() {
        return blackjack;
    }
}
