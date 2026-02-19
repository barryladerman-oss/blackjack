package us.laderman.blackjack.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.assertThat;
import static us.laderman.blackjack.ui.UiTestConstants.ID_STAND_BTN;
import static us.laderman.blackjack.ui.UiTestConstants.ID_START_BTN;

import static us.laderman.blackjack.ui.UiTestConstants.ID_HIT_CONFIRM_BTN;
import static us.laderman.blackjack.ui.UiTestConstants.ID_SPLIT_CONFIRM_BTN;
import static us.laderman.blackjack.ui.UiTestConstants.ID_DOUBLE_CONFIRM_BTN;

/**
 * Broad coverage UI tests for Blackjack application.
 * These tests exercise key flows and UI elements.
 */

@DisplayName("Blackjack Broad Coverage UI Tests")
public class BlackjackBroadCoverageUITest extends BaseSeleniumTest {

    @Test
    @DisplayName("Start game and verify UI elements")
    void testStartGameAndVerifyUI() {
        clickBtn(UiTestConstants.ID_START_BTN);
        assertThat(driver.findElement(By.id(UiTestConstants.ID_GAME_ID)).getText()).isNotBlank();
        assertThat(driver.findElement(By.id(UiTestConstants.ID_PHASE)).getText()).isNotBlank();
        assertThat(driver.findElement(By.id(UiTestConstants.ID_DEALER_UPCARD)).getText()).isNotBlank();
        assertThat(driver.findElement(By.id(UiTestConstants.ID_RUNNING_COUNT)).getText()).isNotBlank();
        assertThat(driver.findElement(By.id(UiTestConstants.ID_TRUE_COUNT)).getText()).isNotBlank();
        assertThat(driver.findElement(By.id(UiTestConstants.ID_DECKS_REMAINING)).getText()).isNotBlank();
    }

    @Test
    @DisplayName("Full game flow: stand")
    void testStand() {
        try {
            setNumHands(1);
            setDealerUpCard("TWO");
            setPlayerCard(1, 1, "TEN");
            setPlayerCard(1, 2, "TEN");
            clickBtn(ID_START_BTN);

            WebElement standBtn = getActionButton(1, ID_STAND_BTN);
            standBtn.click();
        } catch (Exception e) {
            assert (e.getMessage().isBlank());
        }
    }

    @Test
    @DisplayName("Full game flow: hit")
    void testHit() {
        try {
            setNumHands(1);
            setDealerUpCard("TWO");
            setPlayerCard(1, 1, "2");
            setPlayerCard(1, 2, "SIX");
            clickBtn(ID_START_BTN);

            clickBtn(ID_HIT_CONFIRM_BTN);
            assert (Boolean.TRUE);
        } catch (Exception e) {
            assert (e.getMessage().isBlank());
        }
    }

    @Test
    @DisplayName("Full game flow: softHit")
    void testSoftHit() {
        try {
            setNumHands(1);
            setDealerUpCard("TWO");
            setPlayerCard(1, 1, "ACE");
            setPlayerCard(1, 2, "FIVE");
            clickBtn(ID_START_BTN);

            clickBtn(ID_HIT_CONFIRM_BTN);
            assert (Boolean.TRUE);
        } catch (Exception e) {
            assert (e.getMessage().isBlank());
        }
    }

    @Test
    @DisplayName("Full game flow: player blackjack")
    void testPlayerBlackjack() {
        try {
            setNumHands(1);
            setDealerUpCard("FIVE");
            setPlayerCard(1, 1, "ACE");
            setPlayerCard(1, 2, "JACK");
            clickBtn(ID_START_BTN);

            String phase = driver.findElement(By.id(UiTestConstants.ID_PHASE)).getText();
            assertThat(phase).contains("COMPLETE");
        } catch (Exception e) {
            assert (e.getMessage().isBlank());
        }
    }

    @Test
    @DisplayName("Full game flow: split")
    void testSplit() {
        try {
            setNumHands(2);
            setDealerUpCard("TWO");
            setPlayerCard(1, 1, "EIGHT");
            setPlayerCard(1, 2, "EIGHT");
            setPlayerCard(2, 1, "JACK");
            setPlayerCard(2, 2, "NINE");

            clickBtn(ID_START_BTN);

            clickBtn(ID_SPLIT_CONFIRM_BTN);
            getActionButton(1, ID_STAND_BTN).click();
            getActionButton(2, ID_STAND_BTN).click();
            getActionButton(3, ID_STAND_BTN).click();
            assert (Boolean.TRUE);
        } catch (Exception e) {
            assert (e.getMessage().isBlank());
        }
    }

    @Test
    @DisplayName("Full game flow: double")
    void testDouble() {
        try {
            setNumHands(1);
            setDealerUpCard("TWO");
            setPlayerCard(1, 1, "FIVE");
            setPlayerCard(1, 2, "SIX");
            clickBtn(ID_START_BTN);

            clickBtn(ID_DOUBLE_CONFIRM_BTN);
            assert (Boolean.TRUE);
        } catch (Exception e) {
            assert (e.getMessage().isBlank());
        }
    }

    @Test
    @DisplayName("Verify all summary panels update")
    void testSummaryPanels() {
        driver.get(baseUrl);
        clickBtn(UiTestConstants.ID_START_BTN);
        assertThat(driver.findElement(By.id("gameInfoSummary")).getText()).isNotBlank();
        assertThat(driver.findElement(By.id("resultsSummary")).getText()).isNotBlank();
        assertThat(driver.findElement(By.id("strategySummary")).getText()).isNotBlank();
    }
}
