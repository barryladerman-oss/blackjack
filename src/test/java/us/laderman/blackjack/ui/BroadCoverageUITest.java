package us.laderman.blackjack.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.assertj.core.api.Assertions.assertThat;
import static us.laderman.blackjack.ui.TestConstants.ID_STAND_BTN;
import static us.laderman.blackjack.ui.TestConstants.ID_START_BTN;
import static us.laderman.blackjack.ui.TestConstants.ID_GAME_ID;
import static us.laderman.blackjack.ui.TestConstants.ID_PHASE;
import static us.laderman.blackjack.ui.TestConstants.ID_DEALER_UPCARD;
import static us.laderman.blackjack.ui.TestConstants.ID_RUNNING_COUNT;
import static us.laderman.blackjack.ui.TestConstants.ID_TRUE_COUNT;
import static us.laderman.blackjack.ui.TestConstants.ID_HIT_CONFIRM_BTN;
import static us.laderman.blackjack.ui.TestConstants.ID_HIT_CARD_MODAL;
import static us.laderman.blackjack.ui.TestConstants.ID_SPLIT_CONFIRM_BTN;
import static us.laderman.blackjack.ui.TestConstants.ID_DOUBLE_CONFIRM_BTN;

/**
 * Broad coverage UI tests for Blackjack application.
 * These tests exercise key flows and UI elements.
 */

@DisplayName("Blackjack Broad Coverage UI Tests")
public class BroadCoverageUITest extends BaseSeleniumTest {

    @Test
    @DisplayName("Start game and verify UI elements")
    void testStartGameAndVerifyUI() {
        clickBtn(TestConstants.ID_START_BTN);
        // Wait for game ID to be populated
        wait.until(driver -> {
            String gameId = driver.findElement(By.id(ID_GAME_ID)).getText();
            return gameId != null && !gameId.isEmpty();
        });
        assertThat(driver.findElement(By.id(ID_GAME_ID)).getText()).isNotBlank();
        assertThat(driver.findElement(By.id(ID_PHASE)).getText()).isNotBlank();
        assertThat(driver.findElement(By.id(ID_DEALER_UPCARD)).getText()).isNotBlank();
        // runningCount and trueCount elements should exist
        assertThat(driver.findElement(By.id(ID_RUNNING_COUNT))).isNotNull();
        assertThat(driver.findElement(By.id(ID_TRUE_COUNT))).isNotNull();
        assertThat(driver.findElement(By.id("cardsRemaining")).getText()).isNotBlank();
    }

    @Test
    @DisplayName("Full game flow: testAvailableSuits")
    void testAvailableSuits() {
        setNumHands(1);
        setNumDecks(2);
        setDealerUpCard("TWO", "HEARTS");
        setPlayerCard(1, 1, "TWO", "HEARTS");
        setPlayerCard(1, 2, "TWO");

        String css = "playerHand" + 1 + "Card" + 2;
        WebElement webElement = driver.findElement(By.id(css + "Suit"));
        Select select = new Select(webElement);
        select.getOptions().forEach(option -> {
            String value = option.getAttribute("value");
            assert (!value.toString().equals("HEARTS"));
        });
    }

    @Test
    @DisplayName("Full game flow: stand")
    void testStand() {
        setNumHands(1);
        setDealerUpCard("TWO", "HEARTS");
        setPlayerCard(1, 1, "TEN");
        setPlayerCard(1, 2, "TEN");
        clickBtn(ID_START_BTN);

        clickActionBtn(1, ID_STAND_BTN);

        assert (testComplete());
    }

    @Test
    @DisplayName("Full game flow: hit")
    void testHit() {
        setNumHands(1);
        setDealerUpCard("NINE", "HEARTS");
        setPlayerCard(1, 1, "TWO");
        setPlayerCard(1, 2, "SIX");
        clickBtn(ID_START_BTN);

        clickBtn(ID_HIT_CONFIRM_BTN);
        clickActionBtn(1, ID_STAND_BTN);

        assert (testComplete());
    }

    @Test
    @DisplayName("Full game flow: softHit")
    void testSoftHit() {
        setNumHands(2);
        setDealerUpCard("EIGHT", "HEARTS");
        setPlayerCard(1, 1, "ACE");
        setPlayerCard(1, 2, "FIVE");
        setPlayerCard(2, 1, "ACE");
        setPlayerCard(2, 2, "NINE");
        clickBtn(ID_START_BTN);

        clickBtn(ID_HIT_CONFIRM_BTN);
        // Wait for the modal to close after confirm
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(ID_HIT_CARD_MODAL)));

        clickActionBtn(1, ID_STAND_BTN);
        clickActionBtn(2, ID_STAND_BTN);

        assert (testComplete());
    }

    @Test
    @DisplayName("Full game flow: player blackjack")
    void testPlayerBlackjack() {
        setNumHands(1);
        setDealerUpCard("FIVE", "HEARTS");
        setPlayerCard(1, 1, "ACE");
        setPlayerCard(1, 2, "JACK");
        clickBtn(ID_START_BTN);

        testComplete();
    }

    @Test
    @DisplayName("Full game flow: split")
    void testSplit() {
        setNumHands(2);
        setDealerUpCard("TWO", "HEARTS");
        setPlayerCard(1, 1, "EIGHT");
        setPlayerCard(1, 2, "EIGHT");
        setPlayerCard(2, 1, "JACK");
        setPlayerCard(2, 2, "NINE");
        clickBtn(ID_START_BTN);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("splitCard1Rank")));
        WebElement splitCardRank1 = driver.findElement(By.id("splitCard1Rank"));
        WebElement splitCardRank2 = driver.findElement(By.id("splitCard2Rank"));
        new Select(splitCardRank1).selectByValue("TEN");
        new Select(splitCardRank2).selectByValue("TEN");
        clickBtn(ID_SPLIT_CONFIRM_BTN);

        clickActionBtn(1, ID_STAND_BTN);
        clickActionBtn(2, ID_STAND_BTN);
        clickActionBtn(3, ID_STAND_BTN);

        assert (testComplete());
    }

    @Test
    @DisplayName("Full game flow: double")
    void testDouble() {
        setNumHands(1);
        setDealerUpCard("TWO", "HEARTS");
        setPlayerCard(1, 1, "FIVE");
        setPlayerCard(1, 2, "SIX");
        clickBtn(ID_START_BTN);

        clickBtn(ID_DOUBLE_CONFIRM_BTN);

        // no actions left after doubling, so verify game is complete
        testComplete();
    }

    @Test
    @DisplayName("Verify all summary panels update")
    void testSummaryPanels() {
        driver.get(baseUrl);
        clickBtn(ID_START_BTN);

        assertThat(driver.findElement(By.id("gameInfoSummary")).getText()).isNotBlank();
        assertThat(driver.findElement(By.id("resultsSummary")).getText()).isNotBlank();
        assertThat(driver.findElement(By.id("cardsRemaining")).getText()).isNotBlank();
    }
}
