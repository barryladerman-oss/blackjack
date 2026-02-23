package us.laderman.blackjack.service;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Hand;
import us.laderman.blackjack.model.HandValue;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Shoe;
import us.laderman.blackjack.odds.OddsCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core logic for managing a Blackjack game session.
 * Manages the state and logic of a single Blackjack game instance.
 * Handles player actions, dealer logic, and game state transitions.
 */

public class BlackjackGame {
    private final UUID id;
    private final Shoe shoe;
    private final List<Hand> playerHands = new ArrayList<>();
    private final Hand dealerHand = new Hand();
    private final int decksTotal;
    private int runningCount;
    private int cardsDealt;
    private GamePhase phase = GamePhase.PLAYER_TURN;

    /**
     * Constructs a new BlackjackGame with the specified number of hands and decks.
     * 
     * @param hands Number of player hands
     * @param decks Number of decks in the shoe
     */
    public BlackjackGame(int hands, int decks) {
        this(hands, decks, new java.util.HashMap<>(), null, null);
    }

    /**
     * Constructs a new BlackjackGame with specific cards for players and dealer.
     * 
     * @param hands           Number of player hands
     * @param decks           Number of decks in the shoe
     * @param playerHandCards Map of hand index to specific cards
     * @param dealerCard1     Dealer's first card
     * @param dealerCard2     Dealer's second card
     */
    public BlackjackGame(int hands, int decks, java.util.Map<Integer, BlackjackService.HandCards> playerHandCards,
            Card dealerCard1, Card dealerCard2) {
        this.id = UUID.randomUUID();
        this.decksTotal = decks;
        this.shoe = new Shoe(decks);

        // Remove specific cards from shoe before dealing
        for (BlackjackService.HandCards hc : playerHandCards.values()) {
            if (hc.card1 != null) {
                shoe.removeSpecificCard(hc.card1);
            }
            if (hc.card2 != null) {
                shoe.removeSpecificCard(hc.card2);
            }
        }
        if (dealerCard1 != null) {
            shoe.removeSpecificCard(dealerCard1);
        }
        if (dealerCard2 != null) {
            shoe.removeSpecificCard(dealerCard2);
        }

        for (int i = 0; i < hands; i++) {
            playerHands.add(new Hand());
        }
        dealInitial(playerHandCards, dealerCard1, dealerCard2);
        autoStandBlackjacks();
    }

    /**
     * Returns the unique game ID.
     * 
     * @return UUID of the game
     */
    public UUID getId() {
        return id;
    }

    /**
     * Performs a hit action for the specified hand.
     * 
     * @param handIndex    Index of the hand to hit
     * @param selectedCard Specific card to deal, or null for random
     * @return Updated game state
     */
    public GameState hit(int handIndex, Card selectedCard) {
        ensurePlayerTurn();
        Hand hand = getPlayerHand(handIndex);
        if (hand.isStanding() || hand.isBusted()) {
            return toGameState();
        }

        // Check if insurance decisions are pending
        if (dealerHasAce() && !allHandsResolvedInsurance()) {
            throw new IllegalStateException("All hands must make an insurance decision first");
        }
        if (!hand.canHitAfterSplitAces()) {
            throw new IllegalArgumentException("Cannot hit after split aces - only one card dealt per ace");
        }
        if (selectedCard != null) {
            if (!shoe.removeSpecificCard(selectedCard)) {
                throw new IllegalArgumentException(
                        "Selected card not available: " + selectedCard.getRank().getLabel() + " of "
                                + selectedCard.getSuit().getLabel());
            }
            dealSpecificCard(hand, selectedCard);
        } else {
            dealCard(hand);
        }
        if (hand.isBusted() || hand.getValue().getTotal() == 21) {
            hand.setStanding(true);
        }
        finishIfReady();
        return toGameState();
    }

    /**
     * Performs a stand action for the specified hand.
     * 
     * @param handIndex Index of the hand to stand
     * @return Updated game state
     */
    public GameState stand(int handIndex) {
        ensurePlayerTurn();
        Hand hand = getPlayerHand(handIndex);

        // Check if insurance decisions are pending
        if (dealerHasAce() && !allHandsResolvedInsurance()) {
            throw new IllegalStateException("All hands must make an insurance decision first");
        }
        hand.setStanding(true);
        finishIfReady();
        return toGameState();
    }

