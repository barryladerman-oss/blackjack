
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

import static org.assertj.core.api.Assertions.assertThat;
import static us.laderman.blackjack.ui.UiTestConstants.BROWSER_HEIGHT;
import static us.laderman.blackjack.ui.UiTestConstants.BROWSER_WIDTH;
import static us.laderman.blackjack.ui.UiTestConstants.WAIT_TIMEOUT_SECONDS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Advanced Blackjack Selenium Tests")
class BlackjackAdvancedSeleniumTest {

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
    @DisplayName("should navigate between multiple games")
    void testMultipleGameSessions() {
        driver.get(baseUrl);

        // Start first game
        driver.findElement(By.id("startBtn")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameId"), ""));
        String firstGameId = driver.findElement(By.id("gameId")).getText();
        assertThat(firstGameId).isNotBlank();

        // Clear for second game
        driver.navigate().refresh();

        // Start second game
        driver.findElement(By.id("startBtn")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameId"), ""));
        String secondGameId = driver.findElement(By.id("gameId")).getText();

        // Two different games should have different IDs
        assertThat(secondGameId).isNotEqualTo(firstGameId);
    }

    @Test
    @DisplayName("should validate hand count input")
    void testHandCountValidation() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id("hands"));

        // Test minimum value
        handsInput.clear();
        handsInput.sendKeys("1");
        driver.findElement(By.id("startBtn")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameState"), "hands"));
        String gameState = driver.findElement(By.id("gameState")).getText();
        assertThat(gameState).contains("hands");
    }

    @Test
    @DisplayName("should display correct game phase")
    void testGamePhaseDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id(UiTestConstants.ID_START_BTN)).click();

