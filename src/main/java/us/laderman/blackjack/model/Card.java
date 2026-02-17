package us.laderman.blackjack.model;

/**
 * Represents a playing card in Blackjack, defined by rank and suit.
 */
public class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Gets the suit of the card.
     * 
     * @return card suit
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Gets the rank of the card.
     * 
     * @return card rank
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Gets the Blackjack value of the card.
     * 
     * @return card value
     */
    public int getValue() {
        return rank.getValue();
    }

    /**
     * Gets the label for the card (e.g., "A of Hearts").
     * 
     * @return card label
     */
    public String getLabel() {
        return rank.getLabel() + " of " + suit.getLabel();
    }

    /**
     * Returns the string representation of the card.
     * 
     * @return card label as string
     */
    @Override
    public String toString() {
        return rank.getLabel() + " of " + suit.getLabel();
    }
}