    /**
     * Performs a double down action for the specified hand.
     * 
     * @param handIndex    Index of the hand to double down
     * @param selectedCard Specific card to deal, or null for random
     * @return Updated game state
     */
    public GameState doubleDown(int handIndex, Card selectedCard) {
        if (phase != GamePhase.PLAYER_TURN) {
            throw new IllegalStateException("Cannot double down outside player turn");
        }
        Hand hand = getPlayerHand(handIndex);
        if (hand.isStanding() || hand.isBusted()) {
            throw new IllegalStateException("Cannot double down on a completed hand");
        }

        // Check if insurance decisions are pending
        if (dealerHasAce() && !allHandsResolvedInsurance()) {
            throw new IllegalStateException("All hands must make an insurance decision first");
        }

        // Allow doubling on split hands, except split aces
        if (hand.isFromSplit() && hand.getSplitFromRank() == Rank.ACE) {
            throw new IllegalArgumentException("Cannot double on split aces");
        }
        if (hand.getCards().size() != 2) {
            throw new IllegalArgumentException("Double requires exactly two cards");
        }

        // Prevent double down on soft hands
        if (hand.getValue().isSoft()) {
            throw new IllegalArgumentException("Double down is not allowed on soft hands");
        }
        hand.setBetUnits(2);

        // Deal specific or random card
        if (selectedCard != null) {
            hand.addCard(selectedCard);
        } else {
            dealCard(hand);
        }

        hand.setStanding(true);
        finishIfReady();
        return toGameState();
    }

    /**
     * Splits the specified hand, dealing specific cards to each new hand.
     * 
     * @param handIndex Index of the hand to split
     * @param card1     Card for original hand
     * @param card2     Card for new hand
     * @return Updated game state
     */
    public GameState split(int handIndex, Card card1, Card card2) {
        return split(handIndex, card1, card2, null, null);
    }

