package us.laderman.blackjack.ui;

import static us.laderman.blackjack.ui.UiTestConstants.BROWSER_WIDTH;
import static us.laderman.blackjack.ui.UiTestConstants.BROWSER_HEIGHT;
import static us.laderman.blackjack.ui.UiTestConstants.BASE_URL;
import static us.laderman.blackjack.ui.UiTestConstants.WAIT_TIMEOUT_SECONDS;
import static us.laderman.blackjack.ui.UiTestConstants.ID_START_BTN;
import static us.laderman.blackjack.ui.UiTestConstants.ID_HIT_BTN;
import static us.laderman.blackjack.ui.UiTestConstants.ID_GAME_STATE;
import static us.laderman.blackjack.ui.UiTestConstants.ID_HANDS;
import static us.laderman.blackjack.ui.UiTestConstants.ID_PHASE;
import static us.laderman.blackjack.ui.UiTestConstants.PHASE_PLAYER_TURN;
import static us.laderman.blackjack.ui.UiTestConstants.JSON_FIELD_CARDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class BaseSeleniumTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    @Autowired
    protected Environment environment;
    static {
        suppressWebDriverManagerWarnings();
    }

    @BeforeEach
    public void setUpDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new");
        options.addArguments(String.format("--window-size=%d,%d", BROWSER_WIDTH, BROWSER_HEIGHT));
        driver = new ChromeDriver(options);

        baseUrl = BASE_URL + ":" + environment.getProperty("local.server.port");
        driver.get(baseUrl);
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
    }

    @AfterEach
    public void tearDownDriver() {
        if (driver != null) {
            try {
                driver.close();
            } catch (Exception ignored) {
            }
            try {
                driver.quit();
            } catch (Exception ignored) {
            }
            driver = null;
        }
    }

    @BeforeAll
    public static void suppressWebDriverManagerWarnings() {
        // Suppress WebDriverManager warnings and info logs
        System.setProperty("wdm.log.level", "ERROR");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        java.util.logging.Logger.getLogger("io.github.bonigarcia.wdm").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("io.github.bonigarcia.wdm.WebDriverManager")
                .setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("io.github.bonigarcia.wdm.config.Config")
                .setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("io.github.bonigarcia.wdm.cache.Downloader")
                .setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("io.github.bonigarcia.wdm.cache.CacheHandler")
                .setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("io.github.bonigarcia.wdm.webdriver.WebDriverBrowser")
                .setLevel(java.util.logging.Level.SEVERE);
        // Suppress Selenium and WebSocket warnings (e.g., 36692)
        java.util.logging.Logger.getLogger("org.openqa.selenium.remote.http.WebSocket")
                .setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder")
                .setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("org.openqa.selenium.chromium.ChromiumDriver")
                .setLevel(java.util.logging.Level.SEVERE);
        // Try to suppress via SLF4J and Log4j if available
        try {
            Class<?> slf4jLogger = Class.forName("org.slf4j.LoggerFactory");
            Object logger = slf4jLogger.getMethod("getLogger", String.class).invoke(null, "io.github.bonigarcia.wdm");
            logger.getClass().getMethod("setLevel", String.class).invoke(logger, "ERROR");
        } catch (Exception ignored) {
        }
        try {
            Class<?> logManager = Class.forName("org.apache.logging.log4j.LogManager");
            Object logger = logManager.getMethod("getLogger", String.class).invoke(null, "io.github.bonigarcia.wdm");
            logger.getClass().getMethod("setLevel", Class.forName("org.apache.logging.log4j.Level")).invoke(logger,
                    Class.forName("org.apache.logging.log4j.Level").getField("ERROR").get(null));
        } catch (Exception ignored) {
        }
    }

    protected void closeModalIfOpen() {
        java.util.function.Consumer<String> closeModalById = (modalId) -> {
            WebElement modal = driver.findElement(By.id(modalId));
            if (!modal.isDisplayed())
                return;

            WebElement cancelBtn = modal.findElement(By.cssSelector(
                    "button[id*='cancel'],button[class*='cancel'],button[class*='close'],button[id*='close']"));
            cancelBtn.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(modalId)));
        };
        closeModalById.accept(UiTestConstants.ID_HIT_CARD_MODAL);
        closeModalById.accept(UiTestConstants.ID_SPLIT_CARD_MODAL);
        closeModalById.accept(UiTestConstants.ID_DOUBLE_CARD_MODAL);
    }

    void testRunningCountAfterEachHit() {
        driver.findElement(By.id(ID_START_BTN)).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id(ID_PHASE), PHASE_PLAYER_TURN));
        WebElement gameStateDiv = driver.findElement(By.id(ID_GAME_STATE));
        String initialState = gameStateDiv.getText();

        clickBtn(ID_HIT_BTN);

        wait.until(ExpectedConditions.stalenessOf(gameStateDiv));
        WebElement updatedGameState = driver.findElement(By.id(ID_GAME_STATE));
        String updatedState = updatedGameState.getText();
        assertThat(updatedState).isNotEqualTo(initialState);
        assertThat(updatedState).contains(JSON_FIELD_CARDS);
    }

    WebElement getActionButton(int handIndex, String btnAction) {
        String cssHand = "[data-hand='" + handIndex + "']";
        String cssAction = "[data-action='" + btnAction + "']";

        WebElement webElement = driver.findElement(By.cssSelector(cssHand + cssAction));
        return webElement;
    }

    void clickBtn(String buttonID) {
        // Always wait for all modals to be closed before clicking
        waitForNoModalsOpen();

        WebElement btn = driver.findElement(By.id(buttonID));
        btn.click();
    }

    void clickModalBtn(String buttonID) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(buttonID)));

        driver.findElement(By.id(buttonID)).click();
        waitForNoModalsOpen();
    }

    // Wait for all modals to be closed
    protected void waitForNoModalsOpen() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(UiTestConstants.ID_HIT_CARD_MODAL)));
        } catch (Exception ignored) {
        }
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(UiTestConstants.ID_SPLIT_CARD_MODAL)));
        } catch (Exception ignored) {
        }
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(UiTestConstants.ID_DOUBLE_CARD_MODAL)));
        } catch (Exception ignored) {
        }
    }

    void setNumHands(int numHands) {
        try {
            WebElement webElement = driver.findElement(By.id(ID_HANDS));
            webElement.clear();
            webElement.sendKeys("" + numHands);
        } catch (Exception e) {
            assert (e.getMessage().isBlank());
        }
    }

    void setDealerUpCard(String rank) {
        String css = "[data-card='dealer1']";
        try {
            WebElement webElement = driver.findElement(By.cssSelector(css));
            Select select = new Select(webElement);
            select.selectByValue(rank);
        } catch (Exception e) {
            assert (e.getMessage().isBlank());
        }
    }

    void setPlayerCard(int handIndex, int cardIndex, String rank) {
        String css = "[data-card='player" + handIndex + "-" + cardIndex + "']";
        try {
            WebElement webElement = driver.findElement(By.cssSelector(css));
            Select select = new Select(webElement);
            select.selectByValue(rank);
        } catch (Exception e) {

        }
    }
}