package us.laderman.blackjack;

import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.ui.BaseSeleniumTest;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BlackjackSmokeTest extends BaseSeleniumTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void startGameReturnsState() {
        ResponseEntity<GameState> response = restTemplate.postForEntity(
                "/api/blackjack/start?hands=1",
                null,
                GameState.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        GameState state = Objects.requireNonNull(response.getBody(), "state");
        assertThat(state.getHands()).hasSize(1);
        assertThat(state.getId()).isNotBlank();
    }
}
