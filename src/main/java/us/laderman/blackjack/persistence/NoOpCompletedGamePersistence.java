package us.laderman.blackjack.persistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import us.laderman.blackjack.model.GameState;

@Service
@ConditionalOnProperty(name = "blackjack.database.mode", havingValue = "none")
public class NoOpCompletedGamePersistence implements CompletedGamePersistence {
    @Override
    public void persistCompletedGame(GameState state, int decks) {
    }
}
