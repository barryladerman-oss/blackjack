const handsInput = document.getElementById("hands");
const statusText = document.getElementById("statusText");
const handList = document.getElementById("handList");
const dealerRow = document.getElementById("dealerRow");
const dealerCards = document.getElementById("dealerCards");
const dealerMeta = document.getElementById("dealerMeta");
const cardsDealtEl = document.getElementById("cardsDealt");
const trueCountEl = document.getElementById("trueCount");
const cardSelectionContainer = document.getElementById("cardSelectionContainer");

// Card selection constants
const RANKS = [
    { value: "ACE", label: "Ace" },
    { value: "TWO", label: "2" },
    { value: "THREE", label: "3" },
    { value: "FOUR", label: "4" },
    { value: "FIVE", label: "5" },
    { value: "SIX", label: "6" },
    { value: "SEVEN", label: "7" },
    { value: "EIGHT", label: "8" },
    { value: "NINE", label: "9" },
    { value: "TEN", label: "10" },
    { value: "JACK", label: "Jack" },
    { value: "QUEEN", label: "Queen" },
    { value: "KING", label: "King" }
];
const SUITS = [
    { value: "HEARTS", label: "Hearts" },
    { value: "DIAMONDS", label: "Diamonds" },
    { value: "CLUBS", label: "Clubs" },
    { value: "SPADES", label: "Spades" }
];
const suggestedBetEl = document.getElementById("suggestedBet");
const strategyTextEl = document.getElementById("strategyText");
const noteTextEl = document.getElementById("noteText");
const handsWonEl = document.getElementById("handsWon");
const handsLostEl = document.getElementById("handsLost");
const handsPushEl = document.getElementById("handsPush");
const netUnitsEl = document.getElementById("netUnits");
const insuranceTotalEl = document.getElementById("insuranceTotal");
const buttons = [
    document.getElementById("startBtn")
];

let isBusy = false;
let lastState = null;

const setBusy = (busy) => {
    isBusy = busy;
    updateButtonStates();
};

const updateButtonStates = () => {
    const startBtn = document.getElementById("startBtn");
    const gameInProgress = lastState && lastState.phase && lastState.phase !== "COMPLETE";
    startBtn.disabled = isBusy || gameInProgress;
    document.querySelectorAll(".hand-actions button").forEach((button) => {
        button.disabled = isBusy;
    });
};

const setStatus = (message, tone) => {
    statusText.textContent = message;
    statusText.classList.remove("ok", "error");
    if (tone) {
        statusText.classList.add(tone);
    }
};

// Generate player hand card selectors dynamically
const generatePlayerCardSelectors = (numHands) => {
    console.log('generatePlayerCardSelectors called with numHands:', numHands);
    if (!cardSelectionContainer) {
        console.error('cardSelectionContainer not found!');
        return;
    }
    // Remove existing player card selectors (keep dealer card)
    const existingPlayerRows = cardSelectionContainer.querySelectorAll('[data-player-hand]');
    existingPlayerRows.forEach(row => row.remove());

    // Generate selectors for each player hand
    for (let handNum = 1; handNum <= numHands; handNum++) {
        // Card 1
        const card1Row = document.createElement('div');
        card1Row.setAttribute('data-player-hand', handNum);
        card1Row.style.cssText = 'display: grid; grid-template-columns: 120px 1fr 1fr; gap: 4px; align-items: center;';
        card1Row.innerHTML = `
                    <label>Hand ${handNum} Card 1:</label>
                    <select id="playerHand${handNum}Card1Rank" class="card-rank-select" data-card="player${handNum}-1" style="font-size: 11px; padding: 4px;">
                        <option value="">Random</option>
                    </select>
                    <select id="playerHand${handNum}Card1Suit" class="card-suit-select" data-card="player${handNum}-1" style="font-size: 11px; padding: 4px;">
                        <option value="">Random</option>
                    </select>
                `;
        cardSelectionContainer.appendChild(card1Row);

        // Card 2
        const card2Row = document.createElement('div');
        card2Row.setAttribute('data-player-hand', handNum);
        card2Row.style.cssText = 'display: grid; grid-template-columns: 120px 1fr 1fr; gap: 4px; align-items: center;';
        card2Row.innerHTML = `
                    <label>Hand ${handNum} Card 2:</label>
                    <select id="playerHand${handNum}Card2Rank" class="card-rank-select" data-card="player${handNum}-2" style="font-size: 11px; padding: 4px;">
                        <option value="">Random</option>
                    </select>
                    <select id="playerHand${handNum}Card2Suit" class="card-suit-select" data-card="player${handNum}-2" style="font-size: 11px; padding: 4px;">
                        <option value="">Random</option>
                    </select>
                `;
        cardSelectionContainer.appendChild(card2Row);
    }

    // Populate all dropdowns with options
    populateAllDropdowns();
};

