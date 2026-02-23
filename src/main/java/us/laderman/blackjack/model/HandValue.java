package us.laderman.blackjack.model;

/**
 * Represents the value of a Blackjack hand, including soft/hard values and bust
 * status.
 */
public class HandValue {
    private final int total;
    private final boolean soft;

    /**
     * Constructs a HandValue with the given total and softness.
     * 
     * @param total The total value of the hand
     * @param soft  True if the hand is soft (contains an ace counted as 11)
     */
    public HandValue(int total, boolean soft) {
        this.total = total;
        this.soft = soft;
    }

    /**
     * Returns the total value of the hand.
     * 
     * @return The hand's total value
     */
    /**
     * Returns the total value of the hand.
     * 
     * @return The hand's total value
     */
    public int getTotal() {
        return total;
    }

    /**
     * Returns whether the hand is soft (contains an ace counted as 11).
     * 
     * @return True if the hand is soft
     */
    public boolean isSoft() {
        return soft;
    }
}
