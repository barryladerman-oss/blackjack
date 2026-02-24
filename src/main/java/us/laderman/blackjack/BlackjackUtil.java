package us.laderman.blackjack;

public class BlackjackUtil {
    public static double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
