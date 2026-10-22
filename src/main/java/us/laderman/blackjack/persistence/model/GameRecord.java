package us.laderman.blackjack.persistence.model;

import java.time.LocalDateTime;

public class GameRecord {
    private Integer pk;
    private LocalDateTime time;
    private Integer numDecks;

    public Integer getPk() {
        return pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Integer getNumDecks() {
        return numDecks;
    }

    public void setNumDecks(Integer numDecks) {
        this.numDecks = numDecks;
    }
}
