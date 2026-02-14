package com.example.blackjack.service;

import com.example.blackjack.model.Card;
import com.example.blackjack.model.Rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {
    private final List<Card> cards = new ArrayList<>();
    private boolean standing;
    private boolean busted;
    private boolean insured;
    private boolean insuranceWon;
    private int betUnits = 1;
    private boolean isFromSplit = false;
    private boolean canHitAfterSplitAces = true;

    public void addCard(Card card) {
        cards.add(card);
        HandValue value = getValue();
        if (value.getTotal() > 21) {
            busted = true;
        }
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public boolean isStanding() {
        return standing;
    }

    public void setStanding(boolean standing) {
        this.standing = standing;
    }

    public boolean isBusted() {
        return busted;
    }

    public boolean isInsured() {
        return insured;
    }

    public void setInsured(boolean insured) {
        this.insured = insured;
    }

    public boolean isInsuranceWon() {
        return insuranceWon;
    }

    public void setInsuranceWon(boolean insuranceWon) {
        this.insuranceWon = insuranceWon;
    }

    public int getBetUnits() {
        return betUnits;
    }

    public void setBetUnits(int betUnits) {
        this.betUnits = betUnits;
    }

    public boolean canSplit() {
        return cards.size() == 2 && cards.get(0).getRank() == cards.get(1).getRank();
    }

    public Card splitOff() {
        if (!canSplit()) {
            throw new IllegalStateException("Hand cannot be split");
        }
        Card card = cards.remove(1);
        busted = false;
        standing = false;
        return card;
    }

    public boolean isFromSplit() {
        return isFromSplit;
    }

    public void setIsFromSplit(boolean fromSplit) {
        isFromSplit = fromSplit;
    }

    public boolean canHitAfterSplitAces() {
        return canHitAfterSplitAces;
    }

    public void setCanHitAfterSplitAces(boolean can) {
        this.canHitAfterSplitAces = can;
    }

    public HandValue getValue() {
        int total = 0;
        int aces = 0;
        for (Card card : cards) {
            total += card.getRank().getValue();
            if (card.getRank() == Rank.ACE) {
                aces++;
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        boolean soft = aces > 0;
        return new HandValue(total, soft);
    }
}
