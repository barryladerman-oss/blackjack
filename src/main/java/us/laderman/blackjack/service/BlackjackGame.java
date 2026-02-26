package us.laderman.blackjack.service;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GamePhase;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Hand;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.model.Shoe;
import us.laderman.blackjack.strategy.Odds;
import us.laderman.blackjack.strategy.OddsCalculator;
import us.laderman.blackjack.strategy.Strategy;
import us.laderman.blackjack.BlackjackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core logic for managing a Blackjack game session.
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

    // Rule settings
    private final boolean allowDoubleOn9;
    private final boolean dealerHitSoft17;
    private final boolean allowDoubleOnSplit;

    /**
     * Constructs a new BlackjackGame with the specified number of hands and decks.
     *
     * @param hands the number of player hands
     * @param decks the number of decks in the shoe
     */

    public BlackjackGame(int hands, int decks) {
        this(hands, decks, new java.util.HashMap<>(), null, null, true, true, true);
    }

    // Overloads for test compatibility
    public BlackjackGame(int hands, int decks, java.util.HashMap<Integer, BlackjackService.HandCards> playerHandCards,
            Card dealerCard1, Card dealerCard2) {
        this(hands, decks, (java.util.Map<Integer, BlackjackService.HandCards>) playerHandCards, dealerCard1,
                dealerCard2, true, true, true);
    }

    public BlackjackGame(int hands, int decks, java.util.HashMap<Integer, BlackjackService.HandCards> playerHandCards,
            Card dealerCard1, Card dealerCard2, boolean allowDoubleOn9, boolean dealerHitSoft17) {
        this(hands, decks, (java.util.Map<Integer, BlackjackService.HandCards>) playerHandCards, dealerCard1,
                dealerCard2, allowDoubleOn9, dealerHitSoft17, true);
    }

    public BlackjackGame(int hands, int decks, java.util.Map<Integer, BlackjackService.HandCards> playerHandCards,
            Card dealerCard1, Card dealerCard2) {
        this(hands, decks, playerHandCards, dealerCard1, dealerCard2, true, true, true);
    }

    public BlackjackGame(int hands, int decks, java.util.Map<Integer, BlackjackService.HandCards> playerHandCards,
            Card dealerCard1, Card dealerCard2, boolean allowDoubleOn9, boolean dealerHitSoft17) {
        this(hands, decks, playerHandCards, dealerCard1, dealerCard2, allowDoubleOn9, dealerHitSoft17, true);
    }

    /**
     * Constructs a new BlackjackGame with specific cards for players and dealer.
     *
     * @param hands           the number of player hands
     * @param decks           the number of decks in the shoe
     * @param playerHandCards map of hand index to specific cards
     * @param dealerCard1     dealer's first card
     * @param dealerCard2     dealer's second card
     */
    public BlackjackGame(int hands, int decks, java.util.Map<Integer, BlackjackService.HandCards> playerHandCards,
            Card dealerCard1, Card dealerCard2, boolean allowDoubleOn9, boolean dealerHitSoft17,
            boolean allowDoubleOnSplit) {
        this.id = UUID.randomUUID();
        this.decksTotal = decks;
        this.shoe = new Shoe(decks);
        this.runningCount = 0;
        this.allowDoubleOn9 = allowDoubleOn9;
        this.dealerHitSoft17 = dealerHitSoft17;
        this.allowDoubleOnSplit = allowDoubleOnSplit;

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
        // Recalculate running count for all cards already dealt
        for (Hand hand : playerHands) {
            for (Card card : hand.getCards()) {
                updateCount(card);
            }
        }
        for (Card card : dealerHand.getCards()) {
            updateCount(card);
        }
        autoStandBlackjacks();
    }

    /**
     * Returns the unique game ID for this session.
     *
     * @return the UUID of the game
     */
    public UUID getId() {
        return id;
    }

    /**
     * Performs a hit action for the specified hand.
     *
     * @param handIndex    the index of the hand to hit
     * @param selectedCard the specific card to deal, or null for random
     * @return the updated game state
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
        if (hand.isBusted() || hand.getValue() == 21) {
            hand.setStanding(true);
        }
        finishIfReady();
        return toGameState();
    }

    /**
     * Performs a stand action for the specified hand.
     *
     * @param handIndex the index of the hand to stand
     * @return the updated game state
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
     * @param handIndex    the index of the hand to double down
     * @param selectedCard the specific card to deal, or null for random
     * @return the updated game state
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
        if (hand.isFromSplit()) {
            if (hand.getSplitFromRank() == Rank.ACE) {
                throw new IllegalArgumentException("Cannot double on split aces");
            }
            if (!allowDoubleOnSplit) {
                throw new IllegalArgumentException("Double after split is not allowed by rule");
            }
        }
        if (hand.getCards().size() != 2) {
            throw new IllegalArgumentException("Double requires exactly two cards");
        }

        // Prevent double down on soft hands
        if (hand.isSoft()) {
            throw new IllegalArgumentException("Double down is not allowed on soft hands");
        }

        // Only allow double on 9 if rule is enabled
        if (hand.getValue() == 9 && !allowDoubleOn9) {
            throw new IllegalArgumentException("Double down on 9 is not allowed by rule");
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
     * @param handIndex the index of the hand to split
     * @param card1     card for original hand
     * @param card2     card for new hand
     * @return the updated game state
     */
    public GameState split(int handIndex, Card card1, Card card2) {
        return split(handIndex, card1, card2, null, null);
    }

    /**
     * Splits the specified hand, optionally dealing specific ranks to each new
     * hand.
     *
     * @param handIndex the index of the hand to split
     * @param card1     card for original hand
     * @param card2     card for new hand
     * @param rank1     rank for original hand
     * @param rank2     rank for new hand
     * @return the updated game state
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

        // Remove the second card from the original hand (splitOff returns it)
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
        if (isAceHand) {
            hand.setCanHitAfterSplitAces(false);
            newHand.setCanHitAfterSplitAces(false);
        }

        // Each hand should have the original card plus the newly dealt card
        // hand already has its original card
        // newHand gets the moved card (the other original card)
        newHand.addCard(moved);
        playerHands.add(handIndex + 1, newHand);

        // Now deal a new card to each hand
        if (card1 != null) {
            if (!shoe.removeSpecificCard(card1)) {
                throw new IllegalArgumentException(
                        "Selected card not available: " + card1.getRank().getLabel() + " of "
                                + card1.getSuit().getLabel());
            }
            dealSpecificCard(hand, card1);
        } else if (rank1 != null) {
            Card drawnCard = shoe.drawSpecificRank(rank1);
            if (drawnCard == null) {
                throw new IllegalArgumentException("No card with rank " + rank1.getLabel() + " available");
            }
            dealSpecificCard(hand, drawnCard);
        } else {
            dealCard(hand);
        }

        if (card2 != null) {
            if (!shoe.removeSpecificCard(card2)) {
                throw new IllegalArgumentException(
                        "Selected card not available: " + card2.getRank().getLabel() + " of "
                                + card2.getSuit().getLabel());
            }
            dealSpecificCard(newHand, card2);
        } else if (rank2 != null) {
            Card drawnCard = shoe.drawSpecificRank(rank2);
            if (drawnCard == null) {
                throw new IllegalArgumentException("No card with rank " + rank2.getLabel() + " available");
            }
            dealSpecificCard(newHand, drawnCard);
        } else {
            dealCard(newHand);
        }

        if (isAceHand) {
            hand.setStanding(true);
            newHand.setStanding(true);
        }

        finishIfReady();
        return toGameState();
    }

    /**
     * Offers insurance to the specified hand.
     *
     * @param handIndex the index of the hand
     * @return the updated game state
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

        if (dealerHand.isBlackjack()) {
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
     * @param handIndex the index of the hand
     * @return the updated game state
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

        if (dealerHand.isBlackjack()) {
            if (allHandsResolvedInsurance()) {
                completeRound();
            }
        }
        return toGameState();
    }

    /**
     * Returns the current game state.
     *
     * @return the current GameState object
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
        if (!dealerHand.isBlackjack()) {
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
     * Throws IllegalStateException if not.
     */
    private void ensurePlayerTurn() {
        if (phase != GamePhase.PLAYER_TURN) {
            throw new IllegalStateException("Player actions are closed");
        }
    }

    /**
     * Returns the player hand at the specified index.
     *
     * @param index the hand index
     * @return the Hand object
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
     * @return the dealer's up card, or null if none
     */
    private Card getDealerUpCard() {
        List<Card> cards = dealerHand.getCards();
        return cards.isEmpty() ? null : cards.get(0);
    }

    /**
     * Deals initial cards to all player hands and dealer.
     *
     * @param playerHandCards map of hand index to specific cards
     * @param dealerCard1     dealer's first card
     * @param dealerCard2     dealer's second card
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

        // If dealer shows Ace, delay blackjack check until after insurance decisions
        Card dealerUpCard = getDealerUpCard();
        if (dealerUpCard != null && dealerUpCard.getRank() == Rank.ACE) {
            // Wait for insurance decisions before checking for blackjack
            return;
        }
        // Otherwise, check for dealer blackjack immediately
        if (dealerHand.isBlackjack()) {
            completeRound();
            return;
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
     * @param hand the target hand
     * @param card the card to deal
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
            if (hand.isBlackjack()) {
                hand.setStanding(true);
            }
        }
        finishIfReady();
    }

    /**
     * Deals a random card to the given hand and updates count.
     *
     * @param hand the target hand
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
     * @param card the card dealt
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
        for (int i = 0; i < playerHands.size(); i++) {
            Hand hand = playerHands.get(i);
            if (!hand.isStanding() && !hand.isBusted()) {
                return;
            }
        }
        playDealer();
        phase = GamePhase.COMPLETE;
    }

    /**
     * Plays the dealer's turn according to blackjack rules.
     */
    private void playDealer() {
        phase = GamePhase.DEALER_TURN;
        while (true) {
            boolean isSoft17 = dealerHand.isSoft() && dealerHand.getValue() == 17;
            if (dealerHand.getValue() < 17 || (dealerHitSoft17 && isSoft17)) {
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
     * @return the GameState object
     */
    private GameState toGameState() {
        int cardsRemaining = shoe.remaining();
        double decksRemaining = Math.max(0.25, (decksTotal * 52.0 - cardsDealt) / 52.0); // keep for trueCount
                                                                                         // calculation
        double trueCount = runningCount / decksRemaining;

        // Calculate net units (sum of wins/losses weighted by bet units)
        double handResults = 0;
        double insuranceTotal = 0;
        for (Hand hand : playerHands) {
            double resultValue = getResultValue(hand, dealerHand);
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
        List<Card> visibleDealerCards = dealerCards;

        // Only show dealer's actual total when round is complete; show up card value
        // only during player turn
        int displayDealerTotal = (phase == GamePhase.COMPLETE || phase == GamePhase.DEALER_TURN)
                ? dealerHand.getValue()
                : (dealerCards.isEmpty() ? 0 : dealerCards.get(0).getRank().getValue());

        boolean displayDealerSoft = (phase == GamePhase.COMPLETE || phase == GamePhase.DEALER_TURN)
                ? dealerHand.isSoft()
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
            String result = resolveResult(hand, dealerHand);
            String recommended = recommendAction(hand, dealerUpCard, i, trueCount);

            handStates.add(new GameState.PlayerHandState(
                    i,
                    hand.getCards(),
                    hand.getValue(),
                    hand.isSoft(),
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
                note = "Round complete | Insurance: " + BlackjackUtil.roundToTwo(insuranceTotal);
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
                BlackjackUtil.roundToTwo(trueCount),
                cardsRemaining,
                BlackjackUtil.roundToTwo(netUnits),
                BlackjackUtil.roundToTwo(insuranceTotal),
                note);
    }

    /**
     * Resolves the result for a player hand.
     *
     * @param hand       the player hand
     * @param dealerHand the dealer's hand
     * @return result string (WIN, LOSE, PUSH, IN_PROGRESS)
     */
    private String resolveResult(Hand hand, Hand dealerHand) {
        String result = getResultString(hand, dealerHand);
        return (phase == GamePhase.COMPLETE) ? result : "IN_PROGRESS";
    }

    /**
     * Checks if the hand at the given index is active for player actions.
     *
     * @param handIndex the index of the hand
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
     * @param hand       the player hand
     * @param dealerHand the dealer's hand
     * @return result string
     */
    private String getResultString(Hand hand, Hand dealerHand) {
        if (hand.isBusted()) {
            return "LOSE";
        }
        if (dealerHand.isBusted()) {
            return "WIN";
        }
        if (hand.getValue() > dealerHand.getValue()) {
            return "WIN";
        }
        if (hand.getValue() < dealerHand.getValue()) {
            return "LOSE";
        }
        return "PUSH";
    }

    /**
     * Returns the result value for a hand (1 for win, -1 for loss, 1.5 for
     * blackjack win, 0 for push).
     *
     * @param hand       the player hand
     * @param dealerHand the dealer's hand
     * @return result value
     */
    private double getResultValue(Hand hand, Hand dealerHand) {
        // Check if player hand is a blackjack (21 with exactly 2 cards)
        boolean isPlayerBlackjack = hand.getValue() == 21 && hand.getCards().size() == 2;
        boolean isDealerBlackjack = dealerHand.getValue() == 21 && dealerHand.getCards().size() == 2;

        String result = getResultString(hand, dealerHand);

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

    /**
     * Recommends the next action for a player hand based on basic and advanced
     * strategy.
     *
     * @param hand         the player hand
     * @param dealerUpCard the dealer's up card
     * @param handIndex    the index of the hand
     * @param trueCount    the current true count
     * @return recommended action string
     */
    private String recommendAction(Hand hand, Card dealerUpCard, int handIndex, double trueCount) {
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
            if (!hand.isInsured() && !hand.isInsuranceDeclined()
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
        if (!hand.isFromSplit() && hand.isBlackjack()) {
            return "NONE";
        }

        if (shoe.getCards().size() < 2) {
            Odds odds = OddsCalculator.calculateOdds(shoe, hand, getDealerUpCard());
            System.out.println("ODDS ----- Odds for current state: " + odds);
        }

        // Get basic strategy recommendation
        int dealerUpValue = dealerUpCard == null ? 10 : dealerUpCard.getRank().getValue();
        boolean allowSplit = !hand.isFromSplit() && hand.canSplit();

        String recommendation = Strategy.getRecommendedAction(hand, dealerUpValue, allowSplit);

        // Prevent recommending double on 9 if rule is off
        if ("DOUBLE".equals(recommendation) && hand.getValue() == 9 && !allowDoubleOn9) {
            recommendation = "HIT";
        }

        // Prevent recommending double after split if rule is off
        if ("DOUBLE".equals(recommendation) && hand.isFromSplit() && !allowDoubleOnSplit) {
            recommendation = "HIT";
        }

        // Apply count-based deviations for a more sophisticated strategy
        recommendation = Strategy.applyCountDeviation(recommendation, hand, dealerUpValue, trueCount);

        // Prevent recommending double on 9 after count deviation if rule is off
        if ("DOUBLE".equals(recommendation) && hand.getValue() == 9 && !allowDoubleOn9) {
            recommendation = "HIT";
        }

        // Prevent recommending double after split if rule is off (after deviation)
        if ("DOUBLE".equals(recommendation) && hand.isFromSplit() && !allowDoubleOnSplit) {
            recommendation = "HIT";
        }

        if (hand.isFromSplit()) {
            if ("INSURE".equals(recommendation) || "NO_INSURANCE".equals(recommendation)
                    || "SPLIT".equals(recommendation)) {
                String fallback = Strategy.getRecommendedAction(hand, dealerUpValue, false);
                // Only fallback to hit if fallback is split (never double), otherwise allow
                // double if logic/rules allow
                if ("SPLIT".equals(fallback)) {
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
}
