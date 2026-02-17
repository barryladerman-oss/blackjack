package us.laderman.blackjack.ui;

/**
 * Shared constants for UI Selenium tests.
 */
public final class UiTestConstants {
    // ChromeOptions arguments
    public static final String CHROME_HEADLESS_ARG = "--headless=new";
    public static final String CHROME_NO_SANDBOX_ARG = "--no-sandbox";
    public static final String CHROME_DISABLE_DEV_SHM_ARG = "--disable-dev-shm-usage";
    public static final String BASE_URL_PREFIX = "http://localhost:";

    private UiTestConstants() {
        // Prevent instantiation
    }

    // Window dimensions
    public static final int BROWSER_WIDTH = 1200;
    public static final int BROWSER_HEIGHT = 1000;

    // Timeouts
    public static final int WAIT_TIMEOUT_SECONDS = 10;

    // Element IDs
    public static final String ID_START_BTN = "startBtn";
    public static final String ID_HANDS = "hands";
    public static final String ID_GAME_STATE = "gameState";
    public static final String ID_PHASE = "phase";
    public static final String ID_HIT_BTN = "hitBtn";
    public static final String ID_STAND_BTN = "standBtn";
    public static final String ID_TRUE_COUNT = "trueCount";
    public static final String ID_RUNNING_COUNT = "runningCount";
    public static final String ID_SUGGESTED_BET = "suggestedBet";
    public static final String ID_DEALER_UPCARD = "dealerUpcard";
    public static final String ID_DEALER_CARDS = "dealerCards";
    public static final String ID_GAME_ID = "gameId";
    public static final String ID_DECKS_REMAINING = "decksRemaining";
    public static final String ID_CARD_SELECTION_DETAILS = "cardSelectionDetails";
    public static final String ID_DEALER_CARD1_RANK = "dealerCard1Rank";
    public static final String ID_DEALER_CARD1_SUIT = "dealerCard1Suit";
    public static final String ID_TRUE_COUNT_DEBUG = "trueCountDebug";
    public static final String ID_RUNNING_COUNT_DEBUG = "runningCountDebug";

    // Game phases
    public static final String PHASE_PLAYER_TURN = "PLAYER_TURN";
    public static final String PHASE_DEALER_TURN = "DEALER_TURN";
    public static final String PHASE_COMPLETE = "COMPLETE";

    // Default values
    public static final String DEFAULT_HANDS = "5";
    public static final String SINGLE_HAND = "1";
    public static final String EMPTY_VALUE = "-";

    // JSON field names
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_HANDS = "hands";
    public static final String JSON_FIELD_CARDS = "cards";
    public static final String JSON_FIELD_PHASE = "phase";
    public static final String JSON_FIELD_INDEX = "index";

    // Validation patterns
    public static final String PATTERN_NUMBER_OR_DASH = "-|\\d+";
    public static final String PATTERN_DECIMAL_OR_DASH = "-|\\d+\\.\\d+|\\d+";

    // Page content
    public static final String PAGE_TITLE = "Blackjack";

    // Card suits
    public static final String SUIT_HEARTS = "HEARTS";
    public static final String SUIT_DIAMONDS = "DIAMONDS";
    public static final String SUIT_CLUBS = "CLUBS";
    public static final String SUIT_SPADES = "SPADES";

    // Card ranks
    public static final String RANK_ACE = "ACE";
    public static final String RANK_TWO = "2";
    public static final String RANK_THREE = "3";
    public static final String RANK_FOUR = "4";
    public static final String RANK_FIVE = "5";
    public static final String RANK_SIX = "6";
    public static final String RANK_SEVEN = "7";
    public static final String RANK_EIGHT = "8";
    public static final String RANK_NINE = "9";
    public static final String RANK_TEN = "10";
    public static final String RANK_JACK = "JACK";
    public static final String RANK_QUEEN = "QUEEN";
    public static final String RANK_KING = "KING";
}
