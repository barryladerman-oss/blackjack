package us.laderman.blackjack.restclient;

import us.laderman.blackjack.model.GameState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * REST client for testing the Blackjack REST API.
 * Provides methods to interact with all Blackjack endpoints.
 */
public class BlackjackRestClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BlackjackRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        // Configure ObjectMapper for robust deserialization
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // Register custom GameState deserializer
        SimpleModule module = new SimpleModule();
        module.addDeserializer(GameState.class, new GameStateDeserializer());
        this.objectMapper.registerModule(module);
    }

    /**
     * Start a new blackjack game.
     */
    public ApiResponse<GameState> startGame(int hands) {
        return startGame(hands, new HashMap<>());
    }

    /**
     * Start a new blackjack game with additional parameters.
     */
    public ApiResponse<GameState> startGame(int hands, Map<String, String> params) {
        Map<String, String> allParams = new HashMap<>(params);
        allParams.put("hands", String.valueOf(hands));

        String url = baseUrl + "/api/blackjack/start?" + buildQueryString(allParams);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Get the current state of a game.
     */
    public ApiResponse<GameState> getGameState(String gameId) {
        String url = baseUrl + "/api/blackjack/state/" + gameId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Hit on a specific hand.
     */
    public ApiResponse<GameState> hit(String gameId, int hand) {
        return hit(gameId, hand, null, null);
    }

    /**
     * Hit on a specific hand with optional card parameters.
     */
    public ApiResponse<GameState> hit(String gameId, int hand, String rank, String suit) {
        String url = buildUrlWithCards(
                baseUrl + "/api/blackjack/hit/" + gameId,
                hand, rank, suit);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Stand on a specific hand.
     */
    public ApiResponse<GameState> stand(String gameId, int hand) {
        String url = baseUrl + "/api/blackjack/stand/" + gameId + "?hand=" + hand;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Double down on a specific hand.
     */
    public ApiResponse<GameState> doubleDown(String gameId, int hand) {
        return doubleDown(gameId, hand, null, null);
    }

    /**
     * Double down on a specific hand with optional card parameters.
     */
    public ApiResponse<GameState> doubleDown(String gameId, int hand, String rank, String suit) {
        String url = buildUrlWithCards(
                baseUrl + "/api/blackjack/double/" + gameId,
                hand, rank, suit);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Split a hand.
     */
    public ApiResponse<GameState> split(String gameId, int hand) {
        String url = baseUrl + "/api/blackjack/split/" + gameId + "?hand=" + hand;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Split a hand with specific cards.
     */
    public ApiResponse<GameState> split(String gameId, int hand, String card1Rank, String card1Suit,
            String card2Rank, String card2Suit) {
        Map<String, String> params = new HashMap<>();
        params.put("hand", String.valueOf(hand));
        if (card1Rank != null) {
            params.put("card1Rank", card1Rank);
        }
        if (card1Suit != null) {
            params.put("card1Suit", card1Suit);
        }
        if (card2Rank != null) {
            params.put("card2Rank", card2Rank);
        }
        if (card2Suit != null) {
            params.put("card2Suit", card2Suit);
        }

        String url = baseUrl + "/api/blackjack/split/" + gameId + "?" + buildQueryString(params);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Take insurance on a hand.
     */
    public ApiResponse<GameState> insurance(String gameId, int hand) {
        String url = baseUrl + "/api/blackjack/insurance/" + gameId + "?hand=" + hand;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Decline insurance on a hand.
     */
    public ApiResponse<GameState> declineInsurance(String gameId, int hand) {
        String url = baseUrl + "/api/blackjack/decline-insurance/" + gameId + "?hand=" + hand;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return executeRequest(request, GameState.class);
    }

    /**
     * Execute an HTTP request and parse the response.
     */
    private <T> ApiResponse<T> executeRequest(HttpRequest request, Class<T> responseType) {
        try {
            // Capture request details
            String method = request.method();
            String url = request.uri().toString();
            HttpRequestDetails requestDetails = new HttpRequestDetails(method, url);

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String body = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                try {
                    T data = objectMapper.readValue(body, responseType);
                    return new ApiResponse<>(statusCode, data, null, true, requestDetails);
                } catch (Exception deserializeException) {
                    String errorMsg = "Failed to deserialize response. Expected: " + responseType.getName()
                            + ". Response body: " + body + ". Error: " + deserializeException.getMessage();
                    System.err.println(errorMsg);
                    deserializeException.printStackTrace();
                    return new ApiResponse<>(statusCode, null, errorMsg, false, requestDetails);
                }
            } else {
                return new ApiResponse<>(statusCode, null, body, false, requestDetails);
            }
        } catch (Exception e) {
            String errorMsg = "HTTP request failed: " + e.getMessage();
            e.printStackTrace();
            return new ApiResponse<>(-1, null, errorMsg, false, null);
        }
    }

    private String buildUrlWithCards(String baseUrl, int hand, String rank, String suit) {
        Map<String, String> params = new HashMap<>();
        params.put("hand", String.valueOf(hand));
        if (rank != null) {
            params.put("rank", rank);
        }
        if (suit != null) {
            params.put("suit", suit);
        }
        return baseUrl + "?" + buildQueryString(params);
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + encodeUriComponent(e.getValue()))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
    }

    private String encodeUriComponent(String component) {
        try {
            return java.net.URLEncoder.encode(component, java.nio.charset.StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
        } catch (Exception e) {
            return component;
        }
    }
}
