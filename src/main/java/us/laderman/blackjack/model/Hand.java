package us.laderman.blackjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a player's or dealer's hand in Blackjack.
 */
public class Hand {
    private final List<Card> cards = new ArrayList<>();
    private boolean standing;
    private boolean busted;
    private boolean insured;
    private boolean insuranceWon;
    private boolean insuranceDeclined;
    private int betUnits = 1;
    private boolean isFromSplit = false;
    private Rank splitFromRank = null;
    private boolean canHitAfterSplitAces = true;

    public Hand() {
    }

    /**
     * Copy constructor for Hand. Performs a deep copy of the cards list and copies
     * all fields.
     * 
     * @param other the Hand to copy
     */
    public Hand(Hand other) {
        cards.addAll(other.cards); // Card is assumed to be immutable or safe to share

        this.standing = other.standing;
        this.busted = other.busted;
        this.insured = other.insured;
        this.insuranceWon = other.insuranceWon;
        this.insuranceDeclined = other.insuranceDeclined;
        this.betUnits = other.betUnits;
        this.isFromSplit = other.isFromSplit;
        this.splitFromRank = other.splitFromRank;
        this.canHitAfterSplitAces = other.canHitAfterSplitAces;
    }

    /**
     * Adds a card to the hand and updates bust status.
     * 
     * @param card card to add
     */
    public void addCard(Card card) {
        cards.add(card);
        HandValue value = getValue();
        if (value.getTotal() > 21) {
            busted = true;
        }
    }

    /**
     * Gets the list of cards in the hand.
     * 
     * @return unmodifiable list of cards
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    /**
     * Checks if the hand is standing.
     * 
     * @return true if standing, false otherwise
     */
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

    public boolean isInsuranceDeclined() {
        return insuranceDeclined;
    }

    public void setInsuranceDeclined(boolean insuranceDeclined) {
        this.insuranceDeclined = insuranceDeclined;
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

    public Rank getSplitFromRank() {
        return splitFromRank;
    }

    public void setSplitFromRank(Rank rank) {
        splitFromRank = rank;
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
            if (card == null)
                continue;
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
