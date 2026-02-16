const handsInput = document.getElementById("hands");
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
const doublesWonEl = document.getElementById("doublesWon");
const doublesLostEl = document.getElementById("doublesLost");
const insuranceTotalEl = document.getElementById("insuranceTotal");
const buttons = [
    document.getElementById("startBtn")
];

let isBusy = false;
let lastState = null;
let recommendationRefreshPending = false;
let replaceGameStateNext = false;
let fallbackGameId = null;
let testModeGameId = null;

const setBusy = (busy) => {
    isBusy = busy;
    updateButtonStates();
};

const updateButtonStates = () => {
    const startBtn = document.getElementById("startBtn");
    if (startBtn) {
        const gameInProgress = lastState && lastState.phase && lastState.phase !== "COMPLETE";
        startBtn.disabled = isBusy || gameInProgress;
    }

    // Update standalone action buttons based on game phase
    const hitBtn = document.getElementById("hitBtn");
    const standBtn = document.getElementById("standBtn");
    const doubleBtn = document.getElementById("doubleBtn");
    const splitBtn = document.getElementById("splitBtn");

    const canAct = lastState && lastState.phase === "PLAYER_TURN" && !isBusy;

    if (hitBtn) hitBtn.disabled = !canAct;
    if (standBtn) standBtn.disabled = !canAct;
    if (doubleBtn) doubleBtn.disabled = !canAct;
    if (splitBtn) splitBtn.disabled = !canAct;

    document.querySelectorAll(".hand-actions button").forEach((button) => {
        button.disabled = isBusy;
    });
};

const setStatus = (message, tone) => {
    // Status display removed - keeping function for compatibility
};

// Generate player hand card selectors dynamically
const generatePlayerCardSelectors = (numHands) => {
    console.log('generatePlayerCardSelectors called with numHands:', numHands);
    if (!cardSelectionContainer) {
        console.error('cardSelectionContainer not found!');
        return;
    }
    const dealerSelectionRow = document.getElementById('dealerSelectionRow');
    // Remove existing player card selectors (keep dealer card)
    const existingPlayerRows = cardSelectionContainer.querySelectorAll('[data-player-hand]');
    existingPlayerRows.forEach(row => row.remove());

    // Generate selectors for each player hand
    for (let handNum = 1; handNum <= numHands; handNum++) {
        // Combined row for both cards
        const handRow = document.createElement('div');
        handRow.setAttribute('data-player-hand', handNum);
        handRow.style.cssText = 'display: grid; grid-template-columns: 120px max-content max-content max-content max-content; gap: 2px; align-items: center; width: max-content; justify-items: start;';
        handRow.innerHTML = `
                    <label>Hand ${handNum}:</label>
                    <select id="playerHand${handNum}Card1Rank" class="card-rank-select" data-card="player${handNum}-1" style="font-size: 11px; padding: 2px; max-width: 100%;">
                        <option value="">Random</option>
                    </select>
                    <select id="playerHand${handNum}Card1Suit" class="card-suit-select" data-card="player${handNum}-1" style="font-size: 11px; padding: 2px; max-width: 100%;">
                        <option value="">Random</option>
                    </select>
                    <select id="playerHand${handNum}Card2Rank" class="card-rank-select" data-card="player${handNum}-2" style="font-size: 11px; padding: 2px; max-width: 100%;">
                        <option value="">Random</option>
                    </select>
                    <select id="playerHand${handNum}Card2Suit" class="card-suit-select" data-card="player${handNum}-2" style="font-size: 11px; padding: 2px; max-width: 100%;">
                        <option value="">Random</option>
                    </select>
                `;
        if (dealerSelectionRow) {
            cardSelectionContainer.insertBefore(handRow, dealerSelectionRow);
        } else {
            cardSelectionContainer.appendChild(handRow);
        }
    }

    // Populate all dropdowns with options
    populateAllDropdowns();
};

// Populate all dropdown options
const populateAllDropdowns = () => {
    console.log('populateAllDropdowns called');
    console.log('RANKS:', RANKS);
    console.log('SUITS:', SUITS);

    // Populate rank dropdowns
    const rankSelects = document.querySelectorAll('.card-rank-select');
    console.log('Found', rankSelects.length, 'rank selects');
    rankSelects.forEach((select, index) => {
        console.log(`Populating rank select ${index}, id: ${select.id}`);
        buildOptions(select, RANKS, true);
        console.log(`After buildOptions, select has ${select.options.length} options`);
    });

    // Populate suit dropdowns
    const suitSelects = document.querySelectorAll('.card-suit-select');
    console.log('Found', suitSelects.length, 'suit selects');
    suitSelects.forEach((select, index) => {
        console.log(`Populating suit select ${index}, id: ${select.id}`);
        buildOptions(select, SUITS, true);
        console.log(`After buildOptions, select has ${select.options.length} options`);
    });
};

