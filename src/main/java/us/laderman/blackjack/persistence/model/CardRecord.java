package us.laderman.blackjack.persistence.model;

public class CardRecord {
    private Integer pk;
    private Integer handFk;
    private String suit;
    private String value;

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

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
