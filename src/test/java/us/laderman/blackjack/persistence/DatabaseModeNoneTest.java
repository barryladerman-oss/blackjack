package us.laderman.blackjack.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import us.laderman.blackjack.service.BlackjackService;

@SpringBootTest(properties = "blackjack.database.mode=none")
class DatabaseModeNoneTest {
    @Autowired
    private ObjectProvider<DataSource> dataSources;

    @Autowired
    private BlackjackService blackjackService;

    @Autowired
    private CompletedGamePersistence completedGamePersistence;

    @Test
    void startsWithoutDatabaseAndUsesExplicitNoOpPersistence() {
        assertThat(dataSources.getIfAvailable()).isNull();
        assertThat(completedGamePersistence).isInstanceOf(NoOpCompletedGamePersistence.class);
        assertThat(blackjackService.startGame(1, java.util.Map.of()).getHands()).hasSize(1);
    }
}