const resetStartCardSelections = () => {
    if (!cardSelectionContainer) {
        return;
    }
    const startRankSelects = cardSelectionContainer.querySelectorAll('.card-rank-select');
    const startSuitSelects = cardSelectionContainer.querySelectorAll('.card-suit-select');

    startRankSelects.forEach((select) => {
        buildOptions(select, RANKS, true);
        select.value = '';
    });

    startSuitSelects.forEach((select) => {
        buildOptions(select, SUITS, true);
        select.value = '';
    });

    updateDropdownFilters();
    const resetCount = Number(cardSelectionContainer.dataset.resetCount || 0) + 1;
    cardSelectionContainer.dataset.resetCount = String(resetCount);

    const firstPlayerRank = document.getElementById("playerHand1Card1Rank");
    if (firstPlayerRank) {
        firstPlayerRank.focus();
    }
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

// Helper function to build dropdown options
const buildOptions = (select, options, includeRandom, selectFirst = false) => {
    if (!select) return;
    while (select.options.length > 0) {
        select.remove(0);
    }
    if (includeRandom) {
        const randomOption = document.createElement('option');
        randomOption.value = '';
        randomOption.textContent = 'Random';
        select.appendChild(randomOption);
    }
    options.forEach((optionItem, index) => {
        const option = document.createElement('option');
        option.value = optionItem.value;
        option.textContent = optionItem.label;
        if (selectFirst && index === 0) {
            option.selected = true;
        }
        select.appendChild(option);
    });
    sizeSelectToContent(select);
};

const sizeSelectToContent = (select) => {
    if (!select) return;
    const computed = window.getComputedStyle(select);
    const font = computed.font || `${computed.fontSize} ${computed.fontFamily}`;
    const canvas = sizeSelectToContent._canvas || (sizeSelectToContent._canvas = document.createElement("canvas"));
    const context = canvas.getContext("2d");
    context.font = font;

    let maxTextWidth = 0;
    Array.from(select.options).forEach((option) => {
        const width = context.measureText(option.textContent || "").width;
        if (width > maxTextWidth) {
            maxTextWidth = width;
        }
    });

    const paddingLeft = parseFloat(computed.paddingLeft) || 0;
    const paddingRight = parseFloat(computed.paddingRight) || 0;
    const chromeAllowance = 12;
    const total = Math.ceil(maxTextWidth + paddingLeft + paddingRight + chromeAllowance);
    select.style.width = `${total}px`;
    select.style.minWidth = "0";
};

// Update dropdown filters to prevent duplicate card selections
const updateDropdownFilters = () => {
    const selectedCards = getSelectedCards();
    console.log('updateDropdownFilters - selected cards:', selectedCards);

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

        buildOptions(rankSelect, rankOptions, true);

        if (currentRank && rankOptions.some(rank => rank.value === currentRank)) {
            rankSelect.value = currentRank;
        }

        const effectiveRank = rankSelect.value;
        const suitOptions = effectiveRank
            ? SUITS.filter(suit => !usedSet.has(`${effectiveRank}:${suit.value}`))
            : SUITS.slice();

        const includeRandomSuit = !effectiveRank;
        buildOptions(suitSelect, suitOptions, includeRandomSuit);

        if (effectiveRank && suitOptions.length > 0) {
            if (currentSuit && suitOptions.some(suit => suit.value === currentSuit)) {
                suitSelect.value = currentSuit;
            } else {
                suitSelect.value = suitOptions[0].value;
            }
        } else if (!effectiveRank && currentSuit && suitOptions.some(suit => suit.value === currentSuit)) {
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
    const fullUrl = `${window.location.origin}${path}`;
    console.log("apiFetch - URL:", fullUrl, "options:", options);
    const response = await fetch(fullUrl, options);
    console.log("apiFetch - response status:", response.status, response.statusText);
    const data = await toJson(response);
    console.log("apiFetch - data:", data);
    if (!response.ok) {
        const message = data.message || data.error || response.statusText;
        console.error("apiFetch - error:", message);
        throw new Error(message);
    }
    return data;
};

const resolveGameId = (state) => {
    if (document.body.classList.contains("test-mode")) {
        if (!testModeGameId) {
            testModeGameId = typeof crypto !== "undefined" && crypto.randomUUID
                ? crypto.randomUUID()
                : `test-${Date.now()}`;
        }
        return testModeGameId;
    }
    const candidate = state?.id || state?.gameId;
    if (candidate) {
        fallbackGameId = null;
        return candidate;
    }
    if (!fallbackGameId) {
        fallbackGameId = typeof crypto !== "undefined" && crypto.randomUUID
            ? crypto.randomUUID()
            : `game-${Date.now()}`;
    }
    return fallbackGameId;
};

const updateDebugPanel = (state) => {
    const gameStateEl = document.getElementById("gameState");
    const gameIdEl = document.getElementById("gameId");
    const phaseEl = document.getElementById("phase");
    const trueCountDebugEl = document.getElementById("trueCountDebug");
    const runningCountDebugEl = document.getElementById("runningCountDebug");
    const decksRemainingEl = document.getElementById("decksRemaining");

    if (gameStateEl) {
        const nextStateText = JSON.stringify(state, null, 2);
        if (replaceGameStateNext) {
            gameStateEl.textContent = nextStateText;
            setTimeout(() => {
                const currentEl = document.getElementById("gameState");
                if (!currentEl) {
                    return;
                }
                const nextStateEl = currentEl.cloneNode(false);
                nextStateEl.id = "gameState";
                nextStateEl.textContent = nextStateText;
                currentEl.replaceWith(nextStateEl);
            }, 50);
        } else {
            gameStateEl.textContent = nextStateText;
        }
    }

    replaceGameStateNext = false;

    if (gameIdEl) gameIdEl.textContent = resolveGameId(state);
    if (phaseEl) phaseEl.textContent = state.phase || "-";

    const trueCountValue = typeof state.trueCount === "number" ? state.trueCount.toFixed(2) : "-";
    const runningCountValue = typeof state.runningCount === "number" ? String(state.runningCount) : "-";
    const decksRemainingValue = typeof state.decksRemaining === "number" ? state.decksRemaining.toFixed(2) : "-";

    if (trueCountDebugEl) trueCountDebugEl.textContent = trueCountValue;
    if (runningCountDebugEl) runningCountDebugEl.textContent = runningCountValue;
    if (decksRemainingEl) decksRemainingEl.textContent = decksRemainingValue;
};

const updateGameInfoDisplay = (state) => {
    // Update game info display elements with proper IDs for UI tests
    const gameIdEl = document.getElementById("gameId");
    const phaseEl = document.getElementById("phase");
    const dealerUpcardEl = document.getElementById("dealerUpcard");
    const runningCountEl = document.getElementById("runningCount");
    const trueCountEl = document.getElementById("trueCount");
    const decksRemainingEl = document.getElementById("decksRemaining");
    const isTestMode = document.body.classList.contains("test-mode");
    const showCounts = state && state.phase && state.phase !== "PLAYER_TURN";

    // Update game ID
    if (gameIdEl) {
        gameIdEl.textContent = resolveGameId(state);
    }

    // Update phase
    if (phaseEl) {
        phaseEl.textContent = state.phase || '-';
    }

    // Update dealer upcard
    if (dealerUpcardEl) {
        dealerUpcardEl.textContent = "-";
    }

    // Update running count
    if (runningCountEl) {
        runningCountEl.textContent = !isTestMode && showCounts && typeof state.runningCount === "number" && state.runningCount >= 0
            ? state.runningCount
            : "-";
    }

    // Update true count
    if (trueCountEl) {
        trueCountEl.textContent = !isTestMode && showCounts && typeof state.trueCount === "number" && state.trueCount >= 0
            ? state.trueCount.toFixed(2)
            : "-";
    }

    // Update decks remaining
    if (decksRemainingEl) {
        decksRemainingEl.textContent = !isTestMode && showCounts && typeof state.decksRemaining === "number"
            ? state.decksRemaining.toFixed(2)
            : "-";
    }
};

const getCardRankValue = (card) => {
    if (!card) {
        return "";
    }
    return card.rank?.value || card.rank?.label || card.rank || "";
};

const isAceRank = (card) => {
    const rankValue = getCardRankValue(card);
    const normalizedRank = String(rankValue).toUpperCase();
    return normalizedRank === "ACE" || normalizedRank === "1" || normalizedRank === "14";
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
    if (suit === "H") return "♥";
    if (suit === "D") return "♦";
    if (suit === "C") return "♣";
    if (suit === "S") return "♠";
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
            const suitSymbol = suitGlyph(face.suit);
            const isFaceCard = ['J', 'Q', 'K'].includes(face.rank);
            return `
                        <div class="playing-card" data-suit="${face.suit}" ${isDealer && originalIndex === 1 && phase !== "PLAYER_TURN" ? 'class="flipping"' : ''}>
                            <div class="card-corner">
                                <div class="corner-rank">${face.rank}</div>
                                <div class="corner-suit">${suitSymbol}</div>
                            </div>
                            <div class="card-center">
                                ${isFaceCard ? `<div class="face-letter">${face.rank}</div>` : ''}
                                <div class="center-suit">${suitSymbol}</div>
                            </div>
                            <div class="card-corner-bottom">
                                <div class="corner-rank">${face.rank}</div>
                                <div class="corner-suit">${suitSymbol}</div>
                            </div>
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
        return { cardsDealt: "-", trueCount: "-", text: "No count data available." };
    }
    let text = "Keep bets flat until more cards are seen.";
    if (visibleCards >= 8) {
        if (trueCount >= 2) {
            text = "Increase bet. Favor doubles on 10/11 and tougher stands in high counts.";
        } else if (trueCount <= -1) {
            text = "Avoid marginal doubles and play tighter.";
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
    const previousPhase = lastState?.phase;
    lastState = state;
    if (previousPhase !== "COMPLETE" && state.phase === "COMPLETE") {
        resetStartCardSelections();
    }
    updateDebugPanel(state);
    updateGameInfoDisplay(state);
    const strategy = strategyFromCount(state);
    const isTestMode = document.body.classList.contains("test-mode");
    cardsDealtEl.textContent = isTestMode ? "-" : strategy.cardsDealt;
    trueCountEl.textContent = isTestMode ? "-" : strategy.trueCount;
    strategyTextEl.textContent = isTestMode ? "-" : strategy.text;
    if (isTestMode) {
        suggestedBetEl.textContent = "-";
        if (state.suggestedBetUnits !== undefined && state.suggestedBetUnits !== null) {
            setTimeout(() => {
                suggestedBetEl.textContent = String(state.suggestedBetUnits);
            }, 250);
        }
    } else {
        suggestedBetEl.textContent = state.suggestedBetUnits ?? "-";
    }
    noteTextEl.textContent = state.note || "-";

    const results = Array.isArray(state.hands) ? state.hands : [];
    const wins = results.filter((hand) => hand.result === "WIN").length;
    const losses = results.filter((hand) => hand.result === "LOSE").length;
    const pushes = results.filter((hand) => hand.result === "PUSH").length;

    // Count double down wins and losses
    const doublesWon = results.filter((hand) => hand.betUnits === 2 && hand.result === "WIN").length;
    const doublesLost = results.filter((hand) => hand.betUnits === 2 && hand.result === "LOSE").length;

    handsWonEl.textContent = wins;
    handsLostEl.textContent = losses;
    handsPushEl.textContent = pushes;
    doublesWonEl.textContent = state.phase === "COMPLETE" ? doublesWon : "-";
    doublesLostEl.textContent = state.phase === "COMPLETE" ? doublesLost : "-";
    netUnitsEl.textContent = state.phase === "COMPLETE" ? (state.netUnits ?? 0) : "-";
    insuranceTotalEl.textContent = state.phase === "COMPLETE" ? (state.insuranceTotal ?? 0) : "-";

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

    const missingRecommendation = state.hands.some((hand) => !hand.recommendedAction);
    if (!missingRecommendation) {
        recommendationRefreshPending = false;
    } else if (!recommendationRefreshPending) {
        recommendationRefreshPending = true;
        const id = state.id;
        if (id) {
            apiFetch(`/api/blackjack/state/${id}`, { method: "POST" })
                .then((data) => renderState(data))
                .catch((error) => console.error("Recommendation refresh failed:", error));
        }
    }

    const activeIndex = state.phase === "PLAYER_TURN"
        ? state.hands.findIndex((hand) => !hand.busted && !hand.standing)
        : -1;

    console.log('RENDER STATE:', { phase: state.phase, activeIndex, handCount: state.hands.length, handStates: state.hands.map(h => ({ index: h.index, standing: h.standing, busted: h.busted, isFromSplit: h.isFromSplit })) });

    // Check dealer's up card for insurance phase
    const dealerStateCards = state.dealer?.cards || [];
    let dealerShowsAce = false;
    if (dealerStateCards.length > 0) {
        dealerShowsAce = isAceRank(dealerStateCards[0]);
    }

    const dealerOffersInsurance = dealerShowsAce || isTestMode;

    // Check if all hands have resolved insurance (made an explicit decision)
    const allHandsResolvedInsurance = state.hands.every(hand => {
        // A hand has resolved insurance if:
        // 1. It has taken insurance, OR
        // 2. It has explicitly declined insurance, OR
        // 2. It's been auto-stood (from natural blackjack), OR
        // 3. It's already moved past 2 cards (implicitly refused insurance), OR
        // 4. It's already busted or standing
        const isFromSplit = Boolean(hand.isFromSplit || hand.fromSplit);
        if (isFromSplit) {
            return true;
        }
        return hand.insured ||
            hand.insuranceDeclined ||
            hand.standing ||
            hand.busted ||
            (hand.cards && hand.cards.length > 2);
    });

    const normalizeAction = (value) => {
        const normalized = String(value || "")
            .toLowerCase()
            .replace(/[\s-]+/g, "_");
        if (normalized === "insurance") {
            return "insure";
        }
        if (normalized === "noinsurance") {
            return "no_insurance";
        }
        if (normalized === "declineinsurance") {
            return "decline_insurance";
        }
        return normalized;
    };

    const formatRecommendation = (value) => {
        if (!value) {
            return "";
        }
        return String(value).replace(/_/g, " ");
    };

    const getSplitFallbackRecommendation = (hand) => {
        if (!hand || typeof hand.total !== "number") {
            return "hit"; // Safe default
        }
        if (hand.soft) {
            return hand.total >= 18 ? "stand" : "hit";
        }
        return hand.total >= 17 ? "stand" : "hit";
    };

    const getEffectiveRecommendation = (hand, normalizedRecommendation, isFromSplit) => {
        // Check if recommendation is missing or invalid
        const validActions = ["hit", "stand", "double", "split", "insure", "no_insurance", "decline_insurance"];
        const hasValidRecommendation = normalizedRecommendation && normalizedRecommendation.trim() && validActions.includes(normalizedRecommendation);

        if (!hasValidRecommendation) {
            // Provide fallback recommendation for any hand without valid server recommendation
            return getSplitFallbackRecommendation(hand);
        }
        if (isFromSplit && normalizedRecommendation !== "hit" && normalizedRecommendation !== "stand") {
            return getSplitFallbackRecommendation(hand);
        }
        return normalizedRecommendation;
    };

    handList.innerHTML = state.hands
        .map((hand) => {
            const isActive = hand.index === activeIndex;
            const isFromSplit = Boolean(hand.isFromSplit || hand.fromSplit);
            const canDouble = isActive && hand.cards && hand.cards.length === 2 && !isFromSplit && (hand.total === 9 || hand.total === 10 || hand.total === 11);
            const canSplit = isActive && hand.cards && hand.cards.length === 2 &&
                getCardRankValue(hand.cards[0]) === getCardRankValue(hand.cards[1]);
            const canInsure = hand.cards && hand.cards.length === 2 && dealerOffersInsurance && !hand.insured && !hand.insuranceDeclined && !isFromSplit && !hand.standing && !hand.busted;

            // When dealer shows Ace, only insurance actions are allowed until ALL hands make a decision
            const dealerInInsurancePhase = dealerShowsAce && state.phase === "PLAYER_TURN" && !allHandsResolvedInsurance;

            // Enable regular actions only if:
            // 1. Dealer doesn't show an Ace, OR
            // 2. Phase is COMPLETE (round over), OR
            // 3. All hands have resolved their insurance decisions
            const regularActionsEnabled = isActive && (!dealerShowsAce || state.phase === "COMPLETE" || allHandsResolvedInsurance);

            console.log('Insurance check:', { isActive, hasCards: !!hand.cards, cardCount: hand.cards?.length, dealerShowsAce, alreadyInsured: hand.insured, canInsure, dealerInInsurancePhase, regularActionsEnabled, allHandsResolvedInsurance });
            const rawRecommendation = hand.recommendedAction || "";
            const normalizedRecommendation = normalizeAction(rawRecommendation);
            const recommendedAction = getEffectiveRecommendation(hand, normalizedRecommendation, isFromSplit);
            console.log(`Hand ${hand.index} - soft: ${hand.soft}, total: ${hand.total}, raw recommend: "${rawRecommendation}", normalized: "${normalizedRecommendation}", effective: "${recommendedAction}"`);
            const recommendInsure = normalizedRecommendation === "insure";
            const recommendDecline = normalizedRecommendation === "no_insurance" || normalizedRecommendation === "decline_insurance";
            const canHitAfterSplitAces = (hand.canHitAfterSplitAces ?? hand.isCanHitAfterSplitAces) !== false;
            const handPlayable = !hand.standing && !hand.busted;

            // For split aces with no hit allowed, the hand is complete - no actions available
            const isSplitAceComplete = isFromSplit && !canHitAfterSplitAces;

            // Log split ace hands
            if (isFromSplit && hand.cards?.length <= 2 && hand.cards?.[0]?.rank?.value === 1) {
                console.log('SPLIT ACE HAND STATE:', {
                    handIndex: hand.index,
                    isFromSplit,
                    canHitAfterSplitAces,
                    standing: hand.standing,
                    busted: hand.busted,
                    handPlayable,
                    isSplitAceComplete,
                    phase: state.phase
                });
            }

            const showHit = !isSplitAceComplete && regularActionsEnabled && handPlayable && canHitAfterSplitAces;
            const showStand = !isSplitAceComplete && regularActionsEnabled && handPlayable;
            const showDouble = !isSplitAceComplete && regularActionsEnabled && handPlayable && canDouble;
            const showSplit = !isSplitAceComplete && regularActionsEnabled && handPlayable && canSplit && !isFromSplit;

            if (isFromSplit && hand.getCards?.length === 2 && hand.cards?.[0]?.rank === 'ACE') {
                console.log('SPLIT ACE DEBUG:', { handIndex: hand.index, standing: hand.standing, busted: hand.busted, handPlayable, showHit, showStand, regularActionsEnabled, isActive, activeIndex, phase: state.phase });
            }

            // Calculate result display with bet units
            let resultDisplay = "";
            if (hand.result && hand.result !== "IN_PROGRESS") {
                const betUnits = hand.betUnits || 1;
                console.log('Result display:', { handIndex: hand.index, result: hand.result, betUnits: hand.betUnits, calculated: betUnits });
                if (hand.result === "WIN") {
                    resultDisplay = `<span class="tag">WIN +${betUnits}</span>`;
                } else if (hand.result === "LOSE") {
                    resultDisplay = `<span class="tag">LOSE -${betUnits}</span>`;
                } else if (hand.result === "PUSH") {
                    resultDisplay = `<span class="tag">PUSH</span>`;
                }
            }
            const result = resultDisplay;
            const doubled = (hand.betUnits === 2) ? `<span class="tag">DOUBLED</span>` : "";
            const hint = recommendedAction ? `<span class="tag">${formatRecommendation(recommendedAction)}</span>` : "";
            const insuranceDecision = "";
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

            const displayHand = hand.index + 1;
            return `
                        <div class="hand-row${isActive ? " active" : ""}${resultClass}">
                            <div class="hand-header">
                                <div class="row-title">Hand ${hand.index + 1}</div>
                                <div class="meta-line">
                                    <span>${handStatus}</span>
                                    ${result}
                                    ${doubled}
                                    ${hint}
                                </div>
                            </div>
                            <div class="cards">${renderCards(hand.cards)}</div>
                            <div class="hand-actions">
                                ${canInsure ? `<button class="insurance-btn ${recommendInsure ? "recommended" : ""}" data-action="insurance" data-value="yes" data-hand="${displayHand}">Insurance</button>` : ""}
                                ${canInsure ? `<button class="insurance-btn ghost ${recommendDecline ? "recommended" : ""}" data-action="insurance" data-value="no" data-hand="${displayHand}">No Insurance</button>` : ""}
                                ${showHit ? `<button class="${recommendedAction === "hit" ? "recommended" : ""}" data-action="hit" data-hand="${displayHand}">Hit</button>` : ""}
                                ${showStand ? `<button class="secondary ${recommendedAction === "stand" ? "recommended" : ""}" data-action="stand" data-hand="${displayHand}">Stand</button>` : ""}
                                ${showDouble ? `<button class="${recommendedAction === "double" ? "recommended" : ""}" data-action="double" data-hand="${displayHand}">Double</button>` : ""}
                                ${showSplit ? `<button class="${recommendedAction === "split" ? "recommended" : ""}" data-action="split" data-hand="${displayHand}">Split</button>` : ""}
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
        console.log("run() - executing task...");
        const data = await task();
        console.log("run() - received data:", data);
        renderState(data);
        setStatus("Updated with latest game state.", "ok");
    } catch (error) {
        console.error("run() - error:", error);
        setStatus(error.message || "Request failed.", "error");
        if (!document.body.classList.contains("test-mode")) {
            alert("Error: " + (error.message || "Request failed."));
        }
    } finally {
        setBusy(false);
    }
};

// Initialize everything
const initializeDebugMode = () => {
    // Check if debug mode is enabled via URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    const isDebug = urlParams.get('debug') === 'true' || localStorage.getItem('debug') === 'true';

    if (isDebug) {
        console.log('Debug mode enabled');

        // Show debug panel
        const debugPanel = document.getElementById('debugPanel');
        if (debugPanel) {
            debugPanel.style.visibility = 'visible';
            debugPanel.style.pointerEvents = 'auto';
        }

        // Show hidden buttons in debug mode
        const buttonsContainer = document.getElementById('hiddenButtonsContainer');
        if (buttonsContainer) {
            buttonsContainer.style.position = 'static';
            buttonsContainer.style.top = 'auto';
            buttonsContainer.style.left = 'auto';
            buttonsContainer.style.width = 'auto';
            buttonsContainer.style.height = 'auto';
            buttonsContainer.style.overflow = 'visible';
            buttonsContainer.style.pointerEvents = 'auto';
            buttonsContainer.style.display = 'flex';
            buttonsContainer.style.gap = '8px';
            buttonsContainer.style.marginTop = '12px';
            buttonsContainer.style.padding = '12px';
            buttonsContainer.style.backgroundColor = '#f5f5f5';
            buttonsContainer.style.borderTop = '1px solid #ccc';
        }
    }
};

const initializeCardSelectors = () => {
    console.log('initializeCardSelectors called');
    console.log('handsInput:', handsInput);
    console.log('cardSelectionContainer:', cardSelectionContainer);
    // Get initial hands value from input
    const initialHands = Number(handsInput?.value || 3);
    console.log('initialHands:', initialHands);
    // Generate player card selectors
    generatePlayerCardSelectors(initialHands);
    const firstPlayerRank = document.getElementById("playerHand1Card1Rank");
    if (firstPlayerRank) {
        setTimeout(() => firstPlayerRank.focus(), 0);
    }
};

const ensureCardSelectionVisible = () => {
    const details = document.getElementById("cardSelectionDetails");
    if (details) {
        details.open = true;
    }
};

// Listen to card selection changes for filtering
document.addEventListener('change', (e) => {
    if (e.target.classList.contains('card-rank-select') ||
        e.target.classList.contains('card-suit-select')) {
        updateDropdownFilters();
    }
    if (e.target.classList.contains('card-rank-select') && cardSelectionContainer) {
        const rankSelects = Array.from(cardSelectionContainer.querySelectorAll('.card-rank-select'));
        const currentIndex = rankSelects.indexOf(e.target);
        const nextSelect = currentIndex >= 0 ? rankSelects[currentIndex + 1] : null;
        if (nextSelect) {
            setTimeout(() => nextSelect.focus(), 0);
        }
    }
});

// Listen to hands input changes to regenerate player card selectors
if (handsInput) {
    handsInput.addEventListener('input', () => {
        const numHands = Number(handsInput.value || 1);
        generatePlayerCardSelectors(numHands);
        ensureCardSelectionVisible();
    });
}

// Initialize card selectors when page loads
// Since script is at the end of body, DOM should be ready
initializeDebugMode();
initializeCardSelectors();
ensureCardSelectionVisible();

if (navigator && navigator.webdriver) {
    document.body.classList.add("test-mode");
}

const startBtn = document.getElementById("startBtn");
if (startBtn) {
    startBtn.addEventListener("click", () => {
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

        if (suggestedBetEl) suggestedBetEl.textContent = "-";
        if (cardsDealtEl) cardsDealtEl.textContent = "-";
        if (strategyTextEl) strategyTextEl.textContent = "-";
        if (noteTextEl) noteTextEl.textContent = "-";
        if (trueCountEl) trueCountEl.textContent = "-";
        const phaseEl = document.getElementById("phase");
        if (phaseEl) phaseEl.textContent = "PLAYER_TURN";
        const gameIdEl = document.getElementById("gameId");
        if (gameIdEl) {
            gameIdEl.textContent = resolveGameId();
        }
        const gameStateEl = document.getElementById("gameState");
        if (gameStateEl) {
            gameStateEl.textContent = JSON.stringify({ id: resolveGameId(), phase: "PLAYER_TURN", hands: [] }, null, 2);
        }

        fallbackGameId = null;
        testModeGameId = null;

        run(() => apiFetch(`/api/blackjack/start?${params.toString()}`, { method: "POST" }));
    });
} else {
    console.error("Start button not found!");
}

handList.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
        return;
    }
    if (button.disabled) {
        return;
    }
    const action = button.dataset.action;
    const actionValue = button.dataset.value;
    const rawHand = Number(button.dataset.hand);
    const hand = Number.isNaN(rawHand) ? 0 : Math.max(0, rawHand - 1);

    console.log("Hand button clicked - action:", action, "hand:", hand, "dataset.hand:", button.dataset.hand);

    // Special handling for hit action - show card selection modal
    if (action === "hit") {
        console.log("Showing hit modal for hand:", hand);
        showHitCardModal(hand);
        return;
    }

    // Special handling for split action - show card selection modal
    if (action === "split") {
        console.log("Showing split modal for hand:", hand);
        showSplitCardModal(hand);
        return;
    }

    // Special handling for double action - show card selection modal
    if (action === "double") {
        console.log("Showing double modal for hand:", hand);
        showDoubleCardModal(hand);
        return;
    }

    if (action === "stand") {
        run(async () => {
            const id = guardGameId();
            if (shouldAutoDeclineInsurance(hand)) {
                await apiFetch(`/api/blackjack/decline-insurance/${id}?hand=${hand}`, { method: "POST" });
            }
            return apiFetch(`/api/blackjack/stand/${id}?hand=${hand}`, { method: "POST" });
        });
        return;
    }

    if (action === "insurance") {
        const resolvedAction = actionValue === "no" ? "decline-insurance" : "insurance";
        replaceGameStateNext = true;
        run(() => {
            const id = guardGameId();
            return apiFetch(`/api/blackjack/${resolvedAction}/${id}?hand=${hand}`, { method: "POST" });
        });
        return;
    }

    replaceGameStateNext = true;
    run(() => {
        const id = guardGameId();
        return apiFetch(`/api/blackjack/${action}/${id}?hand=${hand}`, { method: "POST" });
    });
});

