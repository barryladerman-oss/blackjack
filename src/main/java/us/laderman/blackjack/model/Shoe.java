package us.laderman.blackjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents the shoe containing multiple decks of cards for Blackjack.
 */
public class Shoe {
    private final List<Card> cards;
    private final Random random = new Random();

    public Shoe() {
        this.cards = new ArrayList<>();
    }

    /**
     * Copy constructor for Shoe.
     * Creates a new Shoe with the same cards as the original.
     * 
     * @param original The Shoe to copy
     */
    public Shoe(Shoe other) {
        this.cards = new ArrayList<>(other.cards.size());
        this.cards.addAll(other.cards);
    }

    public Shoe(int decks) {
        this.cards = new ArrayList<>(decks * 52);
        for (int d = 0; d < decks; d++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(suit, rank));
                }
            }
        }
        Collections.shuffle(cards, random);
    }

    /**
     * Returns a copy of the remaining cards in the shoe.
     * 
     * @return List of remaining Card objects
     */
    public List<Card> getRemainingCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Draws a card from the shoe.
     * 
     * @return the drawn card
     * @throws IllegalStateException if the shoe is empty
     */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Shoe is empty");
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Removes a specific card from the shoe.
     * 
     * @param targetCard the card to add
     */
    public void addSpecificCard(Card targetCard) {
        cards.add(targetCard);
    }

    /**
     * Removes a specific card from the shoe.
     * 
     * @param targetCard the card to remove
     * @return true if the card was removed, false otherwise
     */
    public boolean removeSpecificCard(Card targetCard) {
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (card.getRank() == targetCard.getRank() && card.getSuit() == targetCard.getSuit()) {
                cards.remove(i);
                return true;
            }
        }
        return false;
    }

    public Card drawSpecificRank(Rank rank) {
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (card.getRank() == rank) {
                cards.remove(i);
                return card;
            }
        }
        return null;
    }

    public int remaining() {
        return cards.size();
    }
}
