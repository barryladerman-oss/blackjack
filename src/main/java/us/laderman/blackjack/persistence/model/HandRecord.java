package us.laderman.blackjack.persistence.model;

public class HandRecord {
    private Integer pk;
    private Integer gameFk;
    private boolean dealer;
    private boolean doubled;
    private boolean blackjack;
    private boolean insured;

    public Integer getPk() {
        return pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }

    public Integer getGameFk() {
        return gameFk;
    }

    public void setGameFk(Integer gameFk) {
        this.gameFk = gameFk;
    }

    public boolean isDealer() {
        return dealer;
    }

    public void setDealer(boolean dealer) {
        this.dealer = dealer;
    }

    public boolean isDoubled() {
        return doubled;
    }

    public void setDoubled(boolean doubled) {
        this.doubled = doubled;
    }

    public boolean isBlackjack() {
        return blackjack;
    }

    public void setBlackjack(boolean blackjack) {
        this.blackjack = blackjack;
    }

    public boolean isInsured() {
        return insured;
    }

    public void setInsured(boolean insured) {
        this.insured = insured;
    }
}