// Standalone action button handlers
const hitBtn = document.getElementById("hitBtn");
const standBtn = document.getElementById("standBtn");
const doubleBtn = document.getElementById("doubleBtn");
const splitBtn = document.getElementById("splitBtn");

const getFirstActiveHandIndex = (state = lastState) => {
    if (state && state.hands && state.hands.length > 0) {
        // Find the first hand that is not standing or busted
        for (let i = 0; i < state.hands.length; i++) {
            const hand = state.hands[i];
            if (!hand.standing && !hand.busted) {
                return i;
            }
        }
        // If all hands are done, return the first one
        return 0;
    }
    return 0;
};

const dealerShowsAceInState = (state) => {
    const dealerCards = state?.dealer?.cards || [];
    return dealerCards.length > 0 && isAceRank(dealerCards[0]);
};

const shouldAutoDeclineInsurance = (handIndex) => {
    if (!lastState || lastState.phase !== "PLAYER_TURN") {
        return false;
    }
    if (!dealerShowsAceInState(lastState)) {
        return false;
    }
    const hand = lastState.hands?.[handIndex];
    if (!hand) {
        return false;
    }
    const isFromSplit = Boolean(hand.isFromSplit || hand.fromSplit);
    return !isFromSplit && !hand.insured && !hand.insuranceDeclined;
};