    /**
     * Splits the specified hand, optionally dealing specific ranks to each new
     * hand.
     * 
     * @param handIndex Index of the hand to split
     * @param card1     Card for original hand
     * @param card2     Card for new hand
     * @param rank1     Rank for original hand
     * @param rank2     Rank for new hand
     * @return Updated game state
     */
    public GameState split(int handIndex, Card card1, Card card2, Rank rank1, Rank rank2) {
        ensurePlayerTurn();
        Hand hand = getPlayerHand(handIndex);
        if (hand.isStanding() || hand.isBusted()) {
            return toGameState();
        }
        // Check if insurance decisions are pending
        if (dealerHasAce() && !allHandsResolvedInsurance()) {
            throw new IllegalStateException("All hands must make an insurance decision first");
        }
        if (!hand.canSplit()) {
            throw new IllegalArgumentException("Hand cannot be split");
        }

        // Don't allow a split hand to be split again
        if (hand.isFromSplit()) {
            throw new IllegalArgumentException("Cannot split a hand that was already split");
        }

        System.out.println("DEBUG: Split called with card1=" + card1 + ", card2=" + card2
                + ", rank1=" + rank1 + ", rank2=" + rank2);

        Card moved = hand.splitOff();
        hand.setIsFromSplit(true);
        Hand newHand = new Hand();
        newHand.setIsFromSplit(true);

        // Track the rank that was split
        Rank splitRank = hand.getCards().get(0).getRank();
        hand.setSplitFromRank(splitRank);
        newHand.setSplitFromRank(splitRank);

        // If splitting aces, restrict to one card per ace
        boolean isAceHand = hand.getCards().get(0).getRank() == Rank.ACE;
        System.out.println("DEBUG: After splitOff, hand has " + hand.getCards().size() + " cards");
        System.out.println(
                "DEBUG: Card 0: " + hand.getCards().get(0).getRank() + " of " + hand.getCards().get(0).getSuit());
        System.out.println("DEBUG: isAceHand=" + isAceHand);
        if (isAceHand) {
            hand.setCanHitAfterSplitAces(false);
            newHand.setCanHitAfterSplitAces(false);
        }

        newHand.addCard(moved);
        playerHands.add(handIndex + 1, newHand);

        // Deal cards to both hands
        if (card1 != null) {
            System.out.println("DEBUG: Dealing specific card1: " + card1.getRank() + " of " + card1.getSuit());
            if (!shoe.removeSpecificCard(card1)) {
                throw new IllegalArgumentException(
                        "Selected card not available: " + card1.getRank().getLabel() + " of "
                                + card1.getSuit().getLabel());
            }
            dealSpecificCard(hand, card1);
        } else if (rank1 != null) {
            System.out.println("DEBUG: Dealing specific rank1: " + rank1);
            Card drawnCard = shoe.drawSpecificRank(rank1);
            if (drawnCard == null) {
                throw new IllegalArgumentException("No card with rank " + rank1.getLabel() + " available");
            }
            dealSpecificCard(hand, drawnCard);
        } else {
            System.out.println("DEBUG: Dealing random card to original hand");
            dealCard(hand);
        }

        if (card2 != null) {
            System.out.println("DEBUG: Dealing specific card2: " + card2.getRank() + " of " + card2.getSuit());
            if (!shoe.removeSpecificCard(card2)) {
                throw new IllegalArgumentException(
                        "Selected card not available: " + card2.getRank().getLabel() + " of "
                                + card2.getSuit().getLabel());
            }
            dealSpecificCard(newHand, card2);
        } else if (rank2 != null) {
            System.out.println("DEBUG: Dealing specific rank2: " + rank2);
            Card drawnCard = shoe.drawSpecificRank(rank2);
            if (drawnCard == null) {
                throw new IllegalArgumentException("No card with rank " + rank2.getLabel() + " available");
            }
            dealSpecificCard(newHand, drawnCard);
        } else {
            System.out.println("DEBUG: Dealing random card to new hand");
            dealCard(newHand);
        }

        if (isAceHand) {
            hand.setStanding(true);
            newHand.setStanding(true);
            System.out.println("DEBUG SPLIT ACES: Set both hands to standing");
            System.out.println("DEBUG SPLIT ACES: Hand 0 after standing: standing=" + playerHands.get(0).isStanding());
            System.out.println("DEBUG SPLIT ACES: Hand 1 after standing: standing=" + playerHands.get(1).isStanding());
            System.out.println("DEBUG SPLIT ACES: Now calling finishIfReady()...");
        }

        finishIfReady();
        System.out.println("DEBUG SPLIT: About to return state with phase=" + phase);
        System.out.println("DEBUG SPLIT: Hand 0: standing=" + playerHands.get(0).isStanding() + ", busted="
                + playerHands.get(0).isBusted());
        if (playerHands.size() > 1) {
            System.out.println("DEBUG SPLIT: Hand 1: standing=" + playerHands.get(1).isStanding() + ", busted="
                    + playerHands.get(1).isBusted());
        }
        return toGameState();
    }

    /**
     * Offers insurance to the specified hand.
     * 
     * @param handIndex Index of the hand
     * @return Updated game state
     */
    public GameState insurance(int handIndex) {
        ensurePlayerTurn();
        Hand hand = getPlayerHand(handIndex);
        if (hand.isStanding() || hand.isBusted() || hand.isInsured()) {
            return toGameState();
        }
        if (hand.isFromSplit()) {
            throw new IllegalArgumentException("Insurance not available for split hands");
        }

        // Insurance available only on first 2 cards
        if (hand.getCards().size() != 2) {
            throw new IllegalArgumentException("Insurance only available with exactly two cards");
        }

        // Dealer must be showing an Ace
        Card dealerUpCard = dealerHand.getCards().isEmpty() ? null : dealerHand.getCards().get(0);
        if (dealerUpCard == null || dealerUpCard.getRank() != Rank.ACE) {
            throw new IllegalArgumentException("Insurance only available when dealer shows an Ace");
        }
        hand.setInsured(true);
        hand.setInsuranceDeclined(false);

        // Check if dealer has blackjack (21 with exactly 2 cards)
        HandValue dealerValue = dealerHand.getValue();
        if (dealerValue.getTotal() == 21 && dealerHand.getCards().size() == 2) {
            hand.setInsuranceWon(true);

            // If dealer has blackjack, check if all hands have resolved insurance
            if (allHandsResolvedInsurance()) {
                completeRound();
            }
        }
        return toGameState();
    }

