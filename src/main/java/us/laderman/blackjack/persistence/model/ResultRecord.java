package us.laderman.blackjack.persistence.model;

public class ResultRecord {
    private Integer pk;
    private Integer handFk;
    private short wager;
    private short won;

    public Integer getPk() {
        return pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }

    public Integer getHandFk() {
        return handFk;
    }

    public void setHandFk(Integer handFk) {
        this.handFk = handFk;
    }

    public short getWager() {
        return wager;
    }

    public void setWager(short wager) {
        this.wager = wager;
    }

    public short getWon() {
        return won;
    }

    public void setWon(short won) {
        this.won = won;
    }
}