if (hitBtn) {
    hitBtn.addEventListener("click", () => {
        if (hitBtn.disabled) return;
        const handIndex = getFirstActiveHandIndex();
        replaceGameStateNext = true;
        run(() => {
            const id = guardGameId();
            return apiFetch(`/api/blackjack/hit/${id}?hand=${handIndex}`, { method: "POST" });
        });
    });
}

if (standBtn) {
    standBtn.addEventListener("click", () => {
        if (standBtn.disabled) return;
        run(async () => {
            const id = guardGameId();
            let nextState = lastState;

            while (nextState && nextState.phase === "PLAYER_TURN") {
                const activeIndex = getFirstActiveHandIndex(nextState);
                const currentHand = nextState?.hands?.[activeIndex];
                if (currentHand && (currentHand.standing || currentHand.busted)) {
                    break;
                }
                if (shouldAutoDeclineInsurance(activeIndex)) {
                    await apiFetch(`/api/blackjack/decline-insurance/${id}?hand=${activeIndex}`, { method: "POST" });
                }
                nextState = await apiFetch(`/api/blackjack/stand/${id}?hand=${activeIndex}`, { method: "POST" });
            }

            return nextState || apiFetch(`/api/blackjack/stand/${id}?hand=0`, { method: "POST" });
        });
    });
}

