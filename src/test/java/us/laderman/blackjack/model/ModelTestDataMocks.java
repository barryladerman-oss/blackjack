package us.laderman.blackjack.model;

import java.util.List;

import org.mockito.Mockito;

import static org.mockito.Mockito.when;

final class ModelTestDataMocks {
    private ModelTestDataMocks() {
    }

    static ModelTestData create() {
        ModelTestData data = Mockito.mock(ModelTestData.class);
        when(data.cardCreation()).thenReturn(new CardInput(Suit.HEARTS, Rank.ACE));
        when(data.cardValues()).thenReturn(List.of(
                new CardValueInput(new CardInput(Suit.HEARTS, Rank.ACE), 11),
                new CardValueInput(new CardInput(Suit.DIAMONDS, Rank.TEN), 10),
                new CardValueInput(new CardInput(Suit.CLUBS, Rank.FIVE), 5)));
        when(data.cardLabel()).thenReturn(new CardInput(Suit.SPADES, Rank.KING));
        when(data.cardString()).thenReturn(new CardInput(Suit.HEARTS, Rank.ACE));
        when(data.gameIdState()).thenReturn(new GameStateInput(
                "test-id", "PLAYER_TURN", null, null, 0, 0, 1, 0, 0, "Test"));
        when(data.gamePhaseState()).thenReturn(new GameStateInput(
                "id", "PLAYER_TURN", null, null, 0, 0, 1, 0, 0, "Test"));
        when(data.gameDealerState()).thenReturn(new GameStateInput(
                "id", "PLAYER_TURN", new GameState.DealerState(
                        List.of(new Card(Suit.HEARTS, Rank.SIX)), 6, false, false, false),
                null, 0, 0, 1, 0, 0, "Test"));
        when(data.gameCardCounts()).thenReturn(new GameStateInput(
                "id", "PLAYER_TURN", null, List.of(), 5, 2.5, 52, 0, 0, "Test"));
        when(data.gameNote()).thenReturn(new GameStateInput(
                "id", "PLAYER_TURN", null, List.of(), 0, 0, 1, 0, 0, "Dealer up card 6"));
        when(data.dealerCards()).thenReturn(new DealerStateInput(
                List.of(new Card(Suit.SPADES, Rank.SIX)), 6, false, false, false));
        when(data.dealerTotal()).thenReturn(new DealerStateInput(List.of(), 17, true, false, true));
        when(data.dealerSoft()).thenReturn(new DealerStateInput(List.of(), 17, true, false, true));
        when(data.dealerBusted()).thenReturn(new DealerStateInput(List.of(), 22, false, true, true));
        when(data.dealerStanding()).thenReturn(new DealerStateInput(List.of(), 17, false, false, true));
        when(data.playerHand()).thenReturn(new PlayerHandStateInput(
                1, List.of(), 15, false, false, false, false, false, false,
                null, "HIT", false, true, 1, 0.0));
        return data;
    }

    interface ModelTestData {
        CardInput cardCreation();

        List<CardValueInput> cardValues();

        CardInput cardLabel();

        CardInput cardString();

        GameStateInput gameIdState();

        GameStateInput gamePhaseState();

        GameStateInput gameDealerState();

        GameStateInput gameCardCounts();

        GameStateInput gameNote();

        DealerStateInput dealerCards();

        DealerStateInput dealerTotal();

        DealerStateInput dealerSoft();

        DealerStateInput dealerBusted();

        DealerStateInput dealerStanding();

        PlayerHandStateInput playerHand();
    }

    record CardInput(Suit suit, Rank rank) {
    }

    record CardValueInput(CardInput card, int expectedValue) {
    }

    record GameStateInput(
            String id,
            String phase,
            GameState.DealerState dealer,
            List<GameState.PlayerHandState> hands,
            int runningCount,
            double trueCount,
            int cardsRemaining,
            double netUnits,
            double insuranceTotal,
            String note) {
    }

    record DealerStateInput(List<Card> cards, int total, boolean soft, boolean busted, boolean standing) {
    }

    record PlayerHandStateInput(
            int index,
            List<Card> cards,
            int total,
            boolean soft,
            boolean busted,
            boolean standing,
            boolean insured,
            boolean insuranceDeclined,
            boolean insuranceWon,
            String result,
            String recommendedAction,
            boolean fromSplit,
            boolean canHitAfterSplitAces,
            int betUnits,
            double bustProbability) {
    }
}
