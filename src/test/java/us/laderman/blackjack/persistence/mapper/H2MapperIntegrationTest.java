package us.laderman.blackjack.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import us.laderman.blackjack.persistence.model.CardRecord;
import us.laderman.blackjack.persistence.model.GameRecord;
import us.laderman.blackjack.persistence.model.HandRecord;
import us.laderman.blackjack.persistence.model.ResultRecord;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "blackjack.database.mode=h2")
class H2MapperIntegrationTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private HandMapper handMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ResultMapper resultMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void usesH2AndPersistsRecordsThroughEveryMapper() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("H2");
        }

        GameRecord game = new GameRecord();
        game.setTime(LocalDateTime.now());
        game.setNumDecks(2);
        assertThat(gameMapper.insert(game)).isEqualTo(1);
        Integer gameId = Objects.requireNonNull(game.getPk());
        assertThat(gameMapper.findById(gameId).getNumDecks()).isEqualTo(2);

        HandRecord hand = new HandRecord();
        hand.setGameFk(gameId);
        hand.setInsured(true);
        assertThat(handMapper.insert(hand)).isEqualTo(1);
        Integer handId = Objects.requireNonNull(hand.getPk());
        assertThat(handMapper.findByGameId(gameId)).singleElement()
                .satisfies(savedHand -> assertThat(savedHand.isInsured()).isTrue());

        CardRecord card = new CardRecord();
        card.setHandFk(handId);
        card.setSuit("H");
        card.setValue("A");
        assertThat(cardMapper.insert(card)).isEqualTo(1);
        assertThat(cardMapper.findByHandId(handId)).singleElement()
                .satisfies(savedCard -> assertThat(savedCard.getValue()).isEqualTo("A"));

        ResultRecord result = new ResultRecord();
        result.setHandFk(handId);
        result.setWager((short) 2);
        result.setWon((short) 3);
        assertThat(resultMapper.insert(result)).isEqualTo(1);
        assertThat(resultMapper.findByHandId(handId)).singleElement()
                .satisfies(savedResult -> assertThat(savedResult.getWon()).isEqualTo((short) 3));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hand_cards WHERE hand_id = ? AND card_value = ?",
                Integer.class,
                handId,
                "A")).isEqualTo(1);
        assertThat("game_id".equalsIgnoreCase(jdbcTemplate.queryForMap(
                "SELECT * FROM hand_cards WHERE hand_id = ?",
                handId).keySet().iterator().next())).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_hands WHERE game_id = ? AND hand_id = ?",
                Integer.class,
                gameId,
                handId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hand_results WHERE hand_id = ? AND won = ?",
                Integer.class,
                handId,
                (short) 3)).isEqualTo(1);
        assertThat("game_id".equalsIgnoreCase(jdbcTemplate.queryForMap(
                "SELECT * FROM hand_results WHERE hand_id = ?",
                handId).keySet().iterator().next())).isTrue();

        assertThat(gameMapper.deleteById(gameId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hand WHERE gameFk = ?",
                Integer.class,
                gameId)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM card WHERE handFk = ?",
                Integer.class,
                handId)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM result WHERE handFk = ?",
                Integer.class,
                handId)).isZero();
    }
}