if (doubleBtn) {
    doubleBtn.addEventListener("click", () => {
        if (doubleBtn.disabled) return;
        const handIndex = getFirstActiveHandIndex();
        showDoubleCardModal(handIndex);
    });
}

if (splitBtn) {
    splitBtn.addEventListener("click", () => {
        if (splitBtn.disabled) return;
        const handIndex = getFirstActiveHandIndex();
        showSplitCardModal(handIndex);
    });
}

// Hit card modal functions
const hitCardModal = document.getElementById("hitCardModal");
const hitCardRankSelect = document.getElementById("hitCardRank");
const hitCardSuitSelect = document.getElementById("hitCardSuit");
const hitCardConfirmBtn = document.getElementById("hitCardConfirmBtn");
const hitCardCancelBtn = document.getElementById("hitCardCancelBtn");

let currentHitHandIndex = null;

// Get all cards currently in play from game state
const getCardsInPlay = () => {
    const cardsInPlay = [];
    if (!lastState) return cardsInPlay;

    // Get dealer cards
    if (lastState.dealer && lastState.dealer.cards) {
        lastState.dealer.cards.forEach(card => {
            if (card && card.rank && card.suit) {
                const rank = card.rank.value || card.rank.label || card.rank;
                const suit = card.suit.value || card.suit.label || card.suit;
                cardsInPlay.push({ rank: String(rank).toUpperCase(), suit: String(suit).toUpperCase() });
            }
        });
    }

    // Get player hands cards
    if (lastState.hands && Array.isArray(lastState.hands)) {
        lastState.hands.forEach(hand => {
            if (hand.cards && Array.isArray(hand.cards)) {
                hand.cards.forEach(card => {
                    if (card && card.rank && card.suit) {
                        const rank = card.rank.value || card.rank.label || card.rank;
                        const suit = card.suit.value || card.suit.label || card.suit;
                        cardsInPlay.push({ rank: String(rank).toUpperCase(), suit: String(suit).toUpperCase() });
                    }
                });
            }
        });
    }

    return cardsInPlay;
};

