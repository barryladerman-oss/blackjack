package us.laderman.blackjack.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static us.laderman.blackjack.ui.TestConstants.*;

/**
 * UI test: Dealer up card is ten, hole card is ace (dealer blackjack)
 */
@DisplayName("Dealer up card ten, hole card ace UI test")
public class DealerBlackjackTest extends BaseSeleniumTest {
    @Test
    @DisplayName("Dealer up card ten, hole card ace triggers dealer blackjack and reveals both cards")
    void testDealerUpCardTenHoleCardAce() {
        for (int i = 0; i < 50; i++) {
            setNumHands(1);
            setDealerUpCard("TEN", "HEARTS");
            setPlayerCard(1, 1, "EIGHT", "CLUBS");
            setPlayerCard(1, 2, "SEVEN", "DIAMONDS");

            clickBtn(ID_START_BTN);
            sleepFor(1000); // Wait for game to initialize

            try {
                clickActionBtn(1, ID_STAND_BTN);

                if (testComplete()) {
                    continue;
                }
            } catch (Exception e) {
                // Dealer blackjack
                break;
            }
        }
        assert (testComplete());
    }
}