// Populate all dropdown options
const populateAllDropdowns = () => {
    console.log('populateAllDropdowns called');
    // Populate rank dropdowns
    const rankSelects = document.querySelectorAll('.card-rank-select');
    console.log('Found', rankSelects.length, 'rank selects');
    rankSelects.forEach(select => {
        // Clear existing options except "Random"
        while (select.options.length > 1) {
            select.remove(1);
        }
        // Add rank options
        RANKS.forEach(rank => {
            const option = document.createElement('option');
            option.value = rank.value;
            option.textContent = rank.label;
            select.appendChild(option);
        });
    });

    // Populate suit dropdowns
    const suitSelects = document.querySelectorAll('.card-suit-select');
    console.log('Found', suitSelects.length, 'suit selects');
    suitSelects.forEach(select => {
        // Clear existing options except "Random"
        while (select.options.length > 1) {
            select.remove(1);
        }
        // Add suit options
        SUITS.forEach(suit => {
            const option = document.createElement('option');
            option.value = suit.value;
            option.textContent = suit.label;
            select.appendChild(option);
        });
    });

    // Apply initial filtering
    updateDropdownFilters();
};

// Get all selected cards
const getSelectedCards = () => {
    const selected = [];
    const allCards = document.querySelectorAll('.card-rank-select');
    allCards.forEach(rankSelect => {
        const cardId = rankSelect.dataset.card;
        const suitSelect = document.querySelector(`.card-suit-select[data-card="${cardId}"]`);
        const rank = rankSelect.value;
        const suit = suitSelect ? suitSelect.value : '';
        if (rank && suit) {
            selected.push({ id: cardId, rank, suit });
        }
    });
    return selected;
};

// Update dropdown filters to prevent duplicate card selections
const updateDropdownFilters = () => {
    const selectedCards = getSelectedCards();
    console.log('updateDropdownFilters - selected cards:', selectedCards);

    const buildOptions = (select, options) => {
        while (select.options.length > 0) {
            select.remove(0);
        }
        const randomOption = document.createElement('option');
        randomOption.value = '';
        randomOption.textContent = 'Random';
        select.appendChild(randomOption);
        options.forEach(optionItem => {
            const option = document.createElement('option');
            option.value = optionItem.value;
            option.textContent = optionItem.label;
            select.appendChild(option);
        });
    };

    // For each dropdown pair, filter options
    document.querySelectorAll('.card-rank-select').forEach(rankSelect => {
        const cardId = rankSelect.dataset.card;
        const suitSelect = document.querySelector(`.card-suit-select[data-card="${cardId}"]`);
        if (!suitSelect) return;

        const currentRank = rankSelect.value;
        const currentSuit = suitSelect.value;

        const usedSet = new Set(
            selectedCards
                .filter(card => card.id !== cardId)
                .map(card => `${card.rank}:${card.suit}`)
        );

        if (currentRank && currentSuit && usedSet.has(`${currentRank}:${currentSuit}`)) {
            rankSelect.value = '';
            suitSelect.value = '';
        }

        const rankOptions = RANKS.filter(rank => {
            return SUITS.some(suit => !usedSet.has(`${rank.value}:${suit.value}`));
        });

        buildOptions(rankSelect, rankOptions);

        if (currentRank && rankOptions.some(rank => rank.value === currentRank)) {
            rankSelect.value = currentRank;
        }

        const effectiveRank = rankSelect.value;
        const suitOptions = effectiveRank
            ? SUITS.filter(suit => !usedSet.has(`${effectiveRank}:${suit.value}`))
            : SUITS.slice();

        buildOptions(suitSelect, suitOptions);

        if (currentSuit && suitOptions.some(suit => suit.value === currentSuit)) {
            suitSelect.value = currentSuit;
        }

    });
};

