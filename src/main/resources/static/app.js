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

const handsInput = document.getElementById("hands");
const handList = document.getElementById("handList");
const dealerRow = document.getElementById("dealerRow");
const dealerCards = document.getElementById("dealerCards");
const dealerMeta = document.getElementById("dealerMeta");
const cardSelectionContainer = document.getElementById("cardSelectionContainer");

const cardsDealtEl = document.getElementById("cardsDealt");
const strategyTextEl = document.getElementById("strategyText");
const noteTextEl = document.getElementById("noteText");
const handsWonEl = document.getElementById("handsWon");
const handsLostEl = document.getElementById("handsLost");
const handsPushEl = document.getElementById("handsPush");
const netUnitsEl = document.getElementById("netUnits");
const doublesWonEl = document.getElementById("doublesWon");
const doublesLostEl = document.getElementById("doublesLost");
const insuranceTotalEl = document.getElementById("insuranceTotal");

const gameIdEl = document.getElementById("gameId");
const phaseEl = document.getElementById("phase");
const dealerUpcardEl = document.getElementById("dealerUpcard");
const runningCountEl = document.getElementById("runningCount");
const trueCountEl = document.getElementById("trueCount");
const cardsRemainingEl = document.getElementById("cardsRemaining");

let skipAutoModal = false;
let isBusy = false;
const setBusy = (busy) => {
    isBusy = busy;
    updateButtonStates();
    // Remove fallback timeout logic
    if (hitModalFallbackTimeout) {
        clearTimeout(hitModalFallbackTimeout);
        hitModalFallbackTimeout = null;
    }
};

const run = async (fn) => {
    setBusy(true);
    try {
        const result = await fn();
        if (result && typeof result === 'object') {
            renderState(result);
        }
    } catch (err) {
        console.error("Error in run():", err);
        alert(err.message || "An error occurred.");
    } finally {
        setBusy(false);
    }
};

let lastState = null;
let replaceGameStateNext = false;

// Returns the current gameId, using lastState or fallback
function guardGameId() {
    // Try to get gameId from lastState
    if (lastState && (lastState.id || lastState.gameId)) {
        return lastState.id || lastState.gameId;
    }
    // No fallback logic, return null
    return null;
}

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

    let canAct = lastState && lastState.phase === "PLAYER_TURN" && !isBusy;
    if (hitBtn) hitBtn.disabled = !canAct;
    if (standBtn) standBtn.disabled = !canAct;
    if (doubleBtn) doubleBtn.disabled = !canAct;
    if (splitBtn) splitBtn.disabled = !canAct;
    document.querySelectorAll(".hand-actions button").forEach((button) => {
        button.disabled = isBusy;
    });
};

const canDouble = (hand, isActive) => {
    return isActive && hand.cards && hand.cards.length === 2 && !hand.soft && (hand.total === 9 || hand.total === 10 || hand.total === 11);
};

const canSplit = (hand, isActive) => {
    return isActive && hand.cards && hand.cards.length === 2 && getCardRankValue(hand.cards[0]) === getCardRankValue(hand.cards[1]);
};

