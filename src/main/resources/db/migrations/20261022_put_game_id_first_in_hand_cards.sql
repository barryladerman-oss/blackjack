CREATE OR REPLACE VIEW hand_cards AS
SELECT
    h.gameFk AS game_id,
    h.pk AS hand_id,
    h.isDealer AS is_dealer,
    h.isDoubled AS is_doubled,
    h.isBlackjack AS is_blackjack,
    h.isInsured AS is_insured,
    c.pk AS card_id,
    c.suit AS card_suit,
    c.`value` AS card_value
FROM hand h
LEFT JOIN card c ON c.handFk = h.pk;
