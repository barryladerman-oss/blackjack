package us.laderman.blackjack.service;

public class HandValue {
    private final int total;
    private final boolean soft;

    public HandValue(int total, boolean soft) {
        this.total = total;
        this.soft = soft;
    }

    public int getTotal() {
        return total;
    }

    public boolean isSoft() {
        return soft;
    }
}
