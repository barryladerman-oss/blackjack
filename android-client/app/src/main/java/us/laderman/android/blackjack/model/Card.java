package us.laderman.android.blackjack.model;

import com.google.gson.annotations.SerializedName;

public class Card {
    @SerializedName("suit")
    private String suit;

    @SerializedName("rank")
    private String rank;

    @SerializedName("value")
    private int value;

    @SerializedName("label")
    private String label;

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getDisplayText() {
        // Convert rank to display format
        String displayRank = getRankDisplay();
        String displaySuit = getSuitSymbol();
        return displayRank + displaySuit;
    }

    private String getRankDisplay() {
        if (rank == null)
            return "";
        switch (rank) {
            case "ACE":
                return "A";
            case "TWO":
                return "2";
            case "THREE":
                return "3";
            case "FOUR":
                return "4";
            case "FIVE":
                return "5";
            case "SIX":
                return "6";
            case "SEVEN":
                return "7";
            case "EIGHT":
                return "8";
            case "NINE":
                return "9";
            case "TEN":
                return "10";
            case "JACK":
                return "J";
            case "QUEEN":
                return "Q";
            case "KING":
                return "K";
            default:
                return rank;
        }
    }

    private String getSuitSymbol() {
        if (suit == null)
            return "";
        switch (suit) {
            case "HEARTS":
                return "♥";
            case "DIAMONDS":
                return "♦";
            case "CLUBS":
                return "♣";
            case "SPADES":
                return "♠";
            default:
                return suit;
        }
    }
}
