package us.laderman.android.blackjack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import us.laderman.android.blackjack.api.ApiClient;
import us.laderman.android.blackjack.api.BlackjackApiService;
import us.laderman.android.blackjack.model.Card;
import us.laderman.android.blackjack.model.GameState;
import us.laderman.android.blackjack.model.PlayerHandState;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private EditText serverUrlInput;
    private Button connectButton;
    private Button exitButton;
    private Button settingsButton;
    private LinearLayout configLayout;
    private TextView countingText;
    private TextView cardsRemainingText;
    private TextView dealerCardsText;
    private TextView dealerValueText;
    private LinearLayout playerHandsContainer;
    private Button newGameButton;
    private Button hitButton;
    private Button doubleButton;
    private Button splitButton;
    private Button standButton;
    private TextView statusText;

    // Game State
    private BlackjackApiService apiService;
    private String currentGameId;
    private int currentHandIndex = 0;
    private GameState currentGameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();

        // Initialize API service with default URL
        apiService = ApiClient.getBlackjackService();
    }

    private void initializeViews() {
        serverUrlInput = findViewById(R.id.serverUrlInput);
        connectButton = findViewById(R.id.connectButton);
        exitButton = findViewById(R.id.exitButton);
        settingsButton = findViewById(R.id.settingsButton);
        configLayout = findViewById(R.id.configLayout);
        countingText = findViewById(R.id.countingText);
        cardsRemainingText = findViewById(R.id.cardsRemainingText);
        dealerCardsText = findViewById(R.id.dealerCardsText);
        dealerValueText = findViewById(R.id.dealerValueText);
        playerHandsContainer = findViewById(R.id.playerHandsContainer);
        newGameButton = findViewById(R.id.newGameButton);
        hitButton = findViewById(R.id.hitButton);
        doubleButton = findViewById(R.id.doubleButton);
        splitButton = findViewById(R.id.splitButton);
        standButton = findViewById(R.id.standButton);
        statusText = findViewById(R.id.statusText);
    }

    private void setupListeners() {
        settingsButton.setOnClickListener(v -> toggleSettings());
        connectButton.setOnClickListener(v -> connectToServer());
        exitButton.setOnClickListener(v -> finishAndRemoveTask());
        newGameButton.setOnClickListener(v -> startNewGame());
        hitButton.setOnClickListener(v -> performHit());
        doubleButton.setOnClickListener(v -> performDouble());
        splitButton.setOnClickListener(v -> performSplit());
        standButton.setOnClickListener(v -> performStand());
    }

    private void toggleSettings() {
        if (configLayout.getVisibility() == View.VISIBLE) {
            configLayout.setVisibility(View.GONE);
        } else {
            configLayout.setVisibility(View.VISIBLE);
        }
    }

    private void connectToServer() {
        String url = serverUrlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a server URL", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.endsWith("/")) {
            url += "/";
        }

        apiService = ApiClient.getBlackjackService(url);
        Toast.makeText(this, "Connected to: " + url, Toast.LENGTH_SHORT).show();
        statusText.setText("Connected to server. Start a new game!");
    }

    private void startNewGame() {
        statusText.setText("Starting new game...");

        // Start with 1 hand, 1 deck, allow double on 9, dealer stands on soft 17
        Call<GameState> call = apiService.startGame(1, 1, true, false);
        call.enqueue(new Callback<GameState>() {
            @Override
            public void onResponse(Call<GameState> call, Response<GameState> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentGameState = response.body();
                    currentGameId = currentGameState.getId();
                    currentHandIndex = 0;
                    updateUI();
                    enableGameButtons();
                    newGameButton.setVisibility(View.GONE);
                    statusText.setText("Game started!");
                    checkHandStatus(); // Check if game is already complete (e.g., dealer blackjack)
                } else {
                    statusText.setText("Failed to start game: " + response.code());
                    Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GameState> call, Throwable t) {
                statusText.setText("Network error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performHit() {
        if (currentGameId == null) {
            Toast.makeText(this, "Please start a game first", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Hitting hand " + (currentHandIndex + 1) + "...");

        Call<GameState> call = apiService.hit(currentGameId, currentHandIndex);
        call.enqueue(new Callback<GameState>() {
            @Override
            public void onResponse(Call<GameState> call, Response<GameState> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentGameState = response.body();
                    updateUI();
                    checkHandStatus();
                } else {
                    statusText.setText("Failed to hit: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameState> call, Throwable t) {
                statusText.setText("Network error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performDouble() {
        if (currentGameId == null) {
            Toast.makeText(this, "Please start a game first", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Doubling down on hand " + (currentHandIndex + 1) + "...");

        Call<GameState> call = apiService.doubleDown(currentGameId, currentHandIndex);
        call.enqueue(new Callback<GameState>() {
            @Override
            public void onResponse(Call<GameState> call, Response<GameState> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentGameState = response.body();
                    updateUI();
                    checkHandStatus();
                } else {
                    statusText.setText("Failed to double: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameState> call, Throwable t) {
                statusText.setText("Network error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performStand() {
        if (currentGameId == null) {
            Toast.makeText(this, "Please start a game first", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Standing hand " + (currentHandIndex + 1) + "...");

        Call<GameState> call = apiService.stand(currentGameId, currentHandIndex);
        call.enqueue(new Callback<GameState>() {
            @Override
            public void onResponse(Call<GameState> call, Response<GameState> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentGameState = response.body();
                    updateUI();
                    checkHandStatus();
                } else {
                    statusText.setText("Failed to stand: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameState> call, Throwable t) {
                statusText.setText("Network error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkHandStatus() {
        if (currentGameState == null || currentGameState.getHands() == null) {
            return;
        }

        List<PlayerHandState> hands = currentGameState.getHands();

        // Check if current hand is done
        if (currentHandIndex < hands.size()) {
            PlayerHandState currentHand = hands.get(currentHandIndex);
            if (currentHand.isBusted() || currentHand.isStanding() || currentHand.isBlackjack()) {
                // Move to next hand
                currentHandIndex++;
                if (currentHandIndex < hands.size()) {
                    statusText.setText("Moving to hand " + (currentHandIndex + 1));
                    updateUI(); // Refresh UI to show active hand with buttons
                }
            }
        }

        // Check if game is complete
        if ("COMPLETE".equals(currentGameState.getPhase())) {
            disableGameButtons();
            statusText.setText("Game complete! " + getGameSummary());
        } else if (currentHandIndex >= hands.size()) {
            // All hands are done but game might not be marked complete yet
            disableGameButtons();
            statusText.setText("All hands complete. Game finishing...");
        }
    }

    private String getGameSummary() {
        if (currentGameState == null)
            return "";

        double netUnits = currentGameState.getNetUnits();
        String result;
        String amount;

        if (netUnits > 0) {
            result = "You won";
            amount = String.format("$%.2f", netUnits);
        } else if (netUnits < 0) {
            result = "You lost";
            amount = String.format("$%.2f", Math.abs(netUnits));
        } else {
            result = "Push";
            amount = "$0.00";
        }

        return result + " " + amount;
    }

    private void updateUI() {
        if (currentGameState == null) {
            return;
        }

        // Update game stats
        countingText.setText("Running Count: " + currentGameState.getRunningCount() +
                " | True Count: " + String.format("%.1f", currentGameState.getTrueCount()));
        cardsRemainingText.setText("Cards Remaining: " + currentGameState.getCardsRemaining());

        // Update dealer
        updateDealerUI();

        // Update player hands
        updatePlayerHandsUI();

        // Update button states
        updateDoubleButtonState();
        updateSplitButtonState();
    }

    private void updateDealerUI() {
        if (currentGameState.getDealer() == null) {
            dealerCardsText.setText("No cards");
            dealerValueText.setText("Total: 0");
            return;
        }

        // Check if all player hands are done (standing or busted)
        boolean allHandsDone = true;
        if (currentGameState.getHands() != null) {
            for (PlayerHandState hand : currentGameState.getHands()) {
                if (!hand.isStanding() && !hand.isBusted()) {
                    allHandsDone = false;
                    break;
                }
            }
        }

        List<Card> dealerCards = currentGameState.getDealer().getCards();
        if (dealerCards != null && !dealerCards.isEmpty()) {
            StringBuilder cardsDisplay = new StringBuilder();

            if (allHandsDone || "COMPLETE".equals(currentGameState.getPhase())) {
                // Show all dealer cards
                for (Card card : dealerCards) {
                    cardsDisplay.append(card.getDisplayText()).append(" ");
                }
                dealerCardsText.setText(cardsDisplay.toString().trim());

                int total = currentGameState.getDealer().getTotal();
                String valueText = "Total: " + total;
                if (currentGameState.getDealer().isSoft()) {
                    valueText += " (soft)";
                }
                dealerValueText.setText(valueText);

                if (currentGameState.getDealer().isBusted()) {
                    dealerValueText.append(" BUST");
                } else if (currentGameState.getDealer().isBlackjack()) {
                    dealerValueText.append(" BLACKJACK");
                }
            } else {
                // Show only first card, hide hole card
                if (dealerCards.size() > 0) {
                    cardsDisplay.append(dealerCards.get(0).getDisplayText()).append(" ");
                }
                if (dealerCards.size() > 1) {
                    cardsDisplay.append("🂠"); // Card back symbol
                }
                dealerCardsText.setText(cardsDisplay.toString().trim());
                dealerValueText.setText("Total: ?");
            }
        } else {
            dealerCardsText.setText("No cards");
            dealerValueText.setText("Total: 0");
        }
    }

    private void updatePlayerHandsUI() {
        playerHandsContainer.removeAllViews();

        if (currentGameState.getHands() == null) {
            return;
        }

        List<PlayerHandState> hands = currentGameState.getHands();
        for (int i = 0; i < hands.size(); i++) {
            PlayerHandState hand = hands.get(i);
            View handView = createHandView(hand, i);
            playerHandsContainer.addView(handView);
        }
    }

    private View createHandView(PlayerHandState hand, int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.hand_card_view, playerHandsContainer, false);
        CardView cardView = (CardView) view;

        TextView handTitle = view.findViewById(R.id.handTitle);
        TextView handCards = view.findViewById(R.id.handCards);
        TextView handValue = view.findViewById(R.id.handValue);
        TextView handStatus = view.findViewById(R.id.handStatus);
        TextView handRecommendation = view.findViewById(R.id.handRecommendation);
        TextView handOutcome = view.findViewById(R.id.handOutcome);
        LinearLayout buttonsContainer = view.findViewById(R.id.handButtonsContainer);
        LinearLayout insuranceButtonsContainer = view.findViewById(R.id.insuranceButtonsContainer);
        Button hitBtn = view.findViewById(R.id.handHitButton);
        Button doubleBtn = view.findViewById(R.id.handDoubleButton);
        Button splitBtn = view.findViewById(R.id.handSplitButton);
        Button standBtn = view.findViewById(R.id.handStandButton);
        Button insuranceBtn = view.findViewById(R.id.insuranceButton);
        Button declineInsuranceBtn = view.findViewById(R.id.declineInsuranceButton);

        // Highlight active hand, dim inactive hands
        boolean isActiveHand = index == currentHandIndex && !"COMPLETE".equals(currentGameState.getPhase());
        boolean isHandComplete = hand.isBusted() || hand.isStanding() || hand.isBlackjack();

        if (isActiveHand) {
            cardView.setCardBackgroundColor(0xFFA0826D); // Light brown
            cardView.setAlpha(1.0f);
        } else {
            cardView.setCardBackgroundColor(0xFF6B5D4F); // Darker brown
            cardView.setAlpha(0.6f);
        }

        // Set title
        String title = "Hand " + (index + 1);
        if (isActiveHand) {
            title += " ◄ ACTIVE";
        }
        handTitle.setText(title);

        // Display cards
        if (hand.getCards() != null && !hand.getCards().isEmpty()) {
            StringBuilder cardsDisplay = new StringBuilder();
            for (Card card : hand.getCards()) {
                cardsDisplay.append(card.getDisplayText()).append(" ");
            }
            handCards.setText(cardsDisplay.toString().trim());
        } else {
            handCards.setText("No cards");
        }

        // Display value
        String totalText = "Total: " + hand.getTotal();
        if (hand.isSoft()) {
            totalText += " (soft)";
        }
        handValue.setText(totalText);

        // Display status
        StringBuilder status = new StringBuilder();
        if (hand.isBlackjack()) {
            status.append("BLACKJACK! ");
        }
        if (hand.isBusted()) {
            status.append("BUST! ");
        }
        if (hand.isStanding()) {
            status.append("Standing ");
        }
        handStatus.setText(status.toString().trim());

        // Display recommendation
        if (hand.getRecommendation() != null && !hand.getRecommendation().isEmpty()) {
            handRecommendation.setText("Rec: " + hand.getRecommendation());
            handRecommendation.setVisibility(View.VISIBLE);
        } else {
            handRecommendation.setVisibility(View.GONE);
        }

        // Display outcome
        if (hand.getOutcome() != null && !hand.getOutcome().isEmpty()) {
            String outcomeText = hand.getOutcome();

            // Calculate money won/lost
            if (hand.isWon()) {
                double amount = hand.getBetUnits();
                outcomeText += String.format(" +$%.2f", amount);
            } else if (!hand.isPush() && !hand.getOutcome().isEmpty()) {
                // Lost (not a push)
                double amount = hand.getBetUnits();
                outcomeText += String.format(" -$%.2f", amount);
            } else if (hand.isPush()) {
                outcomeText += " $0.00";
            }

            handOutcome.setText(outcomeText);
            handOutcome.setVisibility(View.VISIBLE);
        } else {
            handOutcome.setVisibility(View.GONE);
        }

        // Setup hand buttons
        if (isActiveHand && !isHandComplete && !"COMPLETE".equals(currentGameState.getPhase())) {
            buttonsContainer.setVisibility(View.VISIBLE);

            // Hit button - always available for active hand
            hitBtn.setOnClickListener(v -> performHitForHand(index));
            hitBtn.setEnabled(true);

            // Stand button - always available for active hand
            standBtn.setOnClickListener(v -> performStandForHand(index));
            standBtn.setEnabled(true);

            // Double button - only if total is 10 or 11 and exactly 2 cards
            int total = hand.getTotal();
            int cardCount = hand.getCards() != null ? hand.getCards().size() : 0;
            boolean canDouble = (total == 10 || total == 11) && cardCount == 2;
            if (canDouble) {
                doubleBtn.setVisibility(View.VISIBLE);
                doubleBtn.setEnabled(true);
                doubleBtn.setOnClickListener(v -> performDoubleForHand(index));
            } else {
                doubleBtn.setVisibility(View.GONE);
            }

            // Split button - only if exactly 2 cards of same rank and no split has occurred
            // yet
            List<Card> cards = hand.getCards();
            int totalHands = currentGameState.getHands() != null ? currentGameState.getHands().size() : 0;
            boolean canSplit = cards != null && cards.size() == 2 &&
                    cards.get(0).getRank().equals(cards.get(1).getRank()) &&
                    totalHands == 1; // Only allow split if still one hand (no previous split)
            if (canSplit) {
                splitBtn.setVisibility(View.VISIBLE);
                splitBtn.setEnabled(true);
                splitBtn.setOnClickListener(v -> performSplitForHand(index));
            } else {
                splitBtn.setVisibility(View.GONE);
            }
        } else {
            buttonsContainer.setVisibility(View.GONE);
        }

        return view;
    }

    private void performHitForHand(int handIndex) {
        currentHandIndex = handIndex;
        performHit();
    }

    private void performDoubleForHand(int handIndex) {
        currentHandIndex = handIndex;
        performDouble();
    }

    private void performSplitForHand(int handIndex) {
        currentHandIndex = handIndex;
        performSplit();
    }

    private void performStandForHand(int handIndex) {
        currentHandIndex = handIndex;
        performStand();
    }

    private void enableGameButtons() {
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
        updateDoubleButtonState();
        updateSplitButtonState();
    }

    private void disableGameButtons() {
        hitButton.setEnabled(false);
        doubleButton.setEnabled(false);
        doubleButton.setVisibility(View.GONE);
        splitButton.setEnabled(false);
        splitButton.setVisibility(View.GONE);
        standButton.setEnabled(false);
        newGameButton.setVisibility(View.VISIBLE);
    }

    private void updateDoubleButtonState() {
        // Enable double if current hand has total of 10 or 11 and exactly 2 cards
        boolean canDouble = false;
        if (currentGameState != null && currentGameState.getHands() != null) {
            List<PlayerHandState> hands = currentGameState.getHands();
            if (currentHandIndex < hands.size()) {
                PlayerHandState currentHand = hands.get(currentHandIndex);
                int total = currentHand.getTotal();
                int cardCount = currentHand.getCards() != null ? currentHand.getCards().size() : 0;
                canDouble = (total == 10 || total == 11) && cardCount == 2 && !currentHand.isStanding()
                        && !currentHand.isBusted();
            }
        }
        doubleButton.setEnabled(canDouble);
        doubleButton.setVisibility(canDouble ? View.VISIBLE : View.GONE);
    }

    private void updateSplitButtonState() {
        // Enable split if current hand has exactly 2 cards with the same rank
        boolean canSplit = false;
        if (currentGameState != null && currentGameState.getHands() != null) {
            List<PlayerHandState> hands = currentGameState.getHands();
            if (currentHandIndex < hands.size()) {
                PlayerHandState currentHand = hands.get(currentHandIndex);
                List<Card> cards = currentHand.getCards();
                if (cards != null && cards.size() == 2 && !currentHand.isStanding() && !currentHand.isBusted()) {
                    // Check if both cards have the same rank
                    canSplit = cards.get(0).getRank().equals(cards.get(1).getRank());
                }
            }
        }
        splitButton.setEnabled(canSplit);
        splitButton.setVisibility(canSplit ? View.VISIBLE : View.GONE);
    }

    private void performSplit() {
        if (currentGameId == null) {
            Toast.makeText(this, "Please start a game first", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Splitting hand " + (currentHandIndex + 1) + "...");

        Call<GameState> call = apiService.split(currentGameId, currentHandIndex);
        call.enqueue(new Callback<GameState>() {
            @Override
            public void onResponse(Call<GameState> call, Response<GameState> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentGameState = response.body();
                    updateUI();
                    checkHandStatus();
                } else {
                    statusText.setText("Failed to split: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameState> call, Throwable t) {
                statusText.setText("Network error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
