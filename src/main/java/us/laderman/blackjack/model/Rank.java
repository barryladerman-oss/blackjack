package us.laderman.blackjack.model;

public enum Rank {
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 10),
    QUEEN("Q", 10),
    KING("K", 10),
    ACE("A", 11);

    private final String label;
    private final int value;

    Rank(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Rank fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String upper = value.toUpperCase();
        // First try to match enum name (e.g., "FOUR", "JACK")
        try {
            return Rank.valueOf(upper);
        } catch (IllegalArgumentException e) {
            // If that fails, try to match by label (e.g., "4", "J", "10")
            for (Rank rank : Rank.values()) {
                if (rank.label.equalsIgnoreCase(value)) {
                    return rank;
                }
            }
            throw new IllegalArgumentException("Invalid rank: " + value);
        }
    }
}
