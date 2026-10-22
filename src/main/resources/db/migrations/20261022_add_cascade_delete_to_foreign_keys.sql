ALTER TABLE hand
    DROP FOREIGN KEY fk_hand_game;
ALTER TABLE hand
    DROP INDEX fk_hand_game;
ALTER TABLE hand
    ADD CONSTRAINT fk_hand_game_cascade
        FOREIGN KEY (gameFk) REFERENCES game (pk) ON DELETE CASCADE;

ALTER TABLE card
    DROP FOREIGN KEY fk_card_hand;
ALTER TABLE card
    DROP INDEX fk_card_hand;
ALTER TABLE card
    ADD CONSTRAINT fk_card_hand_cascade
        FOREIGN KEY (handFk) REFERENCES hand (pk) ON DELETE CASCADE;

ALTER TABLE result
    DROP FOREIGN KEY fk_result_hand;
ALTER TABLE result
    DROP INDEX fk_result_hand;
ALTER TABLE result
    ADD CONSTRAINT fk_result_hand_cascade
        FOREIGN KEY (handFk) REFERENCES hand (pk) ON DELETE CASCADE;