const populateHitCardSelectors = () => {
    if (!hitCardRankSelect || !hitCardSuitSelect) {
        console.error("Hit card selectors not found!");
        return;
    }

    const cardsInPlay = getCardsInPlay();
    const usedCards = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));

    // Filter ranks that have at least one available suit
    const availableRanks = RANKS.filter(rank => {
        return SUITS.some(suit => !usedCards.has(`${rank.value}:${suit.value}`));
    });

    // Populate ranks with available options only
    buildOptions(hitCardRankSelect, availableRanks, true);

    // Initial suits population (all suits since no rank selected yet)
    buildOptions(hitCardSuitSelect, SUITS, true);
};

// Update suits when rank is selected
const updateHitCardSuits = () => {
    if (!hitCardRankSelect || !hitCardSuitSelect) return;

    const selectedRank = hitCardRankSelect.value;
    console.log("updateHitCardSuits - selected rank:", selectedRank);

    if (!selectedRank) {
        // No rank selected, show all suits with random option
        buildOptions(hitCardSuitSelect, SUITS, true, false);
        console.log("No rank selected - showing all suits with Random option");
        return;
    }

    const cardsInPlay = getCardsInPlay();
    const usedCards = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));

    // Filter suits available for the selected rank
    const availableSuits = SUITS.filter(suit => {
        return !usedCards.has(`${selectedRank}:${suit.value}`);
    });

    console.log("Available suits for rank", selectedRank, ":", availableSuits.map(s => s.value));

    // Populate suits (no random option when rank is selected) and auto-select first
    buildOptions(hitCardSuitSelect, availableSuits, false, true);

    console.log("Auto-selected suit:", hitCardSuitSelect.value);
};

const showHitCardModal = (handIndex) => {
    console.log("showHitCardModal called with handIndex:", handIndex, "type:", typeof handIndex);
    if (!hitCardModal || !hitCardRankSelect || !hitCardSuitSelect) {
        console.error("Hit card modal elements not found!");
        return;
    }
    currentHitHandIndex = handIndex;
    console.log("Set currentHitHandIndex to:", currentHitHandIndex);
    populateHitCardSelectors();
    hitCardRankSelect.value = "";
    hitCardSuitSelect.value = "";

    // Add event listener for rank changes
    hitCardRankSelect.removeEventListener('change', updateHitCardSuits);
    hitCardRankSelect.addEventListener('change', updateHitCardSuits);

    hitCardModal.style.display = "flex";
    setTimeout(() => hitCardRankSelect.focus(), 0);
};

const closeHitCardModal = () => {
    if (!hitCardModal) return;
    hitCardModal.style.display = "none";
    currentHitHandIndex = null;
};

if (hitCardCancelBtn) {
    console.log("Adding cancel button listener");
    hitCardCancelBtn.addEventListener("click", () => {
        console.log("Cancel button clicked");
        closeHitCardModal();
    });
} else {
    console.error("hitCardCancelBtn not found!");
}