    /**
     * Declines insurance for the specified hand.
     * 
     * @param handIndex Index of the hand
     * @return Updated game state
     */
    public GameState declineInsurance(int handIndex) {
        ensurePlayerTurn();
        Hand hand = getPlayerHand(handIndex);
        if (hand.isStanding() || hand.isBusted() || hand.isInsured() || hand.isInsuranceDeclined()) {
            return toGameState();
        }
        if (hand.isFromSplit()) {
            throw new IllegalArgumentException("Insurance not available for split hands");
        }

        // Insurance available only on first 2 cards
        if (hand.getCards().size() != 2) {
            throw new IllegalArgumentException("Insurance decision only available with exactly two cards");
        }

        // Dealer must be showing an Ace
        Card dealerUpCard = dealerHand.getCards().isEmpty() ? null : dealerHand.getCards().get(0);
        if (dealerUpCard == null || dealerUpCard.getRank() != Rank.ACE) {
            throw new IllegalArgumentException("Insurance decision only available when dealer shows an Ace");
        }
        hand.setInsuranceDeclined(true);

        // If dealer has blackjack, end round once all hands have resolved insurance
        HandValue dealerValue = dealerHand.getValue();
        if (dealerValue.getTotal() == 21 && dealerHand.getCards().size() == 2) {
            if (allHandsResolvedInsurance()) {
                completeRound();
            }
        }
        return toGameState();
    }

    /**
     * Returns the current game state.
     * 
     * @return GameState object
     */
    public GameState getState() {
        return toGameState();
    }

