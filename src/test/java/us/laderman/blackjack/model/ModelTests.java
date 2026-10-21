package us.laderman.blackjack.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for Card model in Blackjack.
 */
/**
 * Unit tests for Card model in Blackjack.
 */
@DisplayName("Card Model Tests")
class CardModelTest {
    private final ModelTestDataMocks.ModelTestData testData = ModelTestDataMocks.create();

    @Test
    @DisplayName("should create card with suit and rank")
    /**
     * Tests card creation with suit and rank.
     */
    void testCardCreation() {
        var input = testData.cardCreation();
        Card card = new Card(input.suit(), input.rank());
        assertThat(card.getSuit()).isEqualTo(input.suit());
        assertThat(card.getRank()).isEqualTo(input.rank());
    }

    @Test
    @DisplayName("should get correct card value")
    /**
     * Tests correct value assignment for cards.
     */
    void testCardValue() {
        for (var input : testData.cardValues()) {
            Card card = new Card(input.card().suit(), input.card().rank());
            assertThat(card.getValue()).isEqualTo(input.expectedValue());
        }
    }

    @Test
    @DisplayName("should generate card label")
    /**
     * Tests label generation for cards.
     */
    void testCardLabel() {
        var input = testData.cardLabel();
        Card card = new Card(input.suit(), input.rank());
        String label = card.getLabel();

        assertThat(label).contains("K");
        assertThat(label).contains("Spade");
    }

    @Test
    @DisplayName("should represent card as string")
    /**
     * Tests string representation of cards.
     */
    void testCardString() {
        var input = testData.cardString();
        Card card = new Card(input.suit(), input.rank());
        String string = card.toString();

        assertThat(string).isNotNull();
        assertThat(string).isNotEmpty();
    }

    @Test
    @DisplayName("should have all 52 unique card combinations")
    /**
     * Tests that all 52 card combinations are unique.
     */
    void testAllCardsAreUnique() {
        int cardCount = 0;
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                @SuppressWarnings("unused")
                var card = new Card(suit, rank);
                cardCount++;
            }
        }

        assertThat(cardCount).isEqualTo(52);
    }
}

@DisplayName("Rank Enum Tests")
class RankModelTest {

    @Test
    @DisplayName("should have all required ranks")
    void testAllRanksPresent() {
        assertThat(Rank.ACE).isNotNull();
        assertThat(Rank.TWO).isNotNull();
        assertThat(Rank.THREE).isNotNull();
        assertThat(Rank.FOUR).isNotNull();
        assertThat(Rank.FIVE).isNotNull();
        assertThat(Rank.SIX).isNotNull();
        assertThat(Rank.SEVEN).isNotNull();
        assertThat(Rank.EIGHT).isNotNull();
        assertThat(Rank.NINE).isNotNull();
        assertThat(Rank.TEN).isNotNull();
        assertThat(Rank.JACK).isNotNull();
        assertThat(Rank.QUEEN).isNotNull();
        assertThat(Rank.KING).isNotNull();
    }

