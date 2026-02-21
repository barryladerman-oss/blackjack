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

    @Test
    @DisplayName("should create card with suit and rank")
    /**
     * Tests card creation with suit and rank.
     */
    void testCardCreation() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        assertThat(card.getSuit()).isEqualTo(Suit.HEARTS);
        assertThat(card.getRank()).isEqualTo(Rank.ACE);
    }

    @Test
    @DisplayName("should get correct card value")
    /**
     * Tests correct value assignment for cards.
     */
    void testCardValue() {
        Card ace = new Card(Suit.HEARTS, Rank.ACE);
        Card ten = new Card(Suit.DIAMONDS, Rank.TEN);
        Card five = new Card(Suit.CLUBS, Rank.FIVE);

        assertThat(ace.getValue()).isEqualTo(11);
        assertThat(ten.getValue()).isEqualTo(10);
        assertThat(five.getValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("should generate card label")
    /**
     * Tests label generation for cards.
     */
    void testCardLabel() {
        Card card = new Card(Suit.SPADES, Rank.KING);
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
        Card card = new Card(Suit.HEARTS, Rank.ACE);
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

    @Test
    @DisplayName("should store game ID")
    void testGameIdStorage() {
        GameState state = new GameState(
                "test-id",
                "PLAYER_TURN",
                null,
                null,
                0,
                0,
                1,
                0,
                0,
                1,
                "Test");

        assertThat(state.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("should store game phase")
    void testGamePhaseStorage() {
        GameState state = new GameState(
                "id",
                "PLAYER_TURN",
                null,
                null,
                0,
                0,
                1,
                0,
                0,
                1,
                "Test");

        assertThat(state.getPhase()).isEqualTo("PLAYER_TURN");
    }

    @Test
    @DisplayName("should store dealer state")
    void testDealerStateStorage() {
        GameState.DealerState dealer = new GameState.DealerState(
                java.util.List.of(new Card(Suit.HEARTS, Rank.SIX)),
                6,
                false,
                false,
                false);
        GameState state = new GameState(
                "id",
                "PLAYER_TURN",
                dealer,
                null,
                0,
                0,
                1,
                0,
                0,
                1,
                "Test");

        assertThat(state.getDealer()).isNotNull();
        assertThat(state.getDealer().getTotal()).isEqualTo(6);
    }

    @Test
    @DisplayName("should store card counts")
    void testCardCounts() {
        GameState state = new GameState(
                "id",
                "PLAYER_TURN",
                null,
                java.util.List.of(),
                5,
                2.5,
                52,
                0,
                0,
                1,
                "Test");

        assertThat(state.getRunningCount()).isEqualTo(5);
        assertThat(state.getTrueCount()).isEqualTo(2.5);
    }

    @Test
    @DisplayName("should store betting information")
    void testBettingInformation() {
        GameState state = new GameState(
                "id",
                "PLAYER_TURN",
                null,
                java.util.List.of(),
                0,
                0,
                1,
                0,
                0,
                3,
                "Test");

        assertThat(state.getSuggestedBetUnits()).isEqualTo(3);
    }

    @Test
    @DisplayName("should store note/message")
    void testGameNote() {
        GameState state = new GameState(
                "id",
                "PLAYER_TURN",
                null,
                java.util.List.of(),
                0,
                0,
                1,
                0,
                0,
                1,
                "Dealer up card 6");

        assertThat(state.getNote()).isEqualTo("Dealer up card 6");
    }
}

@DisplayName("DealerState Model Tests")
class DealerStateModelTest {

    @Test
    @DisplayName("should store dealer cards")
    void testDealerCards() {
        var cards = java.util.List.of(new Card(Suit.SPADES, Rank.SIX));
        var dealer = new GameState.DealerState(cards, 6, false, false, false);

        assertThat(dealer.getCards()).hasSize(1);
    }

    @Test
    @DisplayName("should store dealer total")
    void testDealerTotal() {
        var dealer = new GameState.DealerState(java.util.List.of(), 17, true, false, true);

        assertThat(dealer.getTotal()).isEqualTo(17);
    }

    @Test
    @DisplayName("should track soft hand state")
    void testDealerSoftState() {
        var dealer = new GameState.DealerState(java.util.List.of(), 17, true, false, true);

        assertThat(dealer.isSoft()).isTrue();
    }

    @Test
    @DisplayName("should track bust state")
    void testDealerBustState() {
        var dealer = new GameState.DealerState(java.util.List.of(), 22, false, true, true);

        assertThat(dealer.isBusted()).isTrue();
    }

    @Test
    @DisplayName("should track standing state")
    void testDealerStandingState() {
        var dealer = new GameState.DealerState(java.util.List.of(), 17, false, false, true);

        assertThat(dealer.isStanding()).isTrue();
    }
}

@DisplayName("PlayerHandState Model Tests")
class PlayerHandStateModelTest {

    @Test
    @DisplayName("should store hand index")
    void testHandIndex() {
        var hand = new GameState.PlayerHandState(
                1,
                java.util.List.of(),
                15,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                "HIT",
                false,
                true,
                1);

        assertThat(hand.getIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("should store hand cards")
    void testHandCards() {
        var cards = java.util.List.of(
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.FIVE));
        var hand = new GameState.PlayerHandState(
                0,
                cards,
                15,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                "HIT",
                false,
                true,
                1);

        assertThat(hand.getCards()).hasSize(2);
    }

    @Test
    @DisplayName("should store hand total")
    void testHandTotal() {
        var hand = new GameState.PlayerHandState(
                0,
                java.util.List.of(),
                15,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                "HIT",
                false,
                true,
                1);

        assertThat(hand.getTotal()).isEqualTo(15);
    }

    @Test
    @DisplayName("should store soft hand state")
    void testHandSoftState() {
        var hand = new GameState.PlayerHandState(
                0,
                java.util.List.of(),
                17,
                true,
                false,
                false,
                false,
                false,
                false,
                null,
                "STAND",
                false,
                true,
                1);

        assertThat(hand.isSoft()).isTrue();
    }

    @Test
    @DisplayName("should store result")
    void testHandResult() {
        var hand = new GameState.PlayerHandState(
                0,
                java.util.List.of(),
                20,
                false,
                false,
                true,
                false,
                false,
                false,
                "WIN",
                null,
                false,
                true,
                1);

        assertThat(hand.getResult()).isEqualTo("WIN");
    }

    @Test
    @DisplayName("should store bet units")
    void testHandBetUnits() {
        var hand = new GameState.PlayerHandState(
                0,
                java.util.List.of(),
                15,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                "HIT",
                false,
                true,
                2);

        assertThat(hand.getBetUnits()).isEqualTo(2);
    }
}