const toJson = async (response) => {
    const text = await response.text();
    try {
        return JSON.parse(text);
    } catch (error) {
        return { raw: text };
    }
};

const apiFetch = async (path, options) => {
    const response = await fetch(`${window.location.origin}${path}`, options);
    const data = await toJson(response);
    if (!response.ok) {
        const message = data.message || data.error || response.statusText;
        throw new Error(message);
    }
    return data;
};

const normalizeRank = (rank) => {
    if (!rank) {
        return "?";
    }
    const value = String(rank).toUpperCase();
    if (value === "ACE") return "A";
    if (value === "KING") return "K";
    if (value === "QUEEN") return "Q";
    if (value === "JACK") return "J";
    if (value === "TEN") return "10";
    return value;
};

const normalizeSuit = (suit) => {
    if (!suit) {
        return "?";
    }
    const value = String(suit).toUpperCase();
    if (value.startsWith("HEART")) return "H";
    if (value.startsWith("DIAMOND")) return "D";
    if (value.startsWith("CLUB")) return "C";
    if (value.startsWith("SPADE")) return "S";
    return value.charAt(0);
};

const cardFace = (card) => {
    if (!card) {
        return { rank: "?", suit: "?" };
    }
    const rankRaw = card.rank?.label || card.rank || "?";
    const suitRaw = card.suit?.label || card.suit || "?";
    return {
        rank: normalizeRank(rankRaw),
        suit: normalizeSuit(suitRaw)
    };
};

const suitGlyph = (suit) => {
    if (suit === "H") return "H";
    if (suit === "D") return "D";
    if (suit === "C") return "C";
    if (suit === "S") return "S";
    return "?";
};

const renderCards = (cards, isDealer = false, phase = null) => {
    const list = Array.isArray(cards) ? cards : [];
    if (list.length === 0) {
        return "<div class=\"mono\">(no cards)</div>";
    }

    const cardsToRender = isDealer ? [...list].reverse() : list;

    return cardsToRender
        .map((card, displayIndex) => {
            const originalIndex = isDealer ? list.length - 1 - displayIndex : displayIndex;
            const isHoleCard = isDealer && originalIndex === 1 && phase === "PLAYER_TURN";

            if (isHoleCard) {
                return `<div class="playing-card card-back" title="Hole card (hidden)"></div>`;
            }

            const face = cardFace(card);
            return `
                        <div class="playing-card" data-suit="${face.suit}" ${isDealer && originalIndex === 1 && phase !== "PLAYER_TURN" ? 'class="flipping"' : ''}>
                            <div class="card-corner">${face.rank}${face.suit}</div>
                            <div class="card-center">${face.rank}</div>
                            <div class="card-suit">${suitGlyph(face.suit)}</div>
                        </div>
                    `;
        })
        .join("");
};

const countVisibleCards = (state) => {
    const dealerCards = state.dealer?.cards ? state.dealer.cards.length : 0;
    const playerCards = Array.isArray(state.hands)
        ? state.hands.reduce((sum, hand) => sum + (hand.cards ? hand.cards.length : 0), 0)
        : 0;
    return dealerCards + playerCards;
};

const strategyFromCount = (state) => {
    const trueCount = typeof state.trueCount === "number" ? state.trueCount : null;
    const visibleCards = countVisibleCards(state);
    if (visibleCards === 0 || trueCount === null) {
        return { cardsDealt: "--", trueCount: "--", text: "No count data available." };
    }
    let text = "Keep bets flat until more cards are seen.";
    if (visibleCards >= 8) {
        if (trueCount >= 2) {
            text = "Increase bet. Favor doubles on 10/11 and tougher stands in high counts.";
        } else if (trueCount <= -1) {
            text = "Lower bet. Avoid marginal doubles and play tighter.";
        } else {
            text = "Maintain base bet and follow basic strategy.";
        }
    }
    return {
        cardsDealt: `${visibleCards} visible`,
        trueCount: trueCount.toFixed(2),
        text
    };
};

