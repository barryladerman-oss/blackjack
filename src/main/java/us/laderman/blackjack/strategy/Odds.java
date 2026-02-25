package us.laderman.blackjack.strategy;

public class Odds {
    private int simWin;
    private int simPush;
    private int simLose;
    private int simTotal;

    public Odds() {
        this(0, 0, 0, 0);
    }

    public Odds(int simWin, int simPush, int simLose, int simTotal) {
        this.simWin = simWin;
        this.simPush = simPush;
        this.simLose = simLose;
        this.simTotal = simTotal;
    }

    public Odds combine(Odds other) {
        this.simWin += other.simWin;
        this.simPush += other.simPush;
        this.simLose += other.simLose;
        this.simTotal += other.simTotal;
        return this;
    }

    public double getWin() {
        return simTotal == 0 ? 0.0 : (double) simWin / simTotal;
    }

    public double getPush() {
        return simTotal == 0 ? 0.0 : (double) simPush / simTotal;
    }

    public double getLose() {
        return simTotal == 0 ? 0.0 : (double) simLose / simTotal;
    }

    public int getSimWin() {
        return simWin;
    }

    public void setSimWin(int simWin) {
        this.simWin = simWin;
    }

    public int getSimPush() {
        return simPush;
    }

    public void setSimPush(int simPush) {
        this.simPush = simPush;
    }

    public int getSimLose() {
        return simLose;
    }

    public void setSimLose(int simLose) {
        this.simLose = simLose;
    }

    public int getSimTotal() {
        return simTotal;
    }

    public void setSimTotal(int simTotal) {
        this.simTotal = simTotal;
    }

    @Override
    public String toString() {
        return String.format("{ win=%.2f%%, lose=%.2f%%, push=%.2f%%, simWin=%d, simLose=%d, simPush=%d, simTotal=%d }",
                getWin() * 100, getLose() * 100, getPush() * 100, simWin, simLose, simPush, simTotal);
    }
}
