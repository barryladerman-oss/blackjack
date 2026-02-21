package us.laderman.blackjack.ui;

import static us.laderman.blackjack.ui.TestConstants.BROWSER_WIDTH;
import static us.laderman.blackjack.ui.TestConstants.BROWSER_HEIGHT;
import static us.laderman.blackjack.ui.TestConstants.BASE_URL;
import static us.laderman.blackjack.ui.TestConstants.WAIT_TIMEOUT_SECONDS;
import static us.laderman.blackjack.ui.TestConstants.ID_PHASE;
import static us.laderman.blackjack.ui.TestConstants.ID_HANDS;
import static us.laderman.blackjack.ui.TestConstants.ID_DECKS;
import static us.laderman.blackjack.ui.TestConstants.ID_HIT_CARD_MODAL;
import static us.laderman.blackjack.ui.TestConstants.ID_SPLIT_CARD_MODAL;
import static us.laderman.blackjack.ui.TestConstants.ID_DOUBLE_CARD_MODAL;
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
        // Add test mode URL parameter for UI tests
        String testModeUrl = baseUrl + "?test=true";
        driver.get(testModeUrl);
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
        closeModalById.accept(ID_HIT_CARD_MODAL);
        closeModalById.accept(ID_SPLIT_CARD_MODAL);
        closeModalById.accept(ID_DOUBLE_CARD_MODAL);
    }

    public void testComplete() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        WebElement phase = driver.findElement(By.id(ID_PHASE));
        if (phase.getText().contains("COMPLETE")) {
            return;
        }

        wait.until(ExpectedConditions.stalenessOf(phase));
        assertThat(phase.getText()).contains("COMPLETE");
    }

    void clickActionBtn(int handIndex, String btnAction) {
        closeModalIfOpen();

        String cssHand = "[data-hand='" + handIndex + "']";
        String cssAction = "[data-action='" + btnAction + "']";

        WebElement webElement = driver.findElement(By.cssSelector(cssHand + cssAction));
        wait.until(ExpectedConditions.elementToBeClickable(webElement));
        webElement.click();
    }

    void clickBtn(String buttonID) {
        WebElement webElement = driver.findElement(By.id(buttonID));

        wait.until(ExpectedConditions.elementToBeClickable(webElement));
        webElement.click();
    }

    void clickModalBtn(String buttonID) {
        WebElement webElement = driver.findElement(By.id(buttonID));

        wait.until(ExpectedConditions.elementToBeClickable(webElement));
        webElement.click();
    }

    void setNumHands(int numHands) {
        WebElement webElement = driver.findElement(By.id(ID_HANDS));
        webElement.clear();
        webElement.sendKeys(Integer.toString(numHands));
    }

    void setNumDecks(int numDecks) {
        WebElement webElement = driver.findElement(By.id(ID_DECKS));
        webElement.clear();
        webElement.sendKeys(Integer.toString(numDecks));
    }

    void setDealerUpCard(String rank) {
        setDealerUpCard(rank, null);
    }

    void setDealerUpCard(String rank, String suit) {
        String css = "dealerCard1";
        if (rank != null) {
            WebElement webElement = driver.findElement(By.id(css + "Rank"));
            Select select = new Select(webElement);
            select.selectByValue(rank);
        }

        if (suit != null) {
            WebElement suitElement = driver.findElement(By.id(css + "Suit"));
            Select suitSelect = new Select(suitElement);
            suitSelect.selectByValue(suit);
        }
    }

    void setPlayerCard(int handIndex, int cardIndex, String rank) {
        setPlayerCard(handIndex, cardIndex, rank, null);
    }

    void setPlayerCard(int handIndex, int cardIndex, String rank, String suit) {
        String css = "playerHand" + handIndex + "Card" + cardIndex;
        if (rank != null) {
            WebElement webElement = driver.findElement(By.id(css + "Rank"));
            Select select = new Select(webElement);
            select.selectByValue(rank);
        }

        if (suit != null) {
            WebElement suitElement = driver.findElement(By.id(css + "Suit"));
            Select suitSelect = new Select(suitElement);
            suitSelect.selectByValue(suit);
        }
    }

    void sleepFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}