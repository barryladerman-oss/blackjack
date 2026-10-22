package us.laderman.blackjack.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.model.Rank;
import us.laderman.blackjack.service.BlackjackService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "blackjack.database.mode=h2")
class H2RandomGamePersistenceTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BlackjackService blackjackService;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private HandMapper handMapper;

    @Autowired
    private ResultMapper resultMapper;

    @Test
    void dealsAndPersistsRandomOneDeckThreeHandGameToH2() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("H2");
        }

        int gamesBefore = gameMapper.findAll().size();
        GameState state = blackjackService.startGame(3, new HashMap<>(java.util.Map.of("decks", "1")));
        UUID gameId = UUID.fromString(state.getId());
        state = finishRandomGame(state, gameId);

        assertThat(state.getPhase()).isEqualTo("COMPLETE");
        assertThat(state.getHands()).hasSize(3);

        var games = gameMapper.findAll();
        assertThat(games).hasSize(gamesBefore + 1);
        var savedGame = games.get(games.size() - 1);
        assertThat(savedGame.getNumDecks()).isEqualTo(1);
        var savedHands = handMapper.findByGameId(savedGame.getPk());
        assertThat(savedHands).hasSize(4);
        assertThat(savedHands.stream().filter(hand -> !hand.isDealer())).hasSize(3);
        assertThat(savedHands.stream()
                .filter(hand -> !hand.isDealer())
                .mapToInt(hand -> resultMapper.findByHandId(hand.getPk()).size())
                .sum()).isEqualTo(3);

        int expectedCards = state.getDealer().getCards().size()
                + state.getHands().stream().mapToInt(hand -> hand.getCards().size()).sum();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM card c JOIN hand h ON h.pk = c.handFk WHERE h.gameFk = ?",
                Integer.class,
                savedGame.getPk())).isEqualTo(expectedCards);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_hands WHERE game_id = ?",
                Integer.class,
                savedGame.getPk())).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hand_cards WHERE game_id = ?",
                Integer.class,
                savedGame.getPk())).isEqualTo(expectedCards);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hand_results WHERE game_id = ? AND result_id IS NOT NULL",
                Integer.class,
                savedGame.getPk())).isEqualTo(3);

        System.out.printf(
                "Persisted random H2 one-deck game pk=%d; player results=%s; cards=%d%n",
                savedGame.getPk(),
                state.getHands().stream().map(hand -> hand.getResult()).toList(),
                expectedCards);
    }

    private GameState finishRandomGame(GameState state, UUID gameId) {
        if (state.getDealer() != null
                && !state.getDealer().getCards().isEmpty()
                && state.getDealer().getCards().get(0).getRank() == Rank.ACE) {
            for (int i = 0; i < state.getHands().size() && !"COMPLETE".equals(state.getPhase()); i++) {
                GameState.PlayerHandState hand = state.getHands().get(i);
                if (!hand.isStanding() && !hand.isBusted() && !hand.isInsured() && !hand.isInsuranceDeclined()) {
                    state = blackjackService.declineInsurance(gameId, i);
                }
            }
        }

        for (int i = 0; i < 3 && !"COMPLETE".equals(state.getPhase()); i++) {
            GameState.PlayerHandState hand = state.getHands().get(i);
            if (!hand.isStanding() && !hand.isBusted()) {
                state = blackjackService.stand(gameId, i);
            }
        }
        return state;
    }
}