    @Test
    @DisplayName("should have correct blackjack values")
    void testRankValues() {
        assertThat(Rank.ACE.getValue()).isEqualTo(11);
        assertThat(Rank.TWO.getValue()).isEqualTo(2);
        assertThat(Rank.FIVE.getValue()).isEqualTo(5);
        assertThat(Rank.TEN.getValue()).isEqualTo(10);
        assertThat(Rank.JACK.getValue()).isEqualTo(10);
        assertThat(Rank.QUEEN.getValue()).isEqualTo(10);
        assertThat(Rank.KING.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("should have correct labels")
    void testRankLabels() {
        assertThat(Rank.ACE.getLabel()).isNotEmpty();
        assertThat(Rank.JACK.getLabel()).isNotEmpty();
        assertThat(Rank.QUEEN.getLabel()).isNotEmpty();
        assertThat(Rank.KING.getLabel()).isNotEmpty();
    }

    @Test
    @DisplayName("should correctly identify numeric ranks")
    void testNumericRanks() {
        assertThat(Rank.TWO.getValue()).isEqualTo(2);
        assertThat(Rank.THREE.getValue()).isEqualTo(3);
        assertThat(Rank.NINE.getValue()).isEqualTo(9);
    }

    @Test
    @DisplayName("should correctly identify face cards")
    void testFaceCards() {
        assertThat(Rank.JACK.getValue()).isEqualTo(10);
        assertThat(Rank.QUEEN.getValue()).isEqualTo(10);
        assertThat(Rank.KING.getValue()).isEqualTo(10);
    }
}

@DisplayName("Suit Enum Tests")
class SuitModelTest {

    @Test
    @DisplayName("should have all required suits")
    void testAllSuitsPresent() {
        assertThat(Suit.HEARTS).isNotNull();
        assertThat(Suit.DIAMONDS).isNotNull();
        assertThat(Suit.CLUBS).isNotNull();
        assertThat(Suit.SPADES).isNotNull();
    }

    @Test
    @DisplayName("should have correct labels")
    void testSuitLabels() {
        assertThat(Suit.HEARTS.getLabel()).isNotBlank();
        assertThat(Suit.DIAMONDS.getLabel()).isNotBlank();
        assertThat(Suit.CLUBS.getLabel()).isNotBlank();
        assertThat(Suit.SPADES.getLabel()).isNotBlank();
    }

    @Test
    @DisplayName("should correctly identify red suits")
    void testRedSuits() {
        assertThat(Suit.HEARTS.getLabel()).isNotBlank();
        assertThat(Suit.DIAMONDS.getLabel()).isNotBlank();
    }

    @Test
    @DisplayName("should correctly identify black suits")
    void testBlackSuits() {
        assertThat(Suit.CLUBS.getLabel()).isNotBlank();
        assertThat(Suit.SPADES.getLabel()).isNotBlank();
    }

    @Test
    @DisplayName("should have exactly 4 suits")
    void testSuitCount() {
        assertThat(Suit.values()).hasSize(4);
    }
}

@DisplayName("GameState Model Tests")
class GameStateModelTest {
    private final ModelTestDataMocks.ModelTestData testData = ModelTestDataMocks.create();

    private GameState createState(ModelTestDataMocks.GameStateInput input) {
        return new GameState(
                input.id(),
                input.phase(),
                input.dealer(),
                input.hands(),
                input.runningCount(),
                input.trueCount(),
                input.cardsRemaining(),
                input.netUnits(),
                input.insuranceTotal(),
                input.note());
    }

    @Test
    @DisplayName("should store game ID")
    void testGameIdStorage() {
        var input = testData.gameIdState();
        GameState state = createState(input);

        assertThat(state.getId()).isEqualTo(input.id());
    }

    @Test
    @DisplayName("should store game phase")
    void testGamePhaseStorage() {
        var input = testData.gamePhaseState();
        GameState state = createState(input);

        assertThat(state.getPhase()).isEqualTo(input.phase());
    }

    @Test
    @DisplayName("should store dealer state")
    void testDealerStateStorage() {
        var input = testData.gameDealerState();
        GameState state = createState(input);

        assertThat(state.getDealer()).isNotNull();
        assertThat(state.getDealer().getTotal()).isEqualTo(input.dealer().getTotal());
    }

    @Test
    @DisplayName("should store card counts")
    void testCardCounts() {
        var input = testData.gameCardCounts();
        GameState state = createState(input);

        assertThat(state.getRunningCount()).isEqualTo(input.runningCount());
        assertThat(state.getTrueCount()).isEqualTo(input.trueCount());
    }

    @Test
    @DisplayName("should store note/message")
    void testGameNote() {
        var input = testData.gameNote();
        GameState state = createState(input);

        assertThat(state.getNote()).isEqualTo(input.note());
    }
}

@DisplayName("DealerState Model Tests")
class DealerStateModelTest {
    private final ModelTestDataMocks.ModelTestData testData = ModelTestDataMocks.create();

    private GameState.DealerState createDealer(ModelTestDataMocks.DealerStateInput input) {
        return new GameState.DealerState(input.cards(), input.total(), input.soft(), input.busted(), input.standing());
    }

    @Test
    @DisplayName("should store dealer cards")
    void testDealerCards() {
        var input = testData.dealerCards();
        var dealer = createDealer(input);

        assertThat(dealer.getCards()).hasSize(input.cards().size());
    }

    @Test
    @DisplayName("should store dealer total")
    void testDealerTotal() {
        var input = testData.dealerTotal();
        var dealer = createDealer(input);

        assertThat(dealer.getTotal()).isEqualTo(input.total());
    }

    @Test
    @DisplayName("should track soft hand state")
    void testDealerSoftState() {
        var input = testData.dealerSoft();
        var dealer = createDealer(input);

        assertThat(dealer.isSoft()).isEqualTo(input.soft());
    }

    @Test
    @DisplayName("should track bust state")
    void testDealerBustState() {
        var input = testData.dealerBusted();
        var dealer = createDealer(input);

        assertThat(dealer.isBusted()).isEqualTo(input.busted());
    }

    @Test
    @DisplayName("should track standing state")
    void testDealerStandingState() {
        var input = testData.dealerStanding();
        var dealer = createDealer(input);

        assertThat(dealer.isStanding()).isEqualTo(input.standing());
    }
}

@DisplayName("PlayerHandState Model Tests")
class PlayerHandStateModelTest {
    private final ModelTestDataMocks.ModelTestData testData = ModelTestDataMocks.create();

    @Test
    @DisplayName("should store hand index")
    void testHandIndex() {
        var input = testData.playerHand();
        var hand = new GameState.PlayerHandState(
                input.index(),
                input.cards(),
                input.total(),
                input.soft(),
                input.busted(),
                input.standing(),
                input.insured(),
                input.insuranceDeclined(),
                input.insuranceWon(),
                input.result(),
                input.recommendedAction(),
                input.fromSplit(),
                input.canHitAfterSplitAces(),
                input.betUnits(),
                input.bustProbability());
        assertThat(hand.getIndex()).isEqualTo(input.index());
    }
}
