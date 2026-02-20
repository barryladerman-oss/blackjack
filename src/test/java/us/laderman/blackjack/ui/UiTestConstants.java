package us.laderman.blackjack.ui;

/**
 * Shared constants for UI Selenium tests.
 */
public final class UiTestConstants {
    // Modal IDs
    public static final String ID_HIT_CARD_MODAL = "hitCardModal";
    public static final String ID_SPLIT_CARD_MODAL = "splitCardModal";
    public static final String ID_DOUBLE_CARD_MODAL = "doubleCardModal";
    // ChromeOptions arguments

    // Window dimensions
    public static final int BROWSER_WIDTH = 1200;
    public static final int BROWSER_HEIGHT = 1000;
    public static final String BASE_URL = "http://localhost";

    // Timeouts
    public static final int WAIT_TIMEOUT_SECONDS = 10;

    // Element IDs
    public static final String ID_START_BTN = "startBtn";
    public static final String ID_HANDS = "hands";
    public static final String ID_DECKS = "decks";
    public static final String ID_GAME_STATE = "gameState";
    public static final String ID_PHASE = "phase";
    public static final String ID_HIT_BTN = "hit";
    public static final String ID_STAND_BTN = "stand";
    public static final String ID_TRUE_COUNT = "trueCount";
    public static final String ID_RUNNING_COUNT = "runningCount";
    public static final String ID_SUGGESTED_BET = "suggestedBet";
    public static final String ID_DEALER_UPCARD = "dealerUpcard";
    public static final String ID_GAME_ID = "gameId";
    public static final String ID_DECKS_REMAINING = "decksRemaining";
    public static final String ID_DOUBLE_BTN = "doubleBtn";
    public static final String ID_SPLIT_BTN = "splitBtn";

    public static final String ID_HIT_CANCEL_BTN = "hitCardCancelBtn";
    public static final String ID_HIT_CONFIRM_BTN = "hitCardConfirmBtn";
    public static final String ID_SPLIT_CANCEL_BTN = "splitCardCancelBtn";
    public static final String ID_SPLIT_CONFIRM_BTN = "splitCardConfirmBtn";
    public static final String ID_DOUBLE_ANCEL_BTN = "doubleCardCancelBtn";
    public static final String ID_DOUBLE_CONFIRM_BTN = "doubleCardConfirmBtn";

    public static final String ID_INSURANCE_BTN = "insurance";

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

    private UiTestConstants() {
        // Prevent instantiation
    }
}
