CREATE OR REPLACE VIEW hand_results AS
SELECT
    h.gameFk AS game_id,
    h.pk AS hand_id,
    h.isDealer AS is_dealer,
    h.isDoubled AS is_doubled,
    h.isBlackjack AS is_blackjack,
    h.isInsured AS is_insured,
    r.pk AS result_id,
    r.wager AS wager,
    r.won AS won
FROM hand h
LEFT JOIN result r ON r.handFk = h.pk;