    /**
     * Checks if all hands have resolved insurance decisions.
     * 
     * @return true if all hands resolved insurance
     */
    private boolean allHandsResolvedInsurance() {
        // Check if all hands that need to make an insurance decision have made one
        // A hand has resolved insurance if either:
        // 1. The hand is not playable (busted, standing, blackjack)
        // 2. The hand has been offered insurance and either took it or ignored it
        // 3. The hand was created by a split (split hands are not offered insurance)
        HandValue dealerValue = dealerHand.getValue();
        if (dealerValue.getTotal() != 21 || dealerHand.getCards().size() != 2) {
            return true; // Dealer doesn't have blackjack, insurance not applicable
        }

        for (Hand hand : playerHands) {
            if (!hand.isBusted() && !hand.isStanding()) {
                // Skip split hands - they are not offered insurance
                if (hand.isFromSplit()) {
                    continue;
                }
                // This hand is still playing and hasn't resolved insurance yet
                if (hand.getCards().size() == 2 && !hand.isInsured() && !hand.isInsuranceDeclined()) {
                    // Hand has 2 cards but hasn't made insurance decision
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Completes the round immediately when dealer has blackjack.
     */
    private void completeRound() {
        // End the round immediately when dealer has blackjack
        dealerHand.setStanding(true);
        phase = GamePhase.COMPLETE;
    }

    /**
     * Checks if the dealer's up card is an Ace.
     * 
     * @return true if dealer has Ace
     */
    private boolean dealerHasAce() {
        Card dealerUpCard = getDealerUpCard();
        return dealerUpCard != null && dealerUpCard.getRank() == Rank.ACE;
    }

    /**
     * Ensures the current phase is PLAYER_TURN.
     * Throws exception if not.
     */
    private void ensurePlayerTurn() {
        if (phase != GamePhase.PLAYER_TURN) {
            throw new IllegalStateException("Player actions are closed");
        }
    }

    /**
     * Returns the player hand at the specified index.
     * 
     * @param index Hand index
     * @return Hand object
     */
    private Hand getPlayerHand(int index) {
        if (index < 0 || index >= playerHands.size()) {
            throw new IllegalArgumentException("Invalid hand index");
        }
        return playerHands.get(index);
    }

    /**
     * Returns the dealer's up card.
     * 
     * @return Dealer's up card, or null if none
     */
    private Card getDealerUpCard() {
        List<Card> cards = dealerHand.getCards();
        return cards.isEmpty() ? null : cards.get(0);
    }

    /**
     * Deals initial cards to all player hands and dealer.
     * 
     * @param playerHandCards Map of hand index to specific cards
     * @param dealerCard1     Dealer's first card
     * @param dealerCard2     Dealer's second card
     */
    private void dealInitial(java.util.Map<Integer, BlackjackService.HandCards> playerHandCards, Card dealerCard1,
            Card dealerCard2) {
        // Deal first card to each player hand
        for (int i = 0; i < playerHands.size(); i++) {
            Hand hand = playerHands.get(i);
            BlackjackService.HandCards cards = playerHandCards.get(i);
            if (cards != null && cards.card1 != null) {
                dealSpecificCard(hand, cards.card1);
            } else {
                dealCard(hand);
            }
        }

        // Deal first card to dealer (up card)
        if (dealerCard1 != null) {
            dealSpecificCard(dealerHand, dealerCard1);
        } else {
            dealCard(dealerHand);
        }

        // Deal second card to each player hand
        for (int i = 0; i < playerHands.size(); i++) {
            Hand hand = playerHands.get(i);
            BlackjackService.HandCards cards = playerHandCards.get(i);
            if (cards != null && cards.card2 != null) {
                dealSpecificCard(hand, cards.card2);
            } else {
                dealCard(hand);
            }
        }

        // Deal second card to dealer (hole card, hidden until round complete)
        if (dealerCard2 != null) {
            dealSpecificCard(dealerHand, dealerCard2);
        } else {
            dealCard(dealerHand);
        }

        // Check for dealer blackjack immediately after dealing
        if (dealerHand.getCards().size() == 2) {
            HandValue dealerValue = dealerHand.getValue();
            if (dealerValue.getTotal() == 21) {
                completeRound();
                return;
            }
        }

        // If dealer shows an ace, delay showing first hand action until all insurance
        // decisions are complete
        if (dealerHasAce()) {
            // Do not auto-stand blackjacks or show first hand action until insurance
            // decisions are resolved
            return;
        }
        autoStandBlackjacks();
    }

    /**
     * Deals a specific card to the given hand and updates count.
     * 
     * @param hand Target hand
     * @param card Card to deal
     */
    private void dealSpecificCard(Hand hand, Card card) {
        cardsDealt++;
        updateCount(card);
        hand.addCard(card);
    }

    /**
     * Automatically stands hands with blackjack (21 with 2 cards).
     */
    private void autoStandBlackjacks() {
        for (Hand hand : playerHands) {
            if (hand.getCards().size() == 2 && hand.getValue().getTotal() == 21) {
                hand.setStanding(true);
            }
        }
        finishIfReady();
    }

    /**
     * Deals a random card to the given hand and updates count.
     * 
     * @param hand Target hand
     */
    private void dealCard(Hand hand) {
        Card card = shoe.draw();
        cardsDealt++;
        updateCount(card);
        hand.addCard(card);
    }

    /**
     * Updates the running count based on the card dealt.
     * 
     * @param card Card dealt
     */
    private void updateCount(Card card) {
        Rank rank = card.getRank();
        switch (rank) {
            case TWO, THREE, FOUR, FIVE, SIX -> runningCount += 1;
            case SEVEN, EIGHT, NINE -> runningCount += 0;
            case TEN, JACK, QUEEN, KING, ACE -> runningCount -= 1;
        }
    }

    /**
     * Checks if all hands are finished and triggers dealer play if so.
     */
    private void finishIfReady() {
        System.out.println("DEBUG finishIfReady: Checking " + playerHands.size() + " hands...");
        for (int i = 0; i < playerHands.size(); i++) {
            Hand hand = playerHands.get(i);
            System.out.println("DEBUG finishIfReady: Hand " + i + " - standing=" + hand.isStanding() + ", busted="
                    + hand.isBusted());
            if (!hand.isStanding() && !hand.isBusted()) {
                System.out.println("DEBUG finishIfReady: Found playable hand at index " + i + ", returning early");
                return;
            }
        }
        System.out
                .println("DEBUG finishIfReady: No playable hands, calling playDealer() and setting phase to COMPLETE");
        playDealer();
        phase = GamePhase.COMPLETE;
    }

    /**
     * Plays the dealer's turn according to blackjack rules.
     */
    private void playDealer() {
        phase = GamePhase.DEALER_TURN;
        while (true) {
            HandValue value = dealerHand.getValue();
            // Dealer hits on soft 17 (has an ace counting as 11)
            boolean isSoft17 = value.isSoft() && value.getTotal() == 17;
            if (value.getTotal() < 17 || isSoft17) {
                dealCard(dealerHand);
                continue;
            }
            break;
        }
        dealerHand.setStanding(true);
    }

    /**
     * Converts the current game state to a GameState object for UI/API.
     * 
     * @return GameState object
     */
    private GameState toGameState() {
        Map<String, Double> odds = getOdds(shoe, playerHands, 0, getDealerUpCard());
        System.out.println("DEBUG: Odds for current state: " + odds);

        HandValue dealerValue = dealerHand.getValue();
        int cardsRemaining = shoe.remaining();
        double decksRemaining = Math.max(0.25, (decksTotal * 52.0 - cardsDealt) / 52.0); // keep for trueCount
                                                                                         // calculation
        double trueCount = runningCount / decksRemaining;

        // Calculate net units (sum of wins/losses weighted by bet units)
        double handResults = 0;
        double insuranceTotal = 0;
        for (Hand hand : playerHands) {
            double resultValue = getResultValue(hand, hand.getValue().getTotal(), hand.isBusted(), dealerValue);
            handResults += resultValue * hand.getBetUnits();
            // Insurance handling: costs 0.5 units per bet unit, pays 2:1 if dealer has
            // blackjack
            if (hand.isInsured()) {
                double betUnits = hand.getBetUnits();
                double insuranceCost = 0.5 * betUnits; // Cost of insurance (half the bet)
                double insuranceGainLoss = -insuranceCost;
                if (hand.isInsuranceWon()) {
                    // Payout is 2:1 on insurance amount, so 2 * insuranceCost
                    insuranceGainLoss += 2 * insuranceCost;
                }
                insuranceTotal += insuranceGainLoss;
            }
        }
        double netUnits = handResults + insuranceTotal;

        // Only show dealer's up card during player turn; reveal hole card when round is
        // complete
        List<Card> dealerCards = dealerHand.getCards();
        // Always send both cards (up card + hole card), UI will handle showing hole
        // card as back
        List<Card> visibleDealerCards = dealerCards;

        // Only show dealer's actual total when round is complete; show up card value
        // only during player turn
        int displayDealerTotal = (phase == GamePhase.COMPLETE || phase == GamePhase.DEALER_TURN)
                ? dealerValue.getTotal()
                : (dealerCards.isEmpty() ? 0 : dealerCards.get(0).getRank().getValue());

        boolean displayDealerSoft = (phase == GamePhase.COMPLETE || phase == GamePhase.DEALER_TURN)
                ? dealerValue.isSoft()
                : false;

        GameState.DealerState dealerState = new GameState.DealerState(
                visibleDealerCards,
                displayDealerTotal,
                displayDealerSoft,
                dealerHand.isBusted(),
                dealerHand.isStanding());

        List<GameState.PlayerHandState> handStates = new ArrayList<>();
        Card dealerUpCard = dealerHand.getCards().isEmpty() ? null : dealerHand.getCards().get(0);
        for (int i = 0; i < playerHands.size(); i++) {
            Hand hand = playerHands.get(i);
            HandValue value = hand.getValue();
            String result = resolveResult(hand, value.getTotal(), hand.isBusted(), dealerValue);
            String recommended = recommendAction(i, playerHands, value, dealerUpCard, trueCount);

            handStates.add(new GameState.PlayerHandState(
                    i,
                    hand.getCards(),
                    value.getTotal(),
                    value.isSoft(),
                    hand.isBusted(),
                    hand.isStanding(),
                    hand.isInsured(),
                    hand.isInsuranceDeclined(),
                    hand.isInsuranceWon(),
                    result,
                    recommended,
                    hand.isFromSplit(),
                    hand.canHitAfterSplitAces(),
                    hand.getBetUnits()));
        }

        String note;
        if (phase == GamePhase.COMPLETE) {
            if (insuranceTotal != 0) {
                note = "Round complete | Insurance: " + roundToTwo(insuranceTotal);
            } else if (getDealerUpCard() != null && getDealerUpCard().getRank() == Rank.ACE) {
                note = "Round complete | insurance declined or not taken";
            } else {
                note = "Round complete";
            }
        } else if (getDealerUpCard() != null && getDealerUpCard().getRank() == Rank.ACE) {
            note = "Insurance available on dealer ace";
        } else {
            note = "Dealer up card used for recommendations";
        }

        return new GameState(
                id.toString(),
                phase.name(),
                dealerState,
                handStates,
                runningCount,
                roundToTwo(trueCount),
                cardsRemaining,
                roundToTwo(netUnits),
                roundToTwo(insuranceTotal),
                note);
    }

    /**
     * Resolves the result for a player hand.
     * 
     * @param hand         Player hand
     * @param playerTotal  Player's total value
     * @param playerBusted Whether player busted
     * @param dealerValue  Dealer's hand value
     * @return Result string (WIN, LOSE, PUSH, IN_PROGRESS)
     */
    private String resolveResult(Hand hand, int playerTotal, boolean playerBusted, HandValue dealerValue) {
        String result = getResultString(playerTotal, playerBusted, dealerValue);
        return (phase == GamePhase.COMPLETE) ? result : "IN_PROGRESS";
    }

    /**
     * Checks if the hand at the given index is active for player actions.
     * 
     * @param handIndex Index of the hand
     * @return true if hand is active
     */
    private boolean isActiveHand(int handIndex) {
        if (phase != GamePhase.PLAYER_TURN) {
            return false;
        }
        for (int i = 0; i < handIndex; i++) {
            Hand prior = playerHands.get(i);
            if (!prior.isStanding() && !prior.isBusted()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the result string for a hand (WIN, LOSE, PUSH).
     * 
     * @param playerTotal  Player's total value
     * @param playerBusted Whether player busted
     * @param dealerValue  Dealer's hand value
     * @return Result string
     */
    private String getResultString(int playerTotal, boolean playerBusted, HandValue dealerValue) {
        if (playerBusted) {
            return "LOSE";
        }
        if (dealerHand.isBusted()) {
            return "WIN";
        }
        if (playerTotal > dealerValue.getTotal()) {
            return "WIN";
        }
        if (playerTotal < dealerValue.getTotal()) {
            return "LOSE";
        }
        return "PUSH";
    }

    /**
     * Returns the result value for a hand (1 for win, -1 for loss, 1.5 for
     * blackjack win, 0 for push).
     * 
     * @param hand         Player hand
     * @param playerTotal  Player's total value
     * @param playerBusted Whether player busted
     * @param dealerValue  Dealer's hand value
     * @return Result value
     */
    private double getResultValue(Hand hand, int playerTotal, boolean playerBusted, HandValue dealerValue) {
        // Check if player hand is a blackjack (21 with exactly 2 cards)
        boolean isPlayerBlackjack = playerTotal == 21 && hand.getCards().size() == 2;
        boolean isDealerBlackjack = dealerValue.getTotal() == 21 && dealerHand.getCards().size() == 2;

        String result = getResultString(playerTotal, playerBusted, dealerValue);

        // Blackjack pays 1.5:1
        if (isPlayerBlackjack && result.equals("WIN") && !isDealerBlackjack) {
            return 1.5;
        }

        return switch (result) {
            case "WIN" -> 1;
            case "LOSE" -> -1;
            case "PUSH" -> 0;
            default -> 0;
        };
    }

    private Map<String, Double> getOdds(Shoe shoe, List<Hand> playerHands, int handIndex, Card dealerUpCard) {
        Shoe testShoe = new Shoe(shoe);
        Hand playerHand = playerHands.get(handIndex);

        if (dealerUpCard.getRank() == Rank.TEN ||
                dealerUpCard.getRank() == Rank.JACK ||
                dealerUpCard.getRank() == Rank.QUEEN ||
                dealerUpCard.getRank() == Rank.KING) {
            for (Card card : testShoe.getCards()) {
                if (card.getRank() == Rank.ACE) {
                    testShoe.removeSpecificCard(card);
                }
            }
        }
        if (dealerUpCard.getRank() == Rank.ACE) {
            for (Card card : testShoe.getCards()) {
                if (card.getRank() == Rank.TEN) {
                    testShoe.removeSpecificCard(card);
                }
            }
        }
        Map<String, Double> odds = OddsCalculator.calculateOdds(testShoe, playerHand, dealerUpCard);
        return odds;
    }

    /**
     * Recommends the next action for a player hand based on basic and advanced
     * strategy.
     * 
     * @param handIndex    Index of the hand
     * @param hand         Player hand
     * @param value        Hand value
     * @param dealerUpCard Dealer's up card
     * @param trueCount    Current true count
     * @return Recommended action string
     */
    private String recommendAction(int handIndex, List<Hand> playerHands, HandValue value, Card dealerUpCard,
            double trueCount) {
        Hand hand = playerHands.get(handIndex);

        // Allow insurance recommendation for all eligible hands when dealer shows Ace
        boolean dealerShowsAce = phase == GamePhase.PLAYER_TURN && dealerUpCard != null
                && dealerUpCard.getRank() == Rank.ACE;
        boolean eligibleForInsurance = dealerShowsAce && !hand.isFromSplit() && hand.getCards().size() == 2
                && !hand.isInsured() && !hand.isInsuranceDeclined() && !hand.isStanding() && !hand.isBusted();
        if (!isActiveHand(handIndex) && !eligibleForInsurance) {
            return "NONE";
        }

        // Check for insurance opportunity first
        if (phase == GamePhase.PLAYER_TURN && !hand.isFromSplit() && dealerUpCard != null
                && dealerUpCard.getRank() == Rank.ACE) {
            if (hand.getCards().size() == 2 && !hand.isInsured() && !hand.isInsuranceDeclined()
                    && !hand.isStanding() && !hand.isBusted()) {
                // Recommend insurance when true count is high (2 or higher)
                if (trueCount >= 2) {
                    return "INSURE";
                }
                return "NO_INSURANCE";
            }
        }

        if (phase != GamePhase.PLAYER_TURN || hand.isBusted() || hand.isStanding()) {
            return "NONE";
        }
        // Don't recommend actions on blackjack (natural 21 with 2 cards)
        if (!hand.isFromSplit() && hand.getCards().size() == 2 && value.getTotal() == 21) {
            return "NONE";
        }

        // Get basic strategy recommendation
        int dealerUpValue = dealerUpCard == null ? 10 : dealerUpCard.getRank().getValue();
        boolean allowSplit = !hand.isFromSplit() && hand.canSplit();
        String recommendation = Strategy.getRecommendedAction(hand, dealerUpValue, allowSplit);

        // Apply count-based deviations for a more sophisticated strategy
        recommendation = Strategy.applyCountDeviation(recommendation, hand, dealerUpValue, trueCount);

        if (hand.isFromSplit()) {
            if ("INSURE".equals(recommendation) || "NO_INSURANCE".equals(recommendation)
                    || "DOUBLE".equals(recommendation) || "SPLIT".equals(recommendation)) {
                String fallback = Strategy.getRecommendedAction(hand, dealerUpValue, false);
                if ("DOUBLE".equals(fallback) || "SPLIT".equals(fallback)) {
                    return "HIT";
                }
                return fallback;
            }
        }

        if ((recommendation == null || recommendation.isBlank() || "NONE".equals(recommendation))
                && phase == GamePhase.PLAYER_TURN
                && !hand.isStanding() && !hand.isBusted()) {
            String fallback = Strategy.getRecommendedAction(hand, dealerUpValue, false);
            if ("DOUBLE".equals(fallback) || "SPLIT".equals(fallback)) {
                return "HIT";
            }
            return fallback;
        }
        return recommendation;
    }

    /**
     * Rounds a double value to two decimal places.
     * 
     * @param value Value to round
     * @return Rounded value
     */
    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