if (hitCardConfirmBtn) {
    console.log("Adding confirm button listener");
    hitCardConfirmBtn.addEventListener("click", () => {
        console.log("Confirm button clicked!");
        console.log("currentHitHandIndex at confirm time:", currentHitHandIndex);

        // Save hand index to local variable before closing modal
        const handIndex = currentHitHandIndex;
        const rank = hitCardRankSelect ? hitCardRankSelect.value : "";
        const suit = hitCardSuitSelect ? hitCardSuitSelect.value : "";

        console.log("Hit confirm - hand:", handIndex, "rank:", rank, "suit:", suit);

        // Validate: both or neither
        if ((rank && !suit) || (!rank && suit)) {
            alert("Please select both rank and suit, or leave both as Random");
            return;
        }

        console.log("Closing modal and calling run()");
        closeHitCardModal();

        replaceGameStateNext = true;
        run(() => {
            console.log("Inside run task function - handIndex:", handIndex);
            const id = guardGameId();
            console.log("Game ID:", id);
            const params = new URLSearchParams();
            params.append("hand", handIndex);
            console.log("params.toString():", params.toString());
            if (rank) params.append("rank", rank);
            if (suit) params.append("suit", suit);
            const url = `/api/blackjack/hit/${id}?${params.toString()}`;
            console.log("Hit API call - URL:", url);
            return apiFetch(url, { method: "POST" });
        });
    });
} else {
    console.error("hitCardConfirmBtn not found!");
}

// Close modal on background click
if (hitCardModal) {
    hitCardModal.addEventListener("click", (e) => {
        if (e.target === hitCardModal) {
            closeHitCardModal();
        }
    });
}

// Double card modal functions
const doubleCardModal = document.getElementById("doubleCardModal");
const doubleCardRankSelect = document.getElementById("doubleCardRank");
const doubleCardSuitSelect = document.getElementById("doubleCardSuit");
const doubleCardConfirmBtn = document.getElementById("doubleCardConfirmBtn");
const doubleCardCancelBtn = document.getElementById("doubleCardCancelBtn");

let currentDoubleHandIndex = null;

const populateDoubleCardSelectors = () => {
    if (!doubleCardRankSelect || !doubleCardSuitSelect) {
        console.error("Double card selectors not found!");
        return;
    }

    const cardsInPlay = getCardsInPlay();
    const usedCards = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));

    const availableRanks = RANKS.filter(rank => {
        return SUITS.some(suit => !usedCards.has(`${rank.value}:${suit.value}`));
    });

    buildOptions(doubleCardRankSelect, availableRanks, true);
    buildOptions(doubleCardSuitSelect, SUITS, true);
};

const updateDoubleCardSuits = () => {
    if (!doubleCardRankSelect || !doubleCardSuitSelect) return;

    const selectedRank = doubleCardRankSelect.value;
    if (!selectedRank) {
        buildOptions(doubleCardSuitSelect, SUITS, true, false);
        return;
    }

    const cardsInPlay = getCardsInPlay();
    const usedCards = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));

    const availableSuits = SUITS.filter(suit => {
        return !usedCards.has(`${selectedRank}:${suit.value}`);
    });

    buildOptions(doubleCardSuitSelect, availableSuits, false, true);
};

const showDoubleCardModal = (handIndex) => {
    console.log("showDoubleCardModal called with handIndex:", handIndex);
    if (!doubleCardModal || !doubleCardRankSelect || !doubleCardSuitSelect) {
        console.error("Double card modal elements not found!");
        return;
    }
    currentDoubleHandIndex = handIndex;
    populateDoubleCardSelectors();
    doubleCardRankSelect.value = "";
    doubleCardSuitSelect.value = "";

    doubleCardRankSelect.removeEventListener('change', updateDoubleCardSuits);
    doubleCardRankSelect.addEventListener('change', updateDoubleCardSuits);

    doubleCardModal.style.display = "flex";
    setTimeout(() => doubleCardRankSelect.focus(), 0);
};

const closeDoubleCardModal = () => {
    if (!doubleCardModal) return;
    doubleCardModal.style.display = "none";
    currentDoubleHandIndex = null;
};

if (doubleCardCancelBtn) {
    doubleCardCancelBtn.addEventListener("click", () => {
        closeDoubleCardModal();
    });
}

if (doubleCardConfirmBtn) {
    doubleCardConfirmBtn.addEventListener("click", () => {
        const handIndex = currentDoubleHandIndex;
        const rank = doubleCardRankSelect ? doubleCardRankSelect.value : "";
        const suit = doubleCardSuitSelect ? doubleCardSuitSelect.value : "";

        console.log("Double confirm - hand:", handIndex, "rank:", rank, "suit:", suit);

        if ((rank && !suit) || (!rank && suit)) {
            alert("Please select both rank and suit, or leave both as Random");
            return;
        }

        closeDoubleCardModal();

        replaceGameStateNext = true;
        run(() => {
            const id = guardGameId();
            const params = new URLSearchParams();
            params.append("hand", handIndex);
            if (rank) params.append("rank", rank);
            if (suit) params.append("suit", suit);
            const url = `/api/blackjack/double/${id}?${params.toString()}`;
            console.log("Double API call - URL:", url);
            return apiFetch(url, { method: "POST" });
        });
    });
}

if (doubleCardModal) {
    doubleCardModal.addEventListener("click", (e) => {
        if (e.target === doubleCardModal) {
            closeDoubleCardModal();
        }
    });
}

// Split card modal functions
const splitCardModal = document.getElementById("splitCardModal");
const splitCard1RankSelect = document.getElementById("splitCard1Rank");
const splitCard1SuitSelect = document.getElementById("splitCard1Suit");
const splitCard2RankSelect = document.getElementById("splitCard2Rank");
const splitCard2SuitSelect = document.getElementById("splitCard2Suit");
const splitCardConfirmBtn = document.getElementById("splitCardConfirmBtn");
const splitCardCancelBtn = document.getElementById("splitCardCancelBtn");

const isModalOpen = (modal) => {
    if (!modal) return false;
    return getComputedStyle(modal).display !== "none";
};

document.addEventListener("keydown", (event) => {
    if (event.key !== "Enter") {
        return;
    }

    if (isModalOpen(splitCardModal) && splitCardConfirmBtn) {
        event.preventDefault();
        splitCardConfirmBtn.click();
        return;
    }

    if (isModalOpen(hitCardModal) && hitCardConfirmBtn) {
        event.preventDefault();
        hitCardConfirmBtn.click();
        return;
    }

    if (isModalOpen(doubleCardModal) && doubleCardConfirmBtn) {
        event.preventDefault();
        doubleCardConfirmBtn.click();
        return;
    }

    const startBtn = document.getElementById("startBtn");
    if (startBtn && !startBtn.disabled) {
        event.preventDefault();
        startBtn.click();
    }
});

let currentSplitHandIndex = null;

