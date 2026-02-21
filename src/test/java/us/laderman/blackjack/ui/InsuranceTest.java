package us.laderman.blackjack.ui;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;

public class InsuranceTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Player hand action buttons should not appear until insurance is resolved on all hands")
    void testPlayerHandActionButtonsHiddenUntilInsuranceResolved() {
        int numHands = 4;
        setNumHands(numHands);
        setDealerUpCard("ACE", "SPADES");
        setPlayerCard(1, 1, "TEN");
        setPlayerCard(2, 1, "TEN");
        setPlayerCard(3, 1, "TEN");
        setPlayerCard(4, 1, "TEN");
        setPlayerCard(1, 2, "NINE");
        setPlayerCard(2, 2, "NINE");
        setPlayerCard(3, 2, "NINE");
        setPlayerCard(4, 2, "NINE");

        clickBtn("startBtn");

        // Wait for insurance buttons to appear for both hands
        String css = ".insurance-btn";
        List<WebElement> insuranceBtns = wait.until(ExpectedConditions
                .visibilityOfAllElementsLocatedBy(By.cssSelector(css)));
        assert (insuranceBtns.size() == numHands * 2);

        // Check that action buttons are NOT visible for all hands
        css = ".hand-actions button[data-action]";
        List<WebElement> handActions = driver.findElements(By.cssSelector(css));
        for (WebElement webElement : handActions) {
            String text = webElement.getText();
            assert (!text.contains("Stand"));
        }

        // Resolve insurance for all hands
        while (insuranceBtns.size() > 0) {
            for (WebElement btn : insuranceBtns) {
                String text = btn.getText();
                if (text.contains("Decline")) {
                    wait.until(ExpectedConditions.elementToBeClickable(btn));
                    btn.click();

                    insuranceBtns = driver.findElements(By.cssSelector(".insurance-btn"));
                    break;
                }
            }
        }
        sleepFor(1000);

        // Now action buttons should appear for the first hand
        css = ".hand-actions button[data-action]";
        handActions = driver.findElements(By.cssSelector(css));
        assert (handActions.size() > 0);
    }
}
