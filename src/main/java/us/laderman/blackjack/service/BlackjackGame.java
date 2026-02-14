package us.laderman.blackjack.service;

import us.laderman.blackjack.model.Card;
import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlackjackGame {
    private final UUID id;
    private final Shoe shoe;
    private final List<Hand> playerHands = new ArrayList<>();
    private final Hand dealerHand = new Hand();
    private final int decksTotal;
    private int runningCount;
    private int cardsDealt;
    private GamePhase phase = GamePhase.PLAYER_TURN;

    public BlackjackGame(int hands, int decks) {
        this(hands, decks, new java.util.HashMap<>(), null, null);
    }

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

    public UUID getId() {
        return id;
    }

    public GameState hit(int handIndex) {
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
        dealCard(hand);
        if (hand.isBusted() || hand.getValue().getTotal() == 21) {
            hand.setStanding(true);
        }
        finishIfReady();
        return toGameState();
    }

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

    public GameState doubleDown(int handIndex) {
        ensurePlayerTurn();
        Hand hand = getPlayerHand(handIndex);
        if (hand.isStanding() || hand.isBusted()) {
            return toGameState();
        }
        // Check if insurance decisions are pending
        if (dealerHasAce() && !allHandsResolvedInsurance()) {
            throw new IllegalStateException("All hands must make an insurance decision first");
        }
        if (hand.isFromSplit()) {
            throw new IllegalArgumentException("Cannot double on a split hand");
        }
        if (hand.getCards().size() != 2) {
            throw new IllegalArgumentException("Double requires exactly two cards");
        }
        int handTotal = hand.getValue().getTotal();
        if (handTotal != 9 && handTotal != 10 && handTotal != 11) {
            throw new IllegalArgumentException("Double only allowed on hands totaling 9, 10, or 11");
        }
        hand.setBetUnits(2);
        dealCard(hand);
        hand.setStanding(true);
        finishIfReady();
        return toGameState();
    }

    public GameState split(int handIndex) {
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
        if (playerHands.size() >= 5) {
            throw new IllegalArgumentException("Max hands reached");
        }

        // Resplit limits: prevent resplitting aces
        boolean isAceHand = hand.getCards().get(0).getRank() == Rank.ACE;
        if (hand.isFromSplit() && isAceHand) {
            throw new IllegalArgumentException("Cannot resplit aces. Each ace receives only one card");
        }

        Card moved = hand.splitOff();
        hand.setIsFromSplit(true);
        Hand newHand = new Hand();
        newHand.setIsFromSplit(true);

        // If splitting aces, restrict to one card per ace
        if (isAceHand) {
            hand.setCanHitAfterSplitAces(false);
            newHand.setCanHitAfterSplitAces(false);
        }

        newHand.addCard(moved);
        playerHands.add(handIndex + 1, newHand);
        dealCard(hand);
        dealCard(newHand);
        finishIfReady();
        return toGameState();
    }

    public GameState insurance(int handIndex) {
        ensurePlayerTurn();
        Hand hand = getPlayerHand(handIndex);
        if (hand.isStanding() || hand.isBusted() || hand.isInsured()) {
            return toGameState();
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

    public GameState declineInsurance(int handIndex) {
        ensurePlayerTurn();
        Hand hand = getPlayerHand(handIndex);
        if (hand.isStanding() || hand.isBusted() || hand.isInsured() || hand.isInsuranceDeclined()) {
            return toGameState();
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

    public GameState getState() {
        return toGameState();
    }

    private boolean allHandsResolvedInsurance() {
        // Check if all hands that need to make an insurance decision have made one
        // A hand has resolved insurance if either:
        // 1. The hand is not playable (busted, standing, blackjack)
        // 2. The hand has been offered insurance and either took it or ignored it
        HandValue dealerValue = dealerHand.getValue();
        if (dealerValue.getTotal() != 21 || dealerHand.getCards().size() != 2) {
            return true; // Dealer doesn't have blackjack, insurance not applicable
        }

        for (Hand hand : playerHands) {
            if (!hand.isBusted() && !hand.isStanding()) {
                // This hand is still playing and hasn't resolved insurance yet
                if (hand.getCards().size() == 2 && !hand.isInsured() && !hand.isInsuranceDeclined()) {
                    // Hand has 2 cards but hasn't made insurance decision
                    return false;
                }
            }
        }
        return true;
    }

    private void completeRound() {
        // End the round immediately when dealer has blackjack
        dealerHand.setStanding(true);
        phase = GamePhase.COMPLETE;
    }

    private boolean dealerHasAce() {
        Card dealerUpCard = getDealerUpCard();
        return dealerUpCard != null && dealerUpCard.getRank() == Rank.ACE;
    }

    private void ensurePlayerTurn() {
        if (phase != GamePhase.PLAYER_TURN) {
            throw new IllegalStateException("Player actions are closed");
        }
    }

    private Hand getPlayerHand(int index) {
        if (index < 0 || index >= playerHands.size()) {
            throw new IllegalArgumentException("Invalid hand index");
        }
        return playerHands.get(index);
    }

    private Card getDealerUpCard() {
        List<Card> cards = dealerHand.getCards();
        return cards.isEmpty() ? null : cards.get(0);
    }

    private void dealInitial(java.util.Map<Integer, BlackjackService.HandCards> playerHandCards, Card dealerCard1,
            Card dealerCard2) {
        // Deal first card to each player hand
        for (int i = 0; i < playerHands.size(); i++) {
            Hand hand = playerHands.get(i);
            BlackjackService.HandCards cards = playerHandCards.get(i);
            if (cards != null && cards.card1 != null) {
                // Use specific card for this hand
                dealSpecificCard(hand, cards.card1);
            } else {
                dealCard(hand);
            }
        }

        // Deal first card to dealer
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
                // Use specific card for this hand
                dealSpecificCard(hand, cards.card2);
            } else {
                dealCard(hand);
            }
        }

        // Deal second card to dealer
        if (dealerCard2 != null) {
            dealSpecificCard(dealerHand, dealerCard2);
        } else {
            dealCard(dealerHand);
        }
    }

    private void dealSpecificCard(Hand hand, Card card) {
        cardsDealt++;
        updateCount(card);
        hand.addCard(card);
    }

    private void autoStandBlackjacks() {
        for (Hand hand : playerHands) {
            if (hand.getCards().size() == 2 && hand.getValue().getTotal() == 21) {
                hand.setStanding(true);
            }
        }
        finishIfReady();
    }

    private void dealCard(Hand hand) {
        Card card = shoe.draw();
        cardsDealt++;
        updateCount(card);
        hand.addCard(card);
    }

    private void updateCount(Card card) {
        Rank rank = card.getRank();
        switch (rank) {
            case TWO, THREE, FOUR, FIVE, SIX -> runningCount += 1;
            case SEVEN, EIGHT, NINE -> runningCount += 0;
            case TEN, JACK, QUEEN, KING, ACE -> runningCount -= 1;
        }
    }

    private void finishIfReady() {
        for (Hand hand : playerHands) {
            if (!hand.isStanding() && !hand.isBusted()) {
                return;
            }
        }
        playDealer();
        phase = GamePhase.COMPLETE;
    }

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

    private GameState toGameState() {
        HandValue dealerValue = dealerHand.getValue();
        double decksRemaining = Math.max(0.25, (decksTotal * 52.0 - cardsDealt) / 52.0);
        double trueCount = runningCount / decksRemaining;
        int suggestedBet = suggestBetUnits(trueCount);

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
            String recommended = recommendAction(hand, value, dealerUpCard, trueCount);
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
                    hand.isFromSplit()));
        }

        String note = phase == GamePhase.COMPLETE
                ? "Round complete" + (insuranceTotal != 0 ? " | Insurance: " + roundToTwo(insuranceTotal) : "")
                : getDealerUpCard() != null && getDealerUpCard().getRank() == Rank.ACE
                        ? "Insurance available on dealer ace"
                        : "Dealer up card used for recommendations";

        return new GameState(
                id.toString(),
                phase.name(),
                dealerState,
                handStates,
                runningCount,
                roundToTwo(trueCount),
                roundToTwo(decksRemaining),
                roundToTwo(netUnits),
                roundToTwo(insuranceTotal),
                note);
    }

    private String resolveResult(Hand hand, int playerTotal, boolean playerBusted, HandValue dealerValue) {
        String result = getResultString(playerTotal, playerBusted, dealerValue);
        return (phase == GamePhase.COMPLETE) ? result : "IN_PROGRESS";
    }

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

    private String recommendAction(Hand hand, HandValue value, Card dealerUpCard, double trueCount) {
        // Check for insurance opportunity first
        if (phase == GamePhase.PLAYER_TURN && dealerUpCard != null && dealerUpCard.getRank() == Rank.ACE) {
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
        if (hand.getCards().size() == 2 && value.getTotal() == 21) {
            return "NONE";
        }

        int dealerUpValue = dealerUpCard == null ? 10 : dealerUpCard.getRank().getValue();

        // Get basic strategy recommendation
        String recommendation = Strategy.getRecommendedAction(hand, dealerUpValue, hand.canSplit());

        // Apply count-based deviations for a more sophisticated strategy
        recommendation = Strategy.applyCountDeviation(recommendation, hand, dealerUpValue, trueCount);

        return recommendation;
    }

    private int suggestBetUnits(double trueCount) {
        if (trueCount <= 1.0) {
            return 1;
        }
        int units = 1 + (int) Math.floor(trueCount - 1.0);
        return Math.min(10, Math.max(1, units));
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
