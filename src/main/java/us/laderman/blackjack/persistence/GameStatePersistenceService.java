package us.laderman.blackjack.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.context.annotation.Conditional;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Suit;
import us.laderman.blackjack.persistence.mapper.CardMapper;
import us.laderman.blackjack.persistence.mapper.GameMapper;
import us.laderman.blackjack.persistence.mapper.HandMapper;
import us.laderman.blackjack.persistence.mapper.ResultMapper;
import us.laderman.blackjack.persistence.model.CardRecord;
import us.laderman.blackjack.persistence.model.GameRecord;
import us.laderman.blackjack.persistence.model.HandRecord;
import us.laderman.blackjack.persistence.model.ResultRecord;

@Service
@Conditional(DatabasePersistenceEnabledCondition.class)
public class GameStatePersistenceService implements CompletedGamePersistence {
    private final GameMapper gameMapper;
    private final HandMapper handMapper;
    private final CardMapper cardMapper;
    private final ResultMapper resultMapper;
    private final TransactionTemplate transactionTemplate;

    public GameStatePersistenceService(
            GameMapper gameMapper,
            HandMapper handMapper,
            CardMapper cardMapper,
            ResultMapper resultMapper,
            TransactionTemplate transactionTemplate) {
        this.gameMapper = gameMapper;
        this.handMapper = handMapper;
        this.cardMapper = cardMapper;
        this.resultMapper = resultMapper;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void persistCompletedGame(GameState state, int decks) {
        if (!"COMPLETE".equals(state.getPhase())) {
            throw new IllegalArgumentException("Only completed games can be persisted");
        }

        transactionTemplate.executeWithoutResult(transaction -> {
            GameRecord game = new GameRecord();
            game.setTime(LocalDateTime.now());
            game.setNumDecks(decks);
            requireOneRow(gameMapper.insert(game), "game");
            Integer gameId = requireGeneratedKey(game.getPk(), "game");

            GameState.DealerState dealerState = state.getDealer();
            HandRecord dealerHand = new HandRecord();
            dealerHand.setGameFk(gameId);
            dealerHand.setDealer(true);
            dealerHand.setBlackjack(isBlackjack(dealerState.getCards()));
            insertHandAndCards(dealerHand, dealerState.getCards());

            for (GameState.PlayerHandState playerHand : state.getHands()) {
                HandRecord hand = new HandRecord();
                hand.setGameFk(gameId);
                hand.setDoubled(playerHand.getBetUnits() > 1);
                hand.setBlackjack(isBlackjack(playerHand.getCards()));
                hand.setInsured(playerHand.isInsured());
                Integer handId = insertHandAndCards(hand, playerHand.getCards());

                ResultRecord result = new ResultRecord();
                result.setHandFk(handId);
                result.setWager(toSmallInt(playerHand.getBetUnits(), "wager"));
                result.setWon(toWonValue(playerHand.getResult()));
                requireOneRow(resultMapper.insert(result), "hand result");
            }
        });
    }

    private Integer insertHandAndCards(HandRecord hand, List<Card> cards) {
        requireOneRow(handMapper.insert(hand), "hand");
        Integer handId = requireGeneratedKey(hand.getPk(), "hand");
        for (Card dealtCard : cards) {
            CardRecord card = new CardRecord();
            card.setHandFk(handId);
            card.setSuit(toSuitCode(dealtCard.getSuit()));
            card.setValue(toRankCode(dealtCard.getRank()));
            requireOneRow(cardMapper.insert(card), "card");
        }
        return handId;
    }

    private static boolean isBlackjack(List<Card> cards) {
        return cards.size() == 2
                && cards.stream().mapToInt(card -> card.getRank().getValue()).sum() == 21;
    }

    private static short toWonValue(String result) {
        return switch (result) {
            case "WIN" -> 1;
            case "LOSE" -> -1;
            case "PUSH" -> 0;
            default -> throw new IllegalStateException("Unexpected completed hand result: " + result);
        };
    }

    private static String toSuitCode(Suit suit) {
        return switch (suit) {
            case CLUBS -> "C";
            case DIAMONDS -> "D";
            case HEARTS -> "H";
            case SPADES -> "S";
        };
    }

    private static String toRankCode(Rank rank) {
        return rank == Rank.TEN ? "T" : rank.getLabel();
    }

    private static short toSmallInt(int value, String field) {
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException(field + " is outside the supported database range");
        }
        return (short) value;
    }

    private static Integer requireGeneratedKey(Integer key, String entity) {
        if (key == null) {
            throw new IllegalStateException("No generated key returned for " + entity);
        }
        return key;
    }

    private static void requireOneRow(int rows, String entity) {
        if (rows != 1) {
            throw new IllegalStateException("Expected to insert one " + entity + ", inserted " + rows);
        }
    }
}
