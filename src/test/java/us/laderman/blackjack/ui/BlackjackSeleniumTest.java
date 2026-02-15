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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

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
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
        assertThat(driver.getTitle()).contains("Blackjack");

        WebElement startBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("startBtn")));
        assertThat(startBtn).isNotNull();
        assertThat(startBtn.isDisplayed()).isTrue();
    }

    @Test
    @DisplayName("should start a new game with default hands")
    void testStartGame() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id("hands"));
        assertThat(handsInput.getAttribute("value")).isEqualTo("3");

        WebElement startBtn = driver.findElement(By.id("startBtn"));
        startBtn.click();

        // Wait for game state to appear
        WebElement gameState = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("gameState")));
        String state = gameState.getText();
        assertThat(state).contains("id");
        assertThat(state).contains("hands");
    }

    @Test
    @DisplayName("should display game information after starting")
    void testGameInfoDisplay() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id("hands"));
        handsInput.clear();
        handsInput.sendKeys("1");

        driver.findElement(By.id("startBtn")).click();

        // Wait for game state to be populated
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameState"), "id"));

        // Verify game state displays
        String phase = driver.findElement(By.id("phase")).getText();
        assertThat(phase).isNotBlank();
        assertThat(phase).isNotEqualTo("-");

        String runningCount = driver.findElement(By.id("runningCount")).getText();
        assertThat(runningCount).isNotBlank();
    }

    @Test
    @DisplayName("should enable action buttons during player turn")
    void testActionButtonsEnabled() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        // Wait for game state
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("phase"), "PLAYER_TURN"));

        WebElement hitBtn = driver.findElement(By.id("hitBtn"));
        WebElement standBtn = driver.findElement(By.id("standBtn"));

        // During PLAYER_TURN, these buttons should be enabled
        assertThat(hitBtn.isEnabled()).isTrue();
        assertThat(standBtn.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("should execute hit action")
    void testHitAction() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("phase"), "PLAYER_TURN"));

        WebElement gameStateDiv = driver.findElement(By.id("gameState"));
        String initialState = gameStateDiv.getText();

        // Click hit button
        driver.findElement(By.id("hitBtn")).click();

        // Wait for state to change
        wait.until(ExpectedConditions.stalenessOf(gameStateDiv));
        WebElement updatedGameState = driver.findElement(By.id("gameState"));
        String updatedState = updatedGameState.getText();

        assertThat(updatedState).isNotEqualTo(initialState);
        assertThat(updatedState).contains("cards");
    }

    @Test
    @DisplayName("should execute stand action")
    void testStandAction() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("phase"), "PLAYER_TURN"));

        // Click stand button
        driver.findElement(By.id("standBtn")).click();

        // Wait for state to change
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//*[contains(text(), 'COMPLETE') or contains(text(), 'DEALER_TURN')]"),
                ""));

        String gameState = driver.findElement(By.id("gameState")).getText();
        assertThat(gameState).contains("phase");
    }

    @Test
    @DisplayName("should handle multiple hands")
    void testMultipleHands() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id("hands"));
        handsInput.clear();
        handsInput.sendKeys("3");

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameState"), "hands"));

        String gameState = driver.findElement(By.id("gameState")).getText();
        // Verify multiple hands are present
        assertThat(gameState).contains("\"hands\"");
        assertThat(gameState).contains("index");
    }

    @Test
    @DisplayName("should update true count display")
    void testTrueCountDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("trueCount"), "-"));

        WebElement trueCountElement = driver.findElement(By.id("trueCount"));
        String trueCount = trueCountElement.getText();

        // True count should be a number or initial "-"
        assertThat(trueCount).matches("-|\\d+\\.\\d+|\\d+");
    }

    @Test
    @DisplayName("should display suggested bet units")
    void testSuggestedBetDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("suggestedBet"), "-"));

        WebElement suggestedBetElement = driver.findElement(By.id("suggestedBet"));
        String suggestedBet = suggestedBetElement.getText();

        // Should be a number or dash
        assertThat(suggestedBet).matches("-|\\d+");
    }

    @Test
    @DisplayName("should display dealer upcard")
    void testDealerUpcardDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("dealerUpcard"), "-"));

        WebElement dealerUpcardElement = driver.findElement(By.id("dealerUpcard"));
        String dealerUpcard = dealerUpcardElement.getText();

        // Dealer should have at least one card displayed
        assertThat(dealerUpcard).isNotBlank();
    }

    @Test
    @DisplayName("should show game ID after starting")
    void testGameIdDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameId"), ""));

        WebElement gameIdElement = driver.findElement(By.id("gameId"));
        String gameId = gameIdElement.getText();

        // Game ID should be a UUID format or non-empty
        assertThat(gameId).isNotBlank();
        assertThat(gameId).isNotEqualTo("-");
    }

    @Test
    @DisplayName("should support full game flow: start, hit, stand")
    void testFullGameFlow() {
        driver.get(baseUrl);

        // Start game
        driver.findElement(By.id("startBtn")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("phase"), "PLAYER_TURN"));

        // Take one hit
        driver.findElement(By.id("hitBtn")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameState"), ""));

        // Stand
        driver.findElement(By.id("standBtn")).click();

        // Wait for game to complete or advance phase
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("gameState")));

        String finalState = driver.findElement(By.id("gameState")).getText();
        assertThat(finalState).contains("phase");
        assertThat(finalState).isNotBlank();
    }

    @Test
    @DisplayName("should handle single hand selection")
    void testSingleHandGame() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id("hands"));
        handsInput.clear();
        handsInput.sendKeys("1");

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameState"), "hands"));

        String gameState = driver.findElement(By.id("gameState")).getText();
        // Verify game started with correct hand count
        assertThat(gameState).contains("hands");
    }

    @Test
    @DisplayName("should display running count")
    void testRunningCountDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("runningCount"), ""));

        WebElement runningCountElement = driver.findElement(By.id("runningCount"));
        String runningCount = runningCountElement.getText();

        // Running count should be a number
        assertThat(runningCount).matches("-|\\d+");
    }

    @Test
    @DisplayName("should display decks remaining")
    void testDecksRemainingDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("decksRemaining"), "-"));

        WebElement decksRemainingElement = driver.findElement(By.id("decksRemaining"));
        String decksRemaining = decksRemainingElement.getText();

        // Decks remaining should be a decimal number or dash
        assertThat(decksRemaining).matches("-|\\d+\\.\\d+|\\d+");
    }
}
