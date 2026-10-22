CREATE TABLE game (
    pk INT NOT NULL AUTO_INCREMENT,
    time DATETIME NOT NULL,
    num_decks INT NOT NULL,
    PRIMARY KEY (pk)
);

CREATE TABLE hand (
    pk INT NOT NULL AUTO_INCREMENT,
    gameFk INT NOT NULL,
    isDealer BOOLEAN NOT NULL,
    isDoubled BOOLEAN NOT NULL,
    isBlackjack BOOLEAN NOT NULL,
    isInsured BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (pk),
    CONSTRAINT fk_hand_game_cascade FOREIGN KEY (gameFk) REFERENCES game (pk) ON DELETE CASCADE
);

CREATE TABLE card (
    pk INT NOT NULL AUTO_INCREMENT,
    handFk INT NOT NULL,
    suit CHAR(1) NOT NULL,
    `value` CHAR(1) NOT NULL,
    PRIMARY KEY (pk),
    CONSTRAINT fk_card_hand_cascade FOREIGN KEY (handFk) REFERENCES hand (pk) ON DELETE CASCADE
);

CREATE TABLE result (
    pk INT NOT NULL AUTO_INCREMENT,
    handFk INT NOT NULL,
    wager SMALLINT NOT NULL DEFAULT 1,
    won SMALLINT NOT NULL,
    PRIMARY KEY (pk),
    CONSTRAINT fk_result_hand_cascade FOREIGN KEY (handFk) REFERENCES hand (pk) ON DELETE CASCADE
);

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

CREATE OR REPLACE VIEW game_hands AS
SELECT
    g.pk AS game_id,
    g.time AS game_time,
    g.num_decks AS num_decks,
    h.pk AS hand_id,
    h.isDealer AS is_dealer,
    h.isDoubled AS is_doubled,
    h.isBlackjack AS is_blackjack,
    h.isInsured AS is_insured
FROM game g
LEFT JOIN hand h ON h.gameFk = g.pk;

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
