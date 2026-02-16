package us.laderman.blackjack.service;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Shoe {
    private final List<Card> cards;
    private final Random random = new Random();

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

    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Shoe is empty");
        }
        return cards.remove(cards.size() - 1);
    }

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