const renderState = (state) => {
    lastState = state;
    const strategy = strategyFromCount(state);
    cardsDealtEl.textContent = strategy.cardsDealt;
    trueCountEl.textContent = strategy.trueCount;
    strategyTextEl.textContent = strategy.text;
    suggestedBetEl.textContent = state.suggestedBetUnits ?? "--";
    noteTextEl.textContent = state.note || "--";

    const results = Array.isArray(state.hands) ? state.hands : [];
    const wins = results.filter((hand) => hand.result === "WIN").length;
    const losses = results.filter((hand) => hand.result === "LOSE").length;
    const pushes = results.filter((hand) => hand.result === "PUSH").length;
    handsWonEl.textContent = wins;
    handsLostEl.textContent = losses;
    handsPushEl.textContent = pushes;
    netUnitsEl.textContent = state.phase === "COMPLETE" ? (state.netUnits ?? 0) : "--";
    insuranceTotalEl.textContent = state.phase === "COMPLETE" ? (state.insuranceTotal ?? 0) : "--";

    if (state.dealer) {
        dealerCards.innerHTML = renderCards(state.dealer.cards, true, state.phase);
        const meta = [
            `Total: ${state.dealer.total ?? "--"}`,
            state.dealer.soft ? "Soft" : null,
            state.dealer.busted ? "Busted" : null,
            state.dealer.standing ? "Standing" : null
        ].filter(Boolean);
        dealerMeta.textContent = meta.join(" | ");
    } else {
        dealerCards.innerHTML = "";
        dealerMeta.textContent = "Total: --";
    }

    if (!state.hands || state.hands.length === 0) {
        handList.innerHTML = "<div class=\"row\">No hands returned.</div>";
        return;
    }

    const activeIndex = state.phase === "PLAYER_TURN"
        ? state.hands.findIndex((hand) => !hand.busted && !hand.standing)
        : -1;

    // Check dealer's up card for insurance phase
    const dealerStateCards = state.dealer?.cards || [];
    let dealerShowsAce = false;
    if (dealerStateCards.length > 0) {
        const dealerUpCard = dealerStateCards[0];
        const rankValue = dealerUpCard?.rank?.value || dealerUpCard?.rank?.label || dealerUpCard?.rank || "";
        dealerShowsAce = String(rankValue).toUpperCase() === "ACE";
    }

    // Check if all hands have resolved insurance (made an explicit decision)
    const allHandsResolvedInsurance = state.hands.every(hand => {
        // A hand has resolved insurance if:
        // 1. It has taken insurance, OR
        // 2. It has explicitly declined insurance, OR
        // 2. It's been auto-stood (from natural blackjack), OR
        // 3. It's already moved past 2 cards (implicitly refused insurance), OR
        // 4. It's already busted or standing
        return hand.insured ||
            hand.insuranceDeclined ||
            hand.standing ||
            hand.busted ||
            (hand.cards && hand.cards.length > 2);
    });

    const normalizeAction = (value) => String(value || "")
        .toLowerCase()
        .replace(/[\s-]+/g, "_");

    const formatRecommendation = (value) => {
        if (!value) {
            return "";
        }
        return String(value).replace(/_/g, " ");
    };

    handList.innerHTML = state.hands
        .map((hand) => {
            const isActive = hand.index === activeIndex;
            const canDouble = isActive && hand.cards && hand.cards.length === 2;
            const canSplit = isActive && hand.cards && hand.cards.length === 2 &&
                hand.cards[0]?.rank === hand.cards[1]?.rank;

            const canInsure = hand.cards && hand.cards.length === 2 && dealerShowsAce && !hand.insured && !hand.insuranceDeclined;

            // When dealer shows Ace, only insurance actions are allowed until ALL hands make a decision
            const dealerInInsurancePhase = dealerShowsAce && state.phase === "PLAYER_TURN" && !allHandsResolvedInsurance;

            // Enable regular actions only if:
            // 1. Dealer doesn't show an Ace, OR
            // 2. Phase is COMPLETE (round over), OR
            // 3. All hands have resolved their insurance decisions
            const regularActionsEnabled = isActive && (!dealerShowsAce || state.phase === "COMPLETE" || allHandsResolvedInsurance);

            console.log('Insurance check:', { isActive, hasCards: !!hand.cards, cardCount: hand.cards?.length, dealerShowsAce, alreadyInsured: hand.insured, canInsure, dealerInInsurancePhase, regularActionsEnabled, allHandsResolvedInsurance });
            const recommendedAction = normalizeAction(hand.recommendedAction);
            const recommendInsure = recommendedAction === "insure";
            const recommendDecline = recommendedAction === "no_insurance" || recommendedAction === "decline_insurance";
            const showHit = regularActionsEnabled;
            const showStand = regularActionsEnabled;
            const showDouble = regularActionsEnabled && canDouble;
            const showSplit = regularActionsEnabled && canSplit;
            const result = hand.result ? `<span class="tag">${hand.result}</span>` : "";
            const hint = hand.recommendedAction ? `<span class="tag">${formatRecommendation(hand.recommendedAction)}</span>` : "";
            const insuranceDecision = hand.insuranceDeclined ? `<span class="tag">NO INSURANCE</span>` : "";
            const handStatus = [
                `${hand.total}`,
                hand.soft ? "(soft)" : null,
                hand.busted ? "(bust)" : null,
                hand.standing ? "(stand)" : null
            ].filter(Boolean).join(" ");

            let resultClass = "";
            if (state.phase === "COMPLETE") {
                if (hand.result === "WIN") resultClass = " result-win";
                else if (hand.result === "LOSE") resultClass = " result-lose";
                else if (hand.result === "PUSH") resultClass = " result-push";
            }

            return `
                        <div class="hand-row${isActive ? " active" : ""}${resultClass}">
                            <div class="hand-header">
                                <div class="row-title">Hand ${hand.index}</div>
                                <div class="meta-line">
                                    <span>${handStatus}</span>
                                    ${result}
                                    ${hint}
                                    ${insuranceDecision}
                                </div>
                            </div>
                            <div class="cards">${renderCards(hand.cards)}</div>
                            <div class="hand-actions">
                                ${canInsure ? `<button class="${recommendInsure ? "recommended" : ""}" data-action="insurance" data-hand="${hand.index}">Insurance</button>` : ""}
                                ${canInsure ? `<button class="ghost ${recommendDecline ? "recommended" : ""}" data-action="decline-insurance" data-hand="${hand.index}">No Insurance</button>` : ""}
                                ${showHit ? `<button class="${recommendedAction === "hit" ? "recommended" : ""}" data-action="hit" data-hand="${hand.index}">Hit</button>` : ""}
                                ${showStand ? `<button class="secondary ${recommendedAction === "stand" ? "recommended" : ""}" data-action="stand" data-hand="${hand.index}">Stand</button>` : ""}
                                ${showDouble ? `<button class="${recommendedAction === "double" ? "recommended" : ""}" data-action="double" data-hand="${hand.index}">Double</button>` : ""}
                                ${showSplit ? `<button class="ghost ${recommendedAction === "split" ? "recommended" : ""}" data-action="split" data-hand="${hand.index}">Split</button>` : ""}
                            </div>
                        </div>
                    `;
        })
        .join("");

    setBusy(isBusy);
};

