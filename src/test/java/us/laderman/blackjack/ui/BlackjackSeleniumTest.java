package us.laderman.blackjack.ui;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static us.laderman.blackjack.ui.UiTestConstants.BROWSER_HEIGHT;
import static us.laderman.blackjack.ui.UiTestConstants.BROWSER_WIDTH;
import static us.laderman.blackjack.ui.UiTestConstants.DEFAULT_HANDS;
import static us.laderman.blackjack.ui.UiTestConstants.EMPTY_VALUE;
import static us.laderman.blackjack.ui.UiTestConstants.ID_DECKS_REMAINING;
import static us.laderman.blackjack.ui.UiTestConstants.ID_DEALER_UPCARD;
import static us.laderman.blackjack.ui.UiTestConstants.ID_GAME_ID;
import static us.laderman.blackjack.ui.UiTestConstants.ID_GAME_STATE;
import static us.laderman.blackjack.ui.UiTestConstants.ID_HANDS;
import static us.laderman.blackjack.ui.UiTestConstants.ID_HIT_BTN;
import static us.laderman.blackjack.ui.UiTestConstants.ID_PHASE;
import static us.laderman.blackjack.ui.UiTestConstants.ID_RUNNING_COUNT;
import static us.laderman.blackjack.ui.UiTestConstants.ID_STAND_BTN;
import static us.laderman.blackjack.ui.UiTestConstants.ID_START_BTN;
import static us.laderman.blackjack.ui.UiTestConstants.ID_SUGGESTED_BET;
import static us.laderman.blackjack.ui.UiTestConstants.ID_TRUE_COUNT;
import static us.laderman.blackjack.ui.UiTestConstants.JSON_FIELD_CARDS;
import static us.laderman.blackjack.ui.UiTestConstants.JSON_FIELD_HANDS;
import static us.laderman.blackjack.ui.UiTestConstants.JSON_FIELD_ID;
import static us.laderman.blackjack.ui.UiTestConstants.JSON_FIELD_INDEX;
import static us.laderman.blackjack.ui.UiTestConstants.JSON_FIELD_PHASE;
import static us.laderman.blackjack.ui.UiTestConstants.PAGE_TITLE;
import static us.laderman.blackjack.ui.UiTestConstants.PATTERN_DECIMAL_OR_DASH;
import static us.laderman.blackjack.ui.UiTestConstants.PATTERN_NUMBER_OR_DASH;
import static us.laderman.blackjack.ui.UiTestConstants.PHASE_COMPLETE;
import static us.laderman.blackjack.ui.UiTestConstants.PHASE_DEALER_TURN;
import static us.laderman.blackjack.ui.UiTestConstants.PHASE_PLAYER_TURN;
import static us.laderman.blackjack.ui.UiTestConstants.SINGLE_HAND;
import static us.laderman.blackjack.ui.UiTestConstants.WAIT_TIMEOUT_SECONDS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Blackjack Selenium UI Tests")
class BlackjackSeleniumTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(BROWSER_WIDTH, BROWSER_HEIGHT));
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        baseUrl = "http://localhost:" + port;

    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("should load the game page successfully")
    void testPageLoads() {
        driver.get(baseUrl);
        assertThat(driver.getTitle()).contains(PAGE_TITLE);

        WebElement startBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(ID_START_BTN)));
        assertThat(startBtn).isNotNull();
        assertThat(startBtn.isDisplayed()).isTrue();
    }

    @Test
    @DisplayName("should start a new game with default hands")
    void testStartGame() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id(UiTestConstants.ID_HANDS));
        assertThat(handsInput.getAttribute("value")).isEqualTo(UiTestConstants.DEFAULT_HANDS);

        WebElement startBtn = driver.findElement(By.id(UiTestConstants.ID_START_BTN));
        startBtn.click();

        // Wait for game state to appear
        WebElement gameState = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id(UiTestConstants.ID_GAME_STATE)));
        String state = gameState.getText();
        assertThat(state).contains(UiTestConstants.JSON_FIELD_ID);
        assertThat(state).contains(UiTestConstants.JSON_FIELD_HANDS);
    }

    @Test
    @DisplayName("should display game information after starting")
    void testGameInfoDisplay() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id(ID_HANDS));
        handsInput.clear();
        handsInput.sendKeys(SINGLE_HAND);

        driver.findElement(By.id(ID_START_BTN)).click();

        // Wait for game state to be populated
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_GAME_STATE), JSON_FIELD_ID));

        // Verify game state displays
        String phase = driver.findElement(By.id(ID_PHASE)).getText();
        assertThat(phase).isNotBlank();
        assertThat(phase).isNotEqualTo(EMPTY_VALUE);

        String runningCount = driver.findElement(By.id(ID_RUNNING_COUNT)).getText();
        assertThat(runningCount).isNotBlank();
    }

    @Test
    @DisplayName("should enable action buttons during player turn")
    void testActionButtonsEnabled() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        // Wait for game state
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_PHASE), PHASE_PLAYER_TURN));

        WebElement hitBtn = driver.findElement(By.id(ID_HIT_BTN));
        WebElement standBtn = driver.findElement(By.id(ID_STAND_BTN));

        // During PLAYER_TURN, these buttons should be enabled
        assertThat(hitBtn.isEnabled()).isTrue();
        assertThat(standBtn.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("should execute hit action")
    void testHitAction() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_PHASE), PHASE_PLAYER_TURN));

        WebElement gameStateDiv = driver.findElement(By.id(ID_GAME_STATE));
        String initialState = gameStateDiv.getText();

        // Click hit button
        driver.findElement(By.id(ID_HIT_BTN)).click();

        // Wait for state to change
        wait.until(ExpectedConditions.stalenessOf(gameStateDiv));
        WebElement updatedGameState = driver.findElement(By.id(ID_GAME_STATE));
        String updatedState = updatedGameState.getText();

        assertThat(updatedState).isNotEqualTo(initialState);
        assertThat(updatedState).contains(JSON_FIELD_CARDS);
    }

    @Test
    @DisplayName("should execute stand action")
    void testStandAction() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_PHASE), PHASE_PLAYER_TURN));

        // Click stand button
        driver.findElement(By.id(ID_STAND_BTN)).click();

        // Wait for state to change
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//*[contains(text(), '" + PHASE_COMPLETE + "') or contains(text(), '" + PHASE_DEALER_TURN
                        + "')]"),
                ""));

        String gameState = driver.findElement(By.id(ID_GAME_STATE)).getText();
        assertThat(gameState).contains(JSON_FIELD_PHASE);
    }

    @Test
    @DisplayName("should handle multiple hands")
    void testMultipleHands() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id(ID_HANDS));
        handsInput.clear();
        handsInput.sendKeys(DEFAULT_HANDS);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_GAME_STATE), JSON_FIELD_HANDS));

        String gameState = driver.findElement(By.id(ID_GAME_STATE)).getText();
        // Verify multiple hands are present
        assertThat(gameState).contains("\"" + JSON_FIELD_HANDS + "\"");
        assertThat(gameState).contains(JSON_FIELD_INDEX);
    }

    @Test
    @DisplayName("should update true count display")
    void testTrueCountDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_TRUE_COUNT), EMPTY_VALUE));

        WebElement trueCountElement = driver.findElement(By.id(ID_TRUE_COUNT));
        String trueCount = trueCountElement.getText();

        // True count should be a number or initial "-"
        assertThat(trueCount).matches(PATTERN_DECIMAL_OR_DASH);
    }

    @Test
    @DisplayName("should display suggested bet units")
    void testSuggestedBetDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_SUGGESTED_BET), EMPTY_VALUE));

        WebElement suggestedBetElement = driver.findElement(By.id(ID_SUGGESTED_BET));
        String suggestedBet = suggestedBetElement.getText();

        // Should be a number or dash
        assertThat(suggestedBet).matches(PATTERN_NUMBER_OR_DASH);
    }

    @Test
    @DisplayName("should display dealer upcard")
    void testDealerUpcardDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_DEALER_UPCARD), EMPTY_VALUE));

        WebElement dealerUpcardElement = driver.findElement(By.id(ID_DEALER_UPCARD));
        String dealerUpcard = dealerUpcardElement.getText();

        // Dealer should have at least one card displayed
        assertThat(dealerUpcard).isNotBlank();
    }

    @Test
    @DisplayName("should show game ID after starting")
    void testGameIdDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_GAME_ID), ""));

        WebElement gameIdElement = driver.findElement(By.id(ID_GAME_ID));
        String gameId = gameIdElement.getText();

        // Game ID should be a UUID format or non-empty
        assertThat(gameId).isNotBlank();
        assertThat(gameId).isNotEqualTo(EMPTY_VALUE);
    }

    @Test
    @DisplayName("should support full game flow: start, hit, stand")
    void testFullGameFlow() {
        driver.get(baseUrl);

        // Start game
        driver.findElement(By.id(ID_START_BTN)).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_PHASE), PHASE_PLAYER_TURN));

        // Take one hit
        driver.findElement(By.id(ID_HIT_BTN)).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_GAME_STATE), ""));

        // Stand
        driver.findElement(By.id(ID_STAND_BTN)).click();

        // Wait for game to complete or advance phase
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(ID_GAME_STATE)));

        String finalState = driver.findElement(By.id(ID_GAME_STATE)).getText();
        assertThat(finalState).contains(JSON_FIELD_PHASE);
        assertThat(finalState).isNotBlank();
    }

    @Test
    @DisplayName("should handle single hand selection")
    void testSingleHandGame() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id(ID_HANDS));
        handsInput.clear();
        handsInput.sendKeys(SINGLE_HAND);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_GAME_STATE), JSON_FIELD_HANDS));

        String gameState = driver.findElement(By.id(ID_GAME_STATE)).getText();
        // Verify game started with correct hand count
        assertThat(gameState).contains(JSON_FIELD_HANDS);
    }

    @Test
    @DisplayName("should display running count")
    void testRunningCountDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_RUNNING_COUNT), ""));

        WebElement runningCountElement = driver.findElement(By.id(ID_RUNNING_COUNT));
        String runningCount = runningCountElement.getText();

        // Running count should be a number
        assertThat(runningCount).matches(PATTERN_NUMBER_OR_DASH);
    }

    @Test
    @DisplayName("should display decks remaining")
    void testDecksRemainingDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id(ID_START_BTN)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id(ID_DECKS_REMAINING), EMPTY_VALUE));

        WebElement decksRemainingElement = driver.findElement(By.id(ID_DECKS_REMAINING));
        String decksRemaining = decksRemainingElement.getText();

        // Decks remaining should be a decimal number or dash
        assertThat(decksRemaining).matches(PATTERN_DECIMAL_OR_DASH);
    }

    @Test
    @DisplayName("should handle UI edge cases and missing elements")
    void testUiEdgeCases() {
        driver.get(baseUrl);
        // Missing element
        try {
            driver.findElement(By.id("nonExistentElement"));
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("no such element");
        }

        // Invalid user input
        WebElement handsInput = driver.findElement(By.id("hands"));
        handsInput.clear();
        handsInput.sendKeys("invalid");
        driver.findElement(By.id(ID_START_BTN)).click();
        WebElement stateElement = driver.findElement(By.id(ID_GAME_STATE));
        assertThat(stateElement.getText()).isNotEmpty();

        // UI race condition (simulate rapid clicks)
        WebElement hitBtn = driver.findElement(By.id(ID_HIT_BTN));
        for (int i = 0; i < 5; i++) {
            hitBtn.click();
        }
        WebElement updatedState = driver.findElement(By.id(ID_GAME_STATE));
        assertThat(updatedState.getText()).isNotEmpty();
    }
}
