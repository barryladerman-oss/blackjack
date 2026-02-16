package us.laderman.blackjack.model;

public class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getValue() {
        return rank.getValue();
    }

    public String getLabel() {
        return rank.getLabel() + " of " + suit.getLabel();
    }

    @Override
    public String toString() {
        return rank.getLabel() + " of " + suit.getLabel();
    }
}