const guardGameId = () => {
    const id = lastState?.id || "";
    if (!id) {
        throw new Error("Start a game first.");
    }
    return id;
};

const run = async (task) => {
    try {
        setBusy(true);
        setStatus("Working...", null);
        const data = await task();
        renderState(data);
        setStatus("Updated with latest game state.", "ok");
    } catch (error) {
        setStatus(error.message || "Request failed.", "error");
    } finally {
        setBusy(false);
    }
};

// Initialize everything
const initializeCardSelectors = () => {
    console.log('initializeCardSelectors called');
    console.log('handsInput:', handsInput);
    console.log('cardSelectionContainer:', cardSelectionContainer);
    // Get initial hands value from input
    const initialHands = Number(handsInput?.value || 3);
    console.log('initialHands:', initialHands);
    // Generate player card selectors
    generatePlayerCardSelectors(initialHands);
};

// Listen to card selection changes for filtering
document.addEventListener('change', (e) => {
    if (e.target.classList.contains('card-rank-select') ||
        e.target.classList.contains('card-suit-select')) {
        updateDropdownFilters();
    }
});

// Listen to hands input changes to regenerate player card selectors
if (handsInput) {
    handsInput.addEventListener('input', () => {
        const numHands = Number(handsInput.value || 1);
        generatePlayerCardSelectors(numHands);
    });
}

