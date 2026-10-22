package us.laderman.blackjack.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfSystemProperty(named = "blackjack.mysql.integration", matches = "true")
class MySqlMapperIntegrationTest {
    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private HandMapper handMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ResultMapper resultMapper;

    @Test
    void readsExistingBlackjackRecordsThroughMappers() {
        var games = gameMapper.findAll();
        var hands = handMapper.findAll();
        var cards = cardMapper.findAll();
        var results = resultMapper.findAll();

        assertThat(games).isNotNull().allSatisfy(game -> {
            assertThat(game.getPk()).isNotNull();
            assertThat(game.getTime()).isNotNull();
            assertThat(game.getNumDecks()).isNotNull();
        });
        assertThat(hands).isNotNull().allSatisfy(hand -> {
            assertThat(hand.getPk()).isNotNull();
            assertThat(hand.getGameFk()).isNotNull();
        });
        assertThat(cards).isNotNull().allSatisfy(card -> {
            assertThat(card.getPk()).isNotNull();
            assertThat(card.getHandFk()).isNotNull();
            assertThat(card.getSuit()).isNotBlank();
            assertThat(card.getValue()).isNotBlank();
        });
        assertThat(results).isNotNull().allSatisfy(result -> {
            assertThat(result.getPk()).isNotNull();
            assertThat(result.getHandFk()).isNotNull();
        });

        Integer gameId = games.isEmpty() ? -1 : Objects.requireNonNull(games.get(0).getPk());
        Integer handId = hands.isEmpty() ? -1 : Objects.requireNonNull(hands.get(0).getPk());
        assertThat(handMapper.findByGameId(gameId)).isNotNull();
        assertThat(cardMapper.findByHandId(handId)).isNotNull();
        assertThat(resultMapper.findByHandId(handId)).isNotNull();
    }
}
