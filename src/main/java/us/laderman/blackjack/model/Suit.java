package us.laderman.blackjack.model;

/**
 * Enum representing card suits in Blackjack.
 */
public enum Suit {
    CLUBS("Clubs"),
    DIAMONDS("Diamonds"),
    HEARTS("Hearts"),
    SPADES("Spades");

    private final String label;

    Suit(String label) {
        this.label = label;
    }

    /**
     * Gets the label for the suit (e.g., "Hearts").
     * 
     * @return suit label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the string representation of the suit.
     * 
     * @return suit label as string
     */
    @Override
    public String toString() {
        return label;
    }
}