// Initialize card selectors when page loads
// Since script is at the end of body, DOM should be ready
initializeCardSelectors();

document.getElementById("startBtn").addEventListener("click", () => {
    const hands = Number(handsInput.value || 1);

    // Collect optional card selections
    const params = new URLSearchParams();
    params.append('hands', hands);

    // Collect dealer card
    const d1r = document.getElementById("dealerCard1Rank").value;
    const d1s = document.getElementById("dealerCard1Suit").value;

    // Validate dealer card
    if ((d1r && !d1s) || (!d1r && d1s)) {
        alert("Dealer Up Card: Please select both rank and suit, or leave both as Random");
        return;
    }

    // Collect all player hand cards
    const playerCards = [];
    for (let handNum = 1; handNum <= hands; handNum++) {
        const c1r = document.getElementById(`playerHand${handNum}Card1Rank`);
        const c1s = document.getElementById(`playerHand${handNum}Card1Suit`);
        const c2r = document.getElementById(`playerHand${handNum}Card2Rank`);
        const c2s = document.getElementById(`playerHand${handNum}Card2Suit`);

        if (!c1r || !c1s || !c2r || !c2s) continue;

        const card1Rank = c1r.value;
        const card1Suit = c1s.value;
        const card2Rank = c2r.value;
        const card2Suit = c2s.value;

        // Validate rank and suit pairs
        if ((card1Rank && !card1Suit) || (!card1Rank && card1Suit)) {
            alert(`Hand ${handNum} Card 1: Please select both rank and suit, or leave both as Random`);
            return;
        }
        if ((card2Rank && !card2Suit) || (!card2Rank && card2Suit)) {
            alert(`Hand ${handNum} Card 2: Please select both rank and suit, or leave both as Random`);
            return;
        }

        playerCards.push({
            hand: handNum,
            card1: { rank: card1Rank, suit: card1Suit },
            card2: { rank: card2Rank, suit: card2Suit }
        });
    }

    // Check for duplicate cards
    const allSelectedCards = [];
    if (d1r && d1s) allSelectedCards.push({ rank: d1r, suit: d1s, name: "Dealer Up Card" });

    playerCards.forEach(hand => {
        if (hand.card1.rank && hand.card1.suit) {
            allSelectedCards.push({
                rank: hand.card1.rank,
                suit: hand.card1.suit,
                name: `Hand ${hand.hand} Card 1`
            });
        }
        if (hand.card2.rank && hand.card2.suit) {
            allSelectedCards.push({
                rank: hand.card2.rank,
                suit: hand.card2.suit,
                name: `Hand ${hand.hand} Card 2`
            });
        }
    });

    for (let i = 0; i < allSelectedCards.length; i++) {
        for (let j = i + 1; j < allSelectedCards.length; j++) {
            if (allSelectedCards[i].rank === allSelectedCards[j].rank &&
                allSelectedCards[i].suit === allSelectedCards[j].suit) {
                alert(`Duplicate card detected: ${allSelectedCards[i].name} and ${allSelectedCards[j].name} are both ${allSelectedCards[i].rank} of ${allSelectedCards[i].suit}`);
                return;
            }
        }
    }

    // Add dealer card to params
    if (d1r) params.append('dealerCard1Rank', d1r);
    if (d1s) params.append('dealerCard1Suit', d1s);

    // Add all player hand cards to params
    playerCards.forEach(hand => {
        if (hand.card1.rank) params.append(`playerHand${hand.hand}Card1Rank`, hand.card1.rank);
        if (hand.card1.suit) params.append(`playerHand${hand.hand}Card1Suit`, hand.card1.suit);
        if (hand.card2.rank) params.append(`playerHand${hand.hand}Card2Rank`, hand.card2.rank);
        if (hand.card2.suit) params.append(`playerHand${hand.hand}Card2Suit`, hand.card2.suit);
    });

    run(() => apiFetch(`/api/blackjack/start?${params.toString()}`, { method: "POST" }));
});

handList.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
        return;
    }
    if (button.disabled) {
        return;
    }
    const action = button.dataset.action;
    const hand = Number(button.dataset.hand);
    run(() => {
        const id = guardGameId();
        return apiFetch(`/api/blackjack/${action}/${id}?hand=${hand}`, { method: "POST" });
    });
});