const canHit = (hand, isSplitAceComplete, canHitAfterSplitAces) => {
    return !isSplitAceComplete && !hand.standing && !hand.busted && canHitAfterSplitAces;
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
    let lastInserted = dealerSelectionRow;
    for (let handNum = 1; handNum <= numHands; handNum++) {
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
        if (lastInserted && lastInserted.parentNode) {
            lastInserted.parentNode.insertBefore(handRow, lastInserted.nextSibling);
        } else {
            cardSelectionContainer.appendChild(handRow);
        }
        lastInserted = handRow;
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
    clearSelectOptions(select);
    if (includeRandom) addRandomOption(select);
    addOptionsToSelect(select, options, selectFirst);
    sizeSelectToContent(select);
};

const clearSelectOptions = (select) => {
    while (select.options.length > 0) {
        select.remove(0);
    }
};

const addRandomOption = (select) => {
    const randomOption = document.createElement('option');
    randomOption.value = '';
    randomOption.textContent = 'Random';
    select.appendChild(randomOption);
};

const addOptionsToSelect = (select, options, selectFirst = false) => {
    options.forEach((optionItem, index) => {
        const option = document.createElement('option');
        option.value = optionItem.value;
        option.textContent = optionItem.label;
        if (selectFirst && index === 0) {
            option.selected = true;
        }
        select.appendChild(option);
    });
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

    // Get number of decks from input
    const decksInput = document.getElementById("decks");
    const numDecks = decksInput ? Number(decksInput.value || 6) : 6;

    // Count how many times each card is selected, but ignore empty values
    const cardCount = {};
    selectedCards.forEach(card => {
        if (card.rank && card.suit) {
            const key = `${card.rank}:${card.suit}`;
            cardCount[key] = (cardCount[key] || 0) + 1;
        }
    });

    document.querySelectorAll('.card-rank-select').forEach(rankSelect => {
        const cardId = rankSelect.dataset.card;
        const suitSelect = document.querySelector(`.card-suit-select[data-card="${cardId}"]`);
        if (!suitSelect) return;

        const currentRank = rankSelect.value;
        const currentSuit = suitSelect.value;

        // Filter rank options: only allow ranks with at least one suit available (not over deck limit)
        const rankOptions = RANKS.filter(rank => {
            return SUITS.some(suit => {
                const key = `${rank.value}:${suit.value}`;
                // Allow if not over deck limit or this dropdown is selecting it
                const selectedCount = cardCount[key] || 0;
                if (currentRank === rank.value && currentSuit === suit.value) {
                    // Allow selection if not over deck limit
                    return selectedCount <= numDecks;
                }
                return selectedCount < numDecks;
            });
        });

        buildOptions(rankSelect, rankOptions, true);

        if (currentRank && rankOptions.some(rank => rank.value === currentRank)) {
            rankSelect.value = currentRank;
        }

        const effectiveRank = rankSelect.value;
        // Filter suit options: only allow suits for selected rank not over deck limit
        const suitOptions = effectiveRank
            ? SUITS.filter(suit => {
                const key = `${effectiveRank}:${suit.value}`;
                const selectedCount = cardCount[key] || 0;
                if (currentRank === effectiveRank && currentSuit === suit.value) {
                    return selectedCount <= numDecks;
                }
                return selectedCount < numDecks;
            })
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

const updateGameInfoDisplay = (state) => {
    gameIdEl.textContent = state?.id || state?.gameId;
    phaseEl.textContent = state.phase || '-';
    dealerUpcardEl.textContent = "-";
    if (runningCountEl) {
        runningCountEl.textContent = typeof state.runningCount === "number" && state.runningCount >= 0
            ? state.runningCount
            : "-";
    }
    trueCountEl.textContent = typeof state.trueCount === "number" && state.trueCount >= 0
        ? state.trueCount.toFixed(2)
        : "-";
    cardsRemainingEl.textContent = typeof state.cardsRemaining === "number"
        ? state.cardsRemaining
        : "-";
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

    // For dealer, reverse the order so up card is left, down card is right
    const cardsToRender = isDealer ? [...list] : list;
    if (isDealer && cardsToRender.length === 2) {
        // Up card is index 0, down card is index 1; keep as [up, down]
        // No change needed, but ensure left is up card
    }

    return cardsToRender
        .map((card, displayIndex) => {
            // For dealer, displayIndex 0 is up card (left), 1 is down card (right)
            const isHoleCard = isDealer && displayIndex === 1 && phase === "PLAYER_TURN";

            if (isHoleCard) {
                return `<div class="playing-card card-back" title="Hole card (hidden)"></div>`;
            }

            const face = cardFace(card);
            const suitSymbol = suitGlyph(face.suit);
            const isFaceCard = ['J', 'Q', 'K'].includes(face.rank);
            return `
                        <div class="playing-card" data-suit="${face.suit}"${isDealer && displayIndex === 1 && phase !== "PLAYER_TURN" ? ' class="flipping"' : ''}>
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

const renderState = (state) => {
    const previousPhase = lastState?.phase;
    lastState = state;
    if (previousPhase !== "COMPLETE" && state.phase === "COMPLETE") {
        resetStartCardSelections();

        // Set focus to dealer up card rank select
        const dealerRank = document.getElementById("dealerCard1Rank");
        setTimeout(() => dealerRank.focus(), 0);
    }

    updateGameInfoDisplay(state);
    if (cardsDealtEl) cardsDealtEl.textContent = "-";
    if (trueCountEl) trueCountEl.textContent = (typeof state.trueCount === "number" ? state.trueCount.toFixed(2) : "-");
    // Show the recommendation for the active hand in the strategy panel
    let strategyText = "-";
    if (state.hands && state.hands.length > 0) {
        // Find the next actionable hand (not busted, not standing, not completed)
        const actionableHand = state.hands.find((hand) => !hand.busted && !hand.standing && hand.result !== "WIN" && hand.result !== "LOSE" && hand.result !== "PUSH");
        let rec = actionableHand ? actionableHand.recommendedAction : null;
        if (actionableHand && actionableHand.soft && rec && rec.toLowerCase() === "double") {
            rec = "hit";
        } else if (actionableHand && actionableHand.soft && rec && !rec.toLowerCase().includes("soft")) {
            rec = `soft ${rec}`;
        }
        // Also normalize double recommendation for soft hands in action buttons
        const normalizedRec = String(rec || "").toLowerCase().replace(/\s|-+/g, "_");
        if (normalizedRec === "insure" || normalizedRec === "insurance" || normalizedRec === "no_insurance" || normalizedRec === "decline_insurance") {
            strategyText = `<span class='strategy-recommendation'>${rec}</span>`;
        } else if (rec && state.phase === "PLAYER_TURN") {
            strategyText = `<span class='strategy-recommendation'>${rec}</span>`;
        } else if (state.phase === "COMPLETE") {
            strategyText = `<span class='strategy-recommendation'>Game complete</span>`;
        } else {
            strategyText = "-";
        }
    }
    if (strategyTextEl) strategyTextEl.innerHTML = strategyText;
    if (noteTextEl) noteTextEl.textContent = state.note || "-";

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
        // Always show dealer up card to the left of down card
        let dealerCardsToShow = state.dealer.cards;
        // No need to reorder, renderCards handles left=up card, right=down card
        dealerCards.innerHTML = renderCards(dealerCardsToShow, true, state.phase);
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

    console.log('RENDER STATE:', { phase: state.phase, activeIndex, handCount: state.hands.length, handStates: state.hands.map(h => ({ index: h.index, standing: h.standing, busted: h.busted, isFromSplit: h.isFromSplit })) });

    // Check dealer's up card for insurance phase
    const dealerStateCards = state.dealer?.cards || [];
    let dealerShowsAce = false;
    if (dealerStateCards.length > 0) {
        dealerShowsAce = isAceRank(dealerStateCards[0]);
    }

    const dealerOffersInsurance = dealerShowsAce;

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

    handList.innerHTML = state.hands
        .map((hand) => {
            const isActive = hand.index === activeIndex;
            const isFromSplit = Boolean(hand.isFromSplit || hand.fromSplit);
            const canDoubleVal = canDouble(hand, isActive);
            const canSplitVal = canSplit(hand, isActive);
            // Restore canInsure definition
            const canInsure = hand.cards && hand.cards.length === 2 && dealerOffersInsurance && !hand.insured && !hand.insuranceDeclined && !isFromSplit && !hand.standing && !hand.busted;
            // Compute split ace complete and handPlayable consistently
            const canHitAfterSplitAces = (hand.canHitAfterSplitAces ?? hand.isCanHitAfterSplitAces) !== false;
            const handPlayable = !hand.standing && !hand.busted;
            const isSplitAceComplete = isFromSplit && !canHitAfterSplitAces;
            let showHit = canHit(hand, isSplitAceComplete, canHitAfterSplitAces);
            let showStand = !isSplitAceComplete && handPlayable;
            let showDouble = !isSplitAceComplete && handPlayable && canDoubleVal;
            let showSplit = !isSplitAceComplete && handPlayable && canSplitVal && !isFromSplit;

            const rawRecommendation = hand.recommendedAction || "";
            let normalizedRecommendation = normalizeAction(rawRecommendation);
            if (hand.soft && normalizedRecommendation === "double") {
                normalizedRecommendation = "hit";
            }
            const recommendedAction = isActive && normalizedRecommendation && normalizedRecommendation !== "none"
                ? normalizedRecommendation
                : "";

            let insuranceBtnClass = "insurance-btn";
            let noInsuranceBtnClass = "insurance-btn ghost";
            if (canInsure) {
                if (normalizedRecommendation === "insure") {
                    insuranceBtnClass += " recommended";
                } else if (normalizedRecommendation === "no_insurance" || normalizedRecommendation === "decline_insurance") {
                    noInsuranceBtnClass += " recommended";
                }
            }

            let resultDisplay = "";
            if (hand.result && hand.result !== "IN_PROGRESS") {
                const betUnits = hand.betUnits || 1;
                if (hand.result === "WIN") {
                    resultDisplay = `<span class=\"tag\">WIN +${betUnits}</span>`;
                } else if (hand.result === "LOSE") {
                    resultDisplay = `<span class=\"tag\">LOSE -${betUnits}</span>`;
                } else if (hand.result === "PUSH") {
                    resultDisplay = `<span class=\"tag\">PUSH</span>`;
                }
            }
            const result = resultDisplay;
            const doubled = (hand.betUnits === 2) ? `<span class=\"tag\">DOUBLED</span>` : "";
            const hint = recommendedAction ? `<span class=\"tag recommended\">${formatRecommendation(recommendedAction)}</span>` : "";
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

            // Action buttons for the active hand
            let actionsHtml = '';
            // Only show insurance buttons if needed
            if (canInsure && state.phase === "PLAYER_TURN") {
                const isInsureRecommended = normalizedRecommendation === "insure";
                const isDeclineRecommended = normalizedRecommendation === "no_insurance" || normalizedRecommendation === "decline_insurance";
                actionsHtml += `<div class="hand-actions">
                    <button data-action="insure" data-hand="${hand.index + 1}" class="insurance-btn${isInsureRecommended ? ' recommended' : ''}">Accept Insurance</button>
                    <button data-action="decline_insurance" data-hand="${hand.index + 1}" class="insurance-btn ghost${isDeclineRecommended ? ' recommended' : ''}">Decline Insurance</button>
                </div>`;
            }
            // Show player actions for the first active hand immediately after insurance is resolved for all hands
            const dealerCards = state.dealer?.cards || [];
            const dealerShowsAce = dealerCards.length > 0 && isAceRank(dealerCards[0]);
            const allHandsResolvedInsurance = state.hands.every(hand => {
                const isFromSplit = Boolean(hand.isFromSplit || hand.fromSplit);
                if (isFromSplit) return true;
                return hand.insured || hand.insuranceDeclined;
            });

            // Find the first active hand
            const firstActiveHandIndex = state.hands.findIndex(hand => !hand.standing && !hand.busted);
            // Show action buttons for the first active hand after insurance is resolved for all hands
            if (hand.index === firstActiveHandIndex && handPlayable && state.phase === "PLAYER_TURN" && !canInsure && dealerShowsAce && allHandsResolvedInsurance) {
                const recommended = recommendedAction;
                actionsHtml += `<div class="hand-actions">`
                    + (showHit ? `<button data-action="hit" data-hand="${hand.index + 1}"${recommended === "hit" ? ' class="recommended"' : ''}>Hit</button>` : '')
                    + (showStand ? `<button data-action="stand" data-hand="${hand.index + 1}"${recommended === "stand" ? ' class="recommended"' : ''}>Stand</button>` : '')
                    + (showDouble ? `<button data-action="double" data-hand="${hand.index + 1}"${recommended === "double" ? ' class="recommended"' : ''}>Double</button>` : '')
                    + (showSplit ? `<button data-action="split" data-hand="${hand.index + 1}"${recommended === "split" ? ' class="recommended"' : ''}>Split</button>` : '')
                    + `</div>`;
            }

            // For other cases (not dealer ace), show actions as before
            else if (isActive && handPlayable && state.phase === "PLAYER_TURN" && !canInsure && !dealerShowsAce) {
                const recommended = recommendedAction;
                actionsHtml += `<div class="hand-actions">`
                    + (showHit ? `<button data-action="hit" data-hand="${hand.index + 1}"${recommended === "hit" ? ' class="recommended"' : ''}>Hit</button>` : '')
                    + (showStand ? `<button data-action="stand" data-hand="${hand.index + 1}"${recommended === "stand" ? ' class="recommended"' : ''}>Stand</button>` : '')
                    + (showDouble ? `<button data-action="double" data-hand="${hand.index + 1}"${recommended === "double" ? ' class="recommended"' : ''}>Double</button>` : '')
                    + (showSplit ? `<button data-action="split" data-hand="${hand.index + 1}"${recommended === "split" ? ' class="recommended"' : ''}>Split</button>` : '')
                    + `</div>`;
            }

            return `<div class=\"row hand-row${isActive ? " active" : ""}${resultClass}\">\n                    <div class=\"row-title\">Hand ${hand.index + 1}</div>\n                    <div class=\"cards\">${renderCards(hand.cards)}</div>\n                    <div class=\"hand-meta\">\n                        <span class=\"hand-status\">${handStatus}</span>\n                        ${result}\n                        ${doubled}\n                        ${hint}\n                    </div>\n                    ${actionsHtml}\n                </div>`;
        })
        .join("");

    // After rendering hands, auto-open the hit/double/split modal if needed
    if (state.phase === "PLAYER_TURN" && state.hands && state.hands.length > 0) {
        const dealerCards = state.dealer?.cards || [];
        const dealerShowsAce = dealerCards.length > 0 && isAceRank(dealerCards[0]);
        const allHandsResolvedInsurance = state.hands.every(hand => {
            const isFromSplit = Boolean(hand.isFromSplit || hand.fromSplit);
            if (isFromSplit) return true;
            return hand.insured || hand.insuranceDeclined || hand.standing || hand.busted || (hand.cards && hand.cards.length > 2);
        });
        const activeIndex = state.hands.findIndex((hand) => !hand.busted && !hand.standing);
        if (activeIndex >= 0) {
            const hand = state.hands[activeIndex];
            let rawRecommendation = hand.recommendedAction || "";
            let normalizedRecommendation = String(rawRecommendation).toLowerCase().replace(/\s|-+/g, "_");
            // If hand is soft and recommendation is double, change to hit
            if (hand.soft && normalizedRecommendation === "double") {
                normalizedRecommendation = "hit";
            }
            // If dealer shows ace, only auto-open after insurance resolved; otherwise, auto-open immediately
            if ((!dealerShowsAce || allHandsResolvedInsurance)) {
                if (normalizedRecommendation === "hit") {
                    setTimeout(() => showHitCardModal(activeIndex), 250);
                } else if (normalizedRecommendation === "double") {
                    setTimeout(() => showDoubleCardModal(activeIndex), 250);
                } else if (normalizedRecommendation === "split") {
                    setTimeout(() => showSplitCardModal(activeIndex), 250);
                }
            }
        }
    }
    skipAutoModal = false;
    setBusy(false);
    updateButtonStates();
}
// Force update after DOM change
setTimeout(updateButtonStates, 0);

const initializeCardSelectors = () => {
    // Get initial hands value from input
    const initialHands = Number(handsInput?.value || 3);
    // Generate player card selectors
    generatePlayerCardSelectors(initialHands);
    const dealerRank = document.getElementById("dealerCard1Rank");
    if (dealerRank) {
        setTimeout(() => dealerRank.focus(), 0);
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
    // If dealer rank was selected, move focus to first player card rank
    if (e.target.id === 'dealerCard1Rank') {
        const firstPlayerRank = document.getElementById('playerHand1Card1Rank');
        if (firstPlayerRank) {
            setTimeout(() => firstPlayerRank.focus(), 0);
        }
        return;
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
initializeCardSelectors();
ensureCardSelectionVisible();

const startBtn = document.getElementById("startBtn");
if (startBtn) {
    startBtn.addEventListener("click", () => {
        const hands = Number(handsInput.value || 1);
        const decksInput = document.getElementById("decks");
        const decks = decksInput ? Number(decksInput.value || 6) : 6;
        const params = new URLSearchParams();
        params.append('hands', hands);
        params.append('decks', decks);
        const d1r = document.getElementById("dealerCard1Rank").value;
        const d1s = document.getElementById("dealerCard1Suit").value;
        let dealerCard1Rank = '';
        let dealerCard1Suit = '';
        if (d1r && d1s) {
            dealerCard1Rank = d1r;
            dealerCard1Suit = d1s;
        }
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
            // Only add if both rank and suit are present or both are empty
            let card1Valid = (card1Rank && card1Suit) || (!card1Rank && !card1Suit);
            let card2Valid = (card2Rank && card2Suit) || (!card2Rank && !card2Suit);
            if (!card1Valid || !card2Valid) {
                alert(`Hand ${handNum}: Please select both rank and suit for each card, or leave both as Random.`);
                return;
            }
            playerCards.push({
                hand: handNum,
                card1: { rank: card1Rank, suit: card1Suit },
                card2: { rank: card2Rank, suit: card2Suit }
            });
        }
        const allSelectedCards = [];
        if (dealerCard1Rank && dealerCard1Suit) allSelectedCards.push({ rank: dealerCard1Rank, suit: dealerCard1Suit, name: "Dealer Up Card" });
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
        // Decks-aware validation: check if any card is selected more times than allowed
        const cardCount = {};
        allSelectedCards.forEach(card => {
            const key = `${card.rank}:${card.suit}`;
            cardCount[key] = (cardCount[key] || 0) + 1;
        });
        for (const key in cardCount) {
            if (cardCount[key] > decks) {
                alert(`Card ${key.replace(':', ' of ')} selected ${cardCount[key]} times, but only ${decks} available in the shoe.`);
                return;
            }
        }
        if (dealerCard1Rank && dealerCard1Suit) {
            params.append('dealerCard1Rank', dealerCard1Rank);
            params.append('dealerCard1Suit', dealerCard1Suit);
        }
        playerCards.forEach(hand => {
            if (hand.card1.rank && hand.card1.suit) {
                params.append(`playerHand${hand.hand}Card1Rank`, hand.card1.rank);
                params.append(`playerHand${hand.hand}Card1Suit`, hand.card1.suit);
            }
            if (hand.card2.rank && hand.card2.suit) {
                params.append(`playerHand${hand.hand}Card2Rank`, hand.card2.rank);
                params.append(`playerHand${hand.hand}Card2Suit`, hand.card2.suit);
            }
        });
        if (cardsDealtEl) cardsDealtEl.textContent = "-";
        if (strategyTextEl) strategyTextEl.textContent = "-";
        if (noteTextEl) noteTextEl.textContent = "-";
        if (trueCountEl) trueCountEl.textContent = "-";
        const phaseEl = document.getElementById("phase");
        if (phaseEl) phaseEl.textContent = "PLAYER_TURN";

        fallbackGameId = null;
        testModeGameId = null;
        run(async () => {
            const result = await apiFetch(`/api/blackjack/start?${params.toString()}`, { method: "POST" });
            // Force update of strategy panel after start
            renderState(result);

            // Set focus to dealer up card rank select
            const dealerRank = document.getElementById("dealerCard1Rank");
            setTimeout(() => dealerRank.focus(), 0);

            return result;
        });
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
        run(() => {
            const id = guardGameId();
            return apiFetch(`/api/blackjack/stand/${id}?hand=${hand}`, { method: "POST" });
        });
        return;
    }

    // Insurance accept/decline button handling
    if (action === "insure" || action === "insurance") {
        replaceGameStateNext = true;
        run(() => {
            const id = guardGameId();
            return apiFetch(`/api/blackjack/insurance/${id}?hand=${hand}`, { method: "POST" });
        });
        return;
    }
    if (action === "decline_insurance" || action === "decline-insurance") {
        replaceGameStateNext = true;
        run(() => {
            const id = guardGameId();
            return apiFetch(`/api/blackjack/decline-insurance/${id}?hand=${hand}`, { method: "POST" });
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

// Action buttons event listeners
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
        const handIndex = getFirstActiveHandIndex();
        replaceGameStateNext = true;
        run(() => {
            const id = guardGameId();
            return apiFetch(`/api/blackjack/stand/${id}?hand=${handIndex}`, { method: "POST" });
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
let hitModalFallbackTimeout = null;

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
    // Clear any previous fallback timeout to prevent unwanted auto-close
    if (hitModalFallbackTimeout) {
        clearTimeout(hitModalFallbackTimeout);
        hitModalFallbackTimeout = null;
    }
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

    // Show hand value in modal
    const hand = lastState && lastState.hands && lastState.hands[handIndex];
    let valueText = "";
    if (hand) {
        valueText = `Hand Value: <strong>${hand.total}</strong> ${hand.soft ? '(soft)' : ''} ${hand.busted ? '(bust)' : ''}`;
    }
    let valueDiv = hitCardModal.querySelector('.hand-value-info');
    if (!valueDiv) {
        valueDiv = document.createElement('div');
        valueDiv.className = 'hand-value-info';
        valueDiv.style = 'font-size:13px;color:var(--muted);margin-bottom:8px;';
        hitCardModal.querySelector('.modal-content').insertBefore(valueDiv, hitCardModal.querySelector('.modal-content').children[1]);
    }
    valueDiv.innerHTML = valueText;

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
    updateButtonStates();
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

        // Always close modal before async call
        closeHitCardModal();

        replaceGameStateNext = true;
        run(async () => {
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
            const result = await apiFetch(url, { method: "POST" });
            // Force update of strategy panel after hit
            renderState(result);
            return result;
        });
    });
} else {
    console.error("hitCardConfirmBtn not found!");
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

    // Show hand value in modal
    const hand = lastState && lastState.hands && lastState.hands[handIndex];
    let valueText = "";
    if (hand) {
        valueText = `Hand Value: <strong>${hand.total}</strong> ${hand.soft ? '(soft)' : ''} ${hand.busted ? '(bust)' : ''}`;
    }
    let valueDiv = doubleCardModal.querySelector('.hand-value-info');
    if (!valueDiv) {
        valueDiv = document.createElement('div');
        valueDiv.className = 'hand-value-info';
        valueDiv.style = 'font-size:13px;color:var(--muted);margin-bottom:8px;';
        doubleCardModal.querySelector('.modal-content').insertBefore(valueDiv, doubleCardModal.querySelector('.modal-content').children[1]);
    }
    valueDiv.innerHTML = valueText;

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

        // Fallback forcibly close modal logic removed

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
    console.log("SPLIT DIALOG - Cards in play:", cardsInPlay);
    const usedCards = new Set(cardsInPlay.map(c => `${c.rank}:${c.suit}`));
    console.log("SPLIT DIALOG - Used cards set:", Array.from(usedCards));

    // Filter ranks for card 1 (exclude card 2's selection)
    const card2Rank = splitCard2RankSelect?.value;
    const card2Suit = splitCard2SuitSelect?.value;
    if (card2Rank && card2Suit) {
        usedCards.add(`${card2Rank}:${card2Suit}`);
    }

    const availableRanks1 = RANKS.filter(rank => {
        return SUITS.some(suit => !usedCards.has(`${rank.value}:${suit.value}`));
    });
    console.log("SPLIT DIALOG - Available ranks for card 1:", availableRanks1.map(r => r.value));

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

    buildOptions(splitCard1SuitSelect, availableSuits, true, false);

    // Move focus to card 2 rank selector after card 1 rank is selected
    if (splitCard2RankSelect && selectedRank) {
        setTimeout(() => splitCard2RankSelect.focus(), 0);
    }
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

    buildOptions(splitCard2SuitSelect, availableSuits, true, false);
};

const showSplitCardModal = (handIndex) => {
    console.log("showSplitCardModal called with handIndex:", handIndex);
    if (!splitCardModal || !splitCard1RankSelect || !splitCard1SuitSelect || !splitCard2RankSelect || !splitCard2SuitSelect) {
        console.error("Split card modal elements not found!");
        return;
    }
    currentSplitHandIndex = handIndex;

    // Reset dropdown values BEFORE populating to avoid filtering issues
    splitCard1RankSelect.value = "";
    splitCard1SuitSelect.value = "";
    splitCard2RankSelect.value = "";
    splitCard2SuitSelect.value = "";

    populateSplitCardSelectors();

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

        console.log("SPLIT CONFIRM - User selections:", {
            handIndex,
            card1: card1Rank ? `${card1Rank} of ${card1Suit}` : "Random",
            card2: card2Rank ? `${card2Rank} of ${card2Suit}` : "Random"
        });

        // Log the hand being split
        if (lastState && lastState.hands && lastState.hands[handIndex]) {
            const hand = lastState.hands[handIndex];
            if (hand.cards && hand.cards.length === 2) {
                console.log("SPLIT CONFIRM - Original hand cards:", {
                    card1: `${hand.cards[0].rank?.value || hand.cards[0].rank} of ${hand.cards[0].suit?.value || hand.cards[0].suit}`,
                    card2: `${hand.cards[1].rank?.value || hand.cards[1].rank} of ${hand.cards[1].suit?.value || hand.cards[1].suit}`
                });
            }
        }

        console.log("Split confirm - hand:", handIndex, "card1:", card1Rank, card1Suit, "card2:", card2Rank, card2Suit);

        // Validate: cannot select the same card twice
        if (card1Rank && card1Suit && card2Rank && card2Suit) {
            if (card1Rank === card2Rank && card1Suit === card2Suit) {
                alert("Cannot select the same card for both hands");
                return;
            }
        }

        closeSplitCardModal();

        // Fallback: forcibly close modal after 1s if still open
        setTimeout(() => {
            if (splitCardModal && splitCardModal.style.display !== "none") {
                console.warn("[Auto-close fallback] Forcibly closing splitCardModal");
                closeSplitCardModal();
            }
        }, 1000);

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
            console.log("Split params - hand:", handIndex, "card1Rank:", card1Rank, "card1Suit:", card1Suit, "card2Rank:", card2Rank, "card2Suit:", card2Suit);
            return apiFetch(url, { method: "POST" });
        });
    });
}

// Close modals on background click
if (hitCardModal) {
    hitCardModal.addEventListener("click", (e) => {
        if (e.target === hitCardModal) {
            closeHitCardModal();
        }
    });
}

if (doubleCardModal) {
    doubleCardModal.addEventListener("click", (e) => {
        if (e.target === doubleCardModal) {
            closeDoubleCardModal();
        }
    });
}

if (splitCardModal) {
    splitCardModal.addEventListener("click", (e) => {
        if (e.target === splitCardModal) {
            closeSplitCardModal();
        }
    });
}
