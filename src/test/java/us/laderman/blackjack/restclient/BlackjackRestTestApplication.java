package us.laderman.blackjack.restclient;

import us.laderman.blackjack.model.GameState;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Scanner;

/**
 * Interactive CLI application for testing the Blackjack REST API.
 * Provides a menu-driven interface to test all endpoints.
 */
public class BlackjackRestTestApplication {
    private static final String DEFAULT_URL = "http://localhost:8080";
    private final BlackjackRestClient client;
    private final Scanner scanner;
    private final ObjectMapper objectMapper;
    private String currentGameId;

    public BlackjackRestTestApplication(String baseUrl) {
        this.client = new BlackjackRestClient(baseUrl);
        this.scanner = new Scanner(System.in);
        this.objectMapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        String baseUrl = args.length > 0 ? args[0] : DEFAULT_URL;
        System.out.println("=== Blackjack REST API Test Application ===");
        System.out.println("Base URL: " + baseUrl);

        BlackjackRestTestApplication app = new BlackjackRestTestApplication(baseUrl);
        app.run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> startNewGame();
                    case "2" -> getGameState();
                    case "3" -> hitCard();
                    case "4" -> standAction();
                    case "5" -> doubleDown();
                    case "6" -> splitHand();
                    case "7" -> takeInsurance();
                    case "8" -> declineInsurance();
                    case "9" -> listActiveGames();
                    case "0" -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Start New Game");
        System.out.println("2. Get Game State");
        System.out.println("3. Hit");
        System.out.println("4. Stand");
        System.out.println("5. Double Down");
        System.out.println("6. Split");
        System.out.println("7. Take Insurance");
        System.out.println("8. Decline Insurance");
        System.out.println("9. Game History");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
    }

    private void startNewGame() {
        System.out.print("Enter number of hands (1-5): ");
        int hands = Integer.parseInt(scanner.nextLine().trim());

        if (hands < 1 || hands > 5) {
            System.out.println("Invalid number of hands. Must be between 1 and 5.");
            return;
        }

        ApiResponse<GameState> response = client.startGame(hands);

        if (!response.isSuccess()) {
            System.out.println("Failed to start game!");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Error: " + response.getErrorMessage());
            System.out.println("\nNote: Please ensure the Blackjack server is running on http://localhost:8080");
            return;
        }

        printResponse(response, "Start Game");

        if (response.isSuccess()) {
            currentGameId = response.getData().getId();
            System.out.println("\nGame ID set to: " + currentGameId);
        }
    }

    private void getGameState() {
        if (currentGameId == null) {
            System.out.println("No active game. Please start a new game first.");
            return;
        }

        ApiResponse<GameState> response = client.getGameState(currentGameId);
        printResponse(response, "Get Game State");
    }

    private void hitCard() {
        if (currentGameId == null) {
            System.out.println("No active game. Please start a new game first.");
            return;
        }

        System.out.print("Enter hand number (0 and up): ");
        int hand = Integer.parseInt(scanner.nextLine().trim());

        ApiResponse<GameState> response = client.hit(currentGameId, hand);
        printResponse(response, "Hit");
    }

    private void standAction() {
        if (currentGameId == null) {
            System.out.println("No active game. Please start a new game first.");
            return;
        }

        System.out.print("Enter hand number (0 and up): ");
        int hand = Integer.parseInt(scanner.nextLine().trim());

        ApiResponse<GameState> response = client.stand(currentGameId, hand);
        printResponse(response, "Stand");
    }

    private void doubleDown() {
        if (currentGameId == null) {
            System.out.println("No active game. Please start a new game first.");
            return;
        }

        System.out.print("Enter hand number (0 and up): ");
        int hand = Integer.parseInt(scanner.nextLine().trim());

        ApiResponse<GameState> response = client.doubleDown(currentGameId, hand);
        printResponse(response, "Double Down");
    }

    private void splitHand() {
        if (currentGameId == null) {
            System.out.println("No active game. Please start a new game first.");
            return;
        }

        System.out.print("Enter hand number (0 and up): ");
        int hand = Integer.parseInt(scanner.nextLine().trim());

        ApiResponse<GameState> response = client.split(currentGameId, hand);
        printResponse(response, "Split");
    }

    private void takeInsurance() {
        if (currentGameId == null) {
            System.out.println("No active game. Please start a new game first.");
            return;
        }

        System.out.print("Enter hand number (0 and up): ");
        int hand = Integer.parseInt(scanner.nextLine().trim());

        ApiResponse<GameState> response = client.insurance(currentGameId, hand);
        printResponse(response, "Take Insurance");
    }

    private void declineInsurance() {
        if (currentGameId == null) {
            System.out.println("No active game. Please start a new game first.");
            return;
        }

        System.out.print("Enter hand number (0 and up): ");
        int hand = Integer.parseInt(scanner.nextLine().trim());

        ApiResponse<GameState> response = client.declineInsurance(currentGameId, hand);
        printResponse(response, "Decline Insurance");
    }

    private void listActiveGames() {
        System.out.println("\n=== Active Games ===");
        if (currentGameId != null) {
            System.out.println("Current Game ID: " + currentGameId);

            ApiResponse<GameState> response = client.getGameState(currentGameId);
            if (response.isSuccess()) {
                GameState state = response.getData();
                System.out.println("Phase: " + state.getPhase());
                System.out.println("Hands: " + state.getHands().size());
                System.out.println("Net Units: " + state.getNetUnits());
            }
        } else {
            System.out.println("No active games.");
        }
    }

    private void printResponse(ApiResponse<?> response, String operation) {
        printResponse(null, response, operation);
    }

    private void printResponse(String request, ApiResponse<?> response, String operation) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("=== " + operation + " ===");
        System.out.println("=".repeat(80));

        // Display request from response object if available
        if (response.getRequestDetails() != null) {
            System.out.println("\n📤 REQUEST:");
            HttpRequestDetails reqDetails = response.getRequestDetails();
            System.out.println("   Method: " + reqDetails.getMethod());
            System.out.println("   URL: " + reqDetails.getUrl());
            if (reqDetails.getBody() != null && !reqDetails.getBody().isEmpty()) {
                System.out.println("   Body: " + reqDetails.getBody());
            }
        } else if (request != null) {
            System.out.println("\n📤 REQUEST:");
            System.out.println("   " + request);
        }

        System.out.println("\n📥 RESPONSE:");
        System.out.println("   Status Code: " + response.getStatusCode());
        System.out.println("   Success: " + response.isSuccess());

        if (response.isSuccess()) {
            try {
                String json = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(response.getData());
                System.out.println("\n   Data:");
                // Indent the JSON response
                String[] lines = json.split("\n");
                for (String line : lines) {
                    System.out.println("   " + line);
                }
            } catch (Exception e) {
                System.out.println("   Data: " + response.getData());
            }
        } else {
            System.out.println("   Error: " + response.getErrorMessage());
        }
        System.out.println("=".repeat(80));
    }
}
