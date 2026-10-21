package us.laderman.blackjack.ui;

import java.lang.reflect.InvocationTargetException;

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
import org.junit.jupiter.api.Assumptions;
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
        Assumptions.assumeTrue(isChromeAvailable(),
                "Chrome/Chromium browser is not installed in this environment; skipping UI test");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        String chromeBinary = resolveChromeBinary();
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
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

    private static boolean isChromeAvailable() {
        return resolveChromeBinary() != null;
    }

    private static String resolveChromeBinary() {
        String configuredPath = System.getenv("CHROME_BIN");
        if (configuredPath != null && !configuredPath.isBlank() && java.nio.file.Files.exists(java.nio.file.Paths.get(configuredPath))) {
            return configuredPath;
        }

        String chromiumPath = System.getenv("CHROMIUM_BIN");
        if (chromiumPath != null && !chromiumPath.isBlank() && java.nio.file.Files.exists(java.nio.file.Paths.get(chromiumPath))) {
            return chromiumPath;
        }

        String[] candidates = {
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files\\Chromium\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Chromium\\Application\\chrome.exe",
                "/usr/bin/google-chrome",
                "/usr/bin/google-chrome-stable",
                "/usr/bin/chromium",
                "/usr/bin/chromium-browser",
                "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                "/Applications/Chromium.app/Contents/MacOS/Chromium"
        };

        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                java.nio.file.Path path = java.nio.file.Paths.get(candidate);
                if (java.nio.file.Files.exists(path)) {
                    return candidate;
                }
            }
        }

        return null;
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
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
        }
        try {
            Class<?> logManager = Class.forName("org.apache.logging.log4j.LogManager");
            Object logger = logManager.getMethod("getLogger", String.class).invoke(null, "io.github.bonigarcia.wdm");
            logger.getClass().getMethod("setLevel", Class.forName("org.apache.logging.log4j.Level")).invoke(logger,
                    Class.forName("org.apache.logging.log4j.Level").getField("ERROR").get(null));
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException ignored) {
        }
    }

    void sleepFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void closeModalIfOpen() {
        java.util.function.Consumer<String> closeModalById = (modalId) -> {
            WebElement modal = driver.findElement(By.id(modalId));
            if (!modal.isDisplayed())
                return;

            String css;
            switch (modalId) {
                case ID_HIT_CARD_MODAL -> css = "hitCardCancelBtn";
                case ID_DOUBLE_CARD_MODAL -> css = "doubleCardCancelBtn";
                case ID_SPLIT_CARD_MODAL -> css = "splitCardCancelBtn";
                default -> {
                    return;
                }
            }
            WebElement cancelBtn = modal.findElement(By.id(css));

            cancelBtn.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(modalId)));
        };
        closeModalById.accept(ID_HIT_CARD_MODAL);
        closeModalById.accept(ID_SPLIT_CARD_MODAL);
        closeModalById.accept(ID_DOUBLE_CARD_MODAL);
    }

    public boolean testComplete() {
        sleepFor(1000);

        WebElement phase = driver.findElement(By.id(ID_PHASE));
        if (phase.getText().contains("COMPLETE")) {
            return true;
        }

        wait.until(ExpectedConditions.stalenessOf(phase));
        assertThat(phase.getText()).contains("COMPLETE");
        return true;
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

    void setDealerUpCard(String rank, String suit) {
        setDealerCard("dealerCard1", rank, suit);
    }

    void setDealerHoleCard(String rank, String suit) {
        setDealerCard("dealerCard2", rank, suit);
    }

    private void setDealerCard(String prefix, String rank, String suit) {
        if (rank != null) {
            WebElement webElement = driver.findElement(By.id(prefix + "Rank"));
            Select select = new Select(webElement);
            select.selectByValue(rank);
        }

        if (suit != null) {
            WebElement suitElement = driver.findElement(By.id(prefix + "Suit"));
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
}