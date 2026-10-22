package us.laderman.blackjack.persistence;

import us.laderman.blackjack.model.GameState;

public interface CompletedGamePersistence {
    void persistCompletedGame(GameState state, int decks);
}
