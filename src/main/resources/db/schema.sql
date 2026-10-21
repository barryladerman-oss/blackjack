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
    CONSTRAINT fk_hand_game FOREIGN KEY (gameFk) REFERENCES game (pk)
);

CREATE TABLE card (
    pk INT NOT NULL AUTO_INCREMENT,
    handFk INT NOT NULL,
    suit CHAR(1) NOT NULL,
    `value` CHAR(1) NOT NULL,
    PRIMARY KEY (pk),
    CONSTRAINT fk_card_hand FOREIGN KEY (handFk) REFERENCES hand (pk)
);

CREATE TABLE result (
    pk INT NOT NULL AUTO_INCREMENT,
    handFk INT NOT NULL,
    wager SMALLINT NOT NULL DEFAULT 1,
    won SMALLINT NOT NULL,
    PRIMARY KEY (pk),
    CONSTRAINT fk_result_hand FOREIGN KEY (handFk) REFERENCES hand (pk)
);