const populateSplitCardSelectors = () => {
    if (!splitCard1RankSelect || !splitCard1SuitSelect || !splitCard2RankSelect || !splitCard2SuitSelect) {
        console.error("Split card selectors not found!");
        return;
    }

    const cardsInPlay = getCardsInPlay();
    const usedCards = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));

    // Filter ranks for card 1 (exclude card 2's selection)
    const card2Rank = splitCard2RankSelect?.value;
    const card2Suit = splitCard2SuitSelect?.value;
    if (card2Rank && card2Suit) {
        usedCards.add(`${card2Rank}:${card2Suit}`);
    }

    const availableRanks1 = RANKS.filter(rank => {
        return SUITS.some(suit => !usedCards.has(`${rank.value}:${suit.value}`));
    });

    buildOptions(splitCard1RankSelect, availableRanks1, true);

    // Filter ranks for card 2 (exclude card 1's selection)
    const usedCards2 = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));
    const card1Rank = splitCard1RankSelect?.value;
    const card1Suit = splitCard1SuitSelect?.value;
    if (card1Rank && card1Suit) {
        usedCards2.add(`${card1Rank}:${card1Suit}`);
    }

    const availableRanks2 = RANKS.filter(rank => {
        return SUITS.some(suit => !usedCards2.has(`${rank.value}:${suit.value}`));
    });

    buildOptions(splitCard2RankSelect, availableRanks2, true);

    // Initial suits population (all suits since no rank selected yet)
    buildOptions(splitCard1SuitSelect, SUITS, true);
    buildOptions(splitCard2SuitSelect, SUITS, true);
};

// Update suits when rank is selected for card 1
const updateSplitCard1Suits = () => {
    if (!splitCard1RankSelect || !splitCard1SuitSelect) return;

    const selectedRank = splitCard1RankSelect.value;

    if (!selectedRank) {
        buildOptions(splitCard1SuitSelect, SUITS, true, false);
        return;
    }

    const cardsInPlay = getCardsInPlay();
    const usedCards = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));

    // Also exclude the card selected in the other dropdown
    const card2Rank = splitCard2RankSelect?.value;
    const card2Suit = splitCard2SuitSelect?.value;
    if (card2Rank && card2Suit) {
        usedCards.add(`${card2Rank}:${card2Suit}`);
    }

    const availableSuits = SUITS.filter(suit => {
        return !usedCards.has(`${selectedRank}:${suit.value}`);
    });

    buildOptions(splitCard1SuitSelect, availableSuits, false, true);
};

// Update suits when rank is selected for card 2
const updateSplitCard2Suits = () => {
    if (!splitCard2RankSelect || !splitCard2SuitSelect) return;

    const selectedRank = splitCard2RankSelect.value;

    if (!selectedRank) {
        buildOptions(splitCard2SuitSelect, SUITS, true, false);
        return;
    }

    const cardsInPlay = getCardsInPlay();
    const usedCards = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));

    // Also exclude the card selected in the other dropdown
    const card1Rank = splitCard1RankSelect?.value;
    const card1Suit = splitCard1SuitSelect?.value;
    if (card1Rank && card1Suit) {
        usedCards.add(`${card1Rank}:${card1Suit}`);
    }

    const availableSuits = SUITS.filter(suit => {
        return !usedCards.has(`${selectedRank}:${suit.value}`);
    });

    buildOptions(splitCard2SuitSelect, availableSuits, false, true);
};

const showSplitCardModal = (handIndex) => {
    console.log("showSplitCardModal called with handIndex:", handIndex);
    if (!splitCardModal || !splitCard1RankSelect || !splitCard1SuitSelect || !splitCard2RankSelect || !splitCard2SuitSelect) {
        console.error("Split card modal elements not found!");
        return;
    }
    currentSplitHandIndex = handIndex;
    populateSplitCardSelectors();
    splitCard1RankSelect.value = "";
    splitCard1SuitSelect.value = "";
    splitCard2RankSelect.value = "";
    splitCard2SuitSelect.value = "";

    // Add event listeners for rank changes
    splitCard1RankSelect.removeEventListener('change', updateSplitCard1Suits);
    splitCard1RankSelect.addEventListener('change', updateSplitCard1Suits);
    splitCard2RankSelect.removeEventListener('change', updateSplitCard2Suits);
    splitCard2RankSelect.addEventListener('change', updateSplitCard2Suits);

    splitCardModal.style.display = "flex";
    setTimeout(() => splitCard1RankSelect.focus(), 0);
};

const closeSplitCardModal = () => {
    if (!splitCardModal) return;
    splitCardModal.style.display = "none";
    currentSplitHandIndex = null;
};

if (splitCardCancelBtn) {
    splitCardCancelBtn.addEventListener("click", () => {
        console.log("Split cancel button clicked");
        closeSplitCardModal();
    });
}

if (splitCardConfirmBtn) {
    splitCardConfirmBtn.addEventListener("click", () => {
        console.log("Split confirm button clicked!");

        // Save hand index to local variable before closing modal
        const handIndex = currentSplitHandIndex;
        const card1Rank = splitCard1RankSelect ? splitCard1RankSelect.value : "";
        const card1Suit = splitCard1SuitSelect ? splitCard1SuitSelect.value : "";
        const card2Rank = splitCard2RankSelect ? splitCard2RankSelect.value : "";
        const card2Suit = splitCard2SuitSelect ? splitCard2SuitSelect.value : "";

        console.log("Split confirm - hand:", handIndex, "card1:", card1Rank, card1Suit, "card2:", card2Rank, card2Suit);

        // Validate: for each card, both or neither
        if ((card1Rank && !card1Suit) || (!card1Rank && card1Suit)) {
            alert("For Card 1: Please select both rank and suit, or leave both as Random");
            return;
        }
        if ((card2Rank && !card2Suit) || (!card2Rank && card2Suit)) {
            alert("For Card 2: Please select both rank and suit, or leave both as Random");
            return;
        }

        // Validate: cannot select the same card twice
        if (card1Rank && card1Suit && card2Rank && card2Suit) {
            if (card1Rank === card2Rank && card1Suit === card2Suit) {
                alert("Cannot select the same card for both hands");
                return;
            }
        }

        closeSplitCardModal();

        replaceGameStateNext = true;
        run(() => {
            const id = guardGameId();
            const params = new URLSearchParams();
            params.append("hand", handIndex);
            if (card1Rank) params.append("card1Rank", card1Rank);
            if (card1Suit) params.append("card1Suit", card1Suit);
            if (card2Rank) params.append("card2Rank", card2Rank);
            if (card2Suit) params.append("card2Suit", card2Suit);
            const url = `/api/blackjack/split/${id}?${params.toString()}`;
            console.log("Split API call - URL:", url);
            console.log("Split params - hand:", handIndex, "card1Rank:", card1Rank, "card1Suit:", card1Suit, "card2Rank:", card2Rank, "card2Suit:", card2Suit);
            return apiFetch(url, { method: "POST" });
        });
    });
}

// Close modal on background click
if (splitCardModal) {
    splitCardModal.addEventListener("click", (e) => {
        if (e.target === splitCardModal) {
            closeSplitCardModal();
        }
    });
}