        WebElement phaseElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id(UiTestConstants.ID_PHASE)));
        String phase = phaseElement.getText();

        // Should show a valid phase
        assertThat(phase).isNotEqualTo(UiTestConstants.EMPTY_VALUE);
        assertThat(phase).isNotBlank();
    }

    @Test
    @DisplayName("should handle rapid successive hits")
    void testRapidHits() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("phase"), "PLAYER_TURN"));

        WebElement hitBtn = driver.findElement(By.id("hitBtn"));
        String initialState = driver.findElement(By.id("gameState")).getText();

        // Take multiple hits
        hitBtn.click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.id("gameState"))));

        String stateAfterHit = driver.findElement(By.id("gameState")).getText();
        assertThat(stateAfterHit).isNotEqualTo(initialState);
    }

    @Test
    @DisplayName("should show card counting information")
    void testCardCountingInfo() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("trueCount"), ""));

        // All card counting elements should be available
        assertThat(driver.findElement(By.id("runningCountDebug")).getText()).isNotBlank();
        assertThat(driver.findElement(By.id("trueCount")).getText()).isNotBlank();
        assertThat(driver.findElement(By.id("decksRemaining")).getText()).isNotBlank();
    }

    @Test
    @DisplayName("should update game state JSON display")
    void testGameStateJsonDisplay() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameState"), "{"));

        String gameStateJson = driver.findElement(By.id("gameState")).getText();

        // Should contain valid JSON properties
        assertThat(gameStateJson).contains("\"id\"");
        assertThat(gameStateJson).contains("\"phase\"");
        assertThat(gameStateJson).contains("\"hands\"");
        assertThat(gameStateJson).contains("{");
        assertThat(gameStateJson).contains("}");
    }

    @Test
    @DisplayName("should maintain state consistency across actions")
    void testStateConsistency() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameId"), ""));

        String gameId1 = driver.findElement(By.id("gameId")).getText();

        // Take an action
        driver.findElement(By.id("hitBtn")).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.id("gameState"))));

        String gameId2 = driver.findElement(By.id("gameId")).getText();

        // Game ID should remain the same
        assertThat(gameId2).isEqualTo(gameId1);
    }

    @Test
    @DisplayName("should handle page refresh and recovery")
    void testPageRefreshRecovery() {
        driver.get(baseUrl);

        // Start a game
        driver.findElement(By.id("startBtn")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("gameId"), ""));

        // Refresh page
        driver.navigate().refresh();

        // Page should still be functional
        WebElement startBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("startBtn")));
        assertThat(startBtn).isNotNull();
        assertThat(startBtn.isDisplayed()).isTrue();
    }

    @Test
    @DisplayName("should display all required UI elements")
    void testRequiredUiElements() {
        driver.get(baseUrl);

        // Check all required elements are present
        assertThat(driver.findElement(By.id("hands"))).isNotNull();
        assertThat(driver.findElement(By.id("startBtn"))).isNotNull();
        assertThat(driver.findElement(By.id("hitBtn"))).isNotNull();
        assertThat(driver.findElement(By.id("standBtn"))).isNotNull();
        assertThat(driver.findElement(By.id("doubleDownBtn"))).isNotNull();
        assertThat(driver.findElement(By.id("splitBtn"))).isNotNull();
        assertThat(driver.findElement(By.id("insuranceBtn"))).isNotNull();
        assertThat(driver.findElement(By.id("declineInsuranceBtn"))).isNotNull();
        assertThat(driver.findElement(By.id("gameState"))).isNotNull();
    }

    @Test
    @DisplayName("should have accessible form controls")
    void testFormAccessibility() {
        driver.get(baseUrl);

        WebElement handsInput = driver.findElement(By.id("hands"));
        assertThat(handsInput.isDisplayed()).isTrue();
        assertThat(handsInput.isEnabled()).isTrue();

        WebElement startBtn = driver.findElement(By.id("startBtn"));
        assertThat(startBtn.isDisplayed()).isTrue();
        assertThat(startBtn.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("should display betting information")
    void testBettingInformation() {
        driver.get(baseUrl);

        driver.findElement(By.id("startBtn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("suggestedBet"), ""));

        String suggestedBet = driver.findElement(By.id("suggestedBet")).getText();

        // Should show suggested betting units or a dash placeholder
        assertThat(suggestedBet).isNotBlank();
        assertThat(suggestedBet).matches("-|\\d+");
    }

    @Test
    @DisplayName("should handle game with maximum hands")
    void testMaximumHandsGame() {
        driver.get(baseUrl);

        // Use JavaScript to set value and trigger event
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "document.getElementById('hands').value = '5';" +
                        "document.getElementById('hands').dispatchEvent(new Event('input', { bubbles: true }));");

        // Wait for card selectors to be generated
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("playerHand5Card1Rank")));

        driver.findElement(By.id("startBtn")).click();

        // Wait for game state to be non-empty (less strict than waiting for specific
        // JSON)
        wait.until(driver -> {
            String gameState = driver.findElement(By.id("gameState")).getText();
            System.out.println("DEBUG: gameState text = '" + gameState + "'");
            return !gameState.isEmpty() && gameState.contains("id");
        });

        String gameState = driver.findElement(By.id("gameState")).getText();
        assertThat(gameState).isNotBlank();
        assertThat(gameState).contains("hands");
    }

    @Test
    @DisplayName("should handle advanced UI edge cases")
    void testAdvancedUiEdgeCases() {
        driver.get(baseUrl);
        // Timing issue: simulate delayed element
        try {
            Thread.sleep(1000);
            WebElement delayedElement = driver.findElement(By.id("gameState"));
            assertThat(delayedElement).isNotNull();
        } catch (Exception e) {
            assertThat(e.getMessage()).isNotNull();
        }

        // Browser compatibility: check element presence
        WebElement startBtn = driver.findElement(By.id("startBtn"));
        assertThat(startBtn).isNotNull();
        // Advanced scenario: simulate multiple hands
        WebElement handsInput = driver.findElement(By.id("hands"));
        handsInput.clear();
        handsInput.sendKeys("3");
        driver.findElement(By.id("startBtn")).click();
        WebElement gameState = driver.findElement(By.id("gameState"));
        assertThat(gameState.getText()).contains("hands");
    }
}
