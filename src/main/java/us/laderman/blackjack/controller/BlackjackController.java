package us.laderman.blackjack.controller;

import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.service.BlackjackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@RestController
@RequestMapping("/api/blackjack")
public class BlackjackController {
    private final BlackjackService service;

    public BlackjackController(BlackjackService service) {
        this.service = service;
    }

    private void logRequest(String endpoint, String additionalInfo) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n").append("=".repeat(80)).append("\n");
        logMessage.append("📥 INCOMING REQUEST\n");
        logMessage.append("=".repeat(80)).append("\n");
        logMessage.append(String.format("Method: %s\n", request.getMethod()));
        logMessage.append(String.format("Endpoint: %s\n", endpoint));
        logMessage.append(String.format("Full URL: %s\n", request.getRequestURL()));
        logMessage.append(String.format("Remote Address: %s\n", request.getRemoteAddr()));

        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            logMessage.append(String.format("Query String: %s\n", request.getQueryString()));
        }

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logMessage.append(String.format("Parameters: %s\n", additionalInfo));
        }

        logMessage.append("=".repeat(80));
        System.out.println(logMessage.toString());
    }

    @RequestMapping(value = "/start", method = { RequestMethod.GET, RequestMethod.POST })
    public GameState start(
            @RequestParam int hands,
            @RequestParam java.util.Map<String, String> allParams) {
        logRequest("/api/blackjack/start", "hands=" + hands);
        try {
            return service.startGame(hands, allParams);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @RequestMapping(value = "/state/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public GameState state(@PathVariable String id) {
        logRequest("/api/blackjack/state/" + id, "id=" + id);
        return handleStateRequest(id, null, ActionType.STATE);
    }

    @RequestMapping(value = "/hit/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public GameState hit(
            @PathVariable String id,
            @RequestParam int hand,
            @RequestParam(required = false) String rank,
            @RequestParam(required = false) String suit) {
        String params = String.format("id=%s, hand=%d, rank=%s, suit=%s", id, hand, rank, suit);
        logRequest("/api/blackjack/hit/" + id, params);
        return handleHitRequest(id, hand, rank, suit);
    }

    private GameState handleHitRequest(String id, Integer hand, String rank, String suit) {
        UUID gameId;
        try {
            gameId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid game id", ex);
        }

        try {
            return service.hit(gameId, hand, rank, suit);
        } catch (IllegalArgumentException ex) {
            HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            throw new ResponseStatusException(status, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "/stand/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public GameState stand(@PathVariable String id, @RequestParam int hand) {
        String params = String.format("id=%s, hand=%d", id, hand);
        logRequest("/api/blackjack/stand/" + id, params);
        return handleStateRequest(id, hand, ActionType.STAND);
    }

    @RequestMapping(value = "/double/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public GameState doubleDown(
            @PathVariable String id,
            @RequestParam int hand,
            @RequestParam(required = false) String rank,
            @RequestParam(required = false) String suit) {
        String params = String.format("id=%s, hand=%d, rank=%s, suit=%s", id, hand, rank, suit);
        logRequest("/api/blackjack/double/" + id, params);
        return handleDoubleRequest(id, hand, rank, suit);
    }

    private GameState handleDoubleRequest(String id, Integer hand, String rank, String suit) {
        UUID gameId;
        try {
            gameId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid game id", ex);
        }

        try {
            return service.doubleDown(gameId, hand, rank, suit);
        } catch (IllegalArgumentException ex) {
            HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            throw new ResponseStatusException(status, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "/split/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public GameState split(@PathVariable String id, @RequestParam int hand,
            @RequestParam(required = false) String card1Rank,
            @RequestParam(required = false) String card1Suit,
            @RequestParam(required = false) String card2Rank,
            @RequestParam(required = false) String card2Suit) {
        String params = String.format("id=%s, hand=%d, card1Rank=%s, card1Suit=%s, card2Rank=%s, card2Suit=%s",
                id, hand, card1Rank, card1Suit, card2Rank, card2Suit);
        logRequest("/api/blackjack/split/" + id, params);
        UUID gameId;
        try {
            gameId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid game id", ex);
        }

        try {
            if (card1Rank != null || card1Suit != null || card2Rank != null || card2Suit != null) {
                return service.split(gameId, hand, card1Rank, card1Suit, card2Rank, card2Suit);
            } else {
                return service.split(gameId, hand);
            }
        } catch (IllegalArgumentException ex) {
            HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            throw new ResponseStatusException(status, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "/insurance/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public GameState insurance(@PathVariable String id, @RequestParam int hand) {
        String params = String.format("id=%s, hand=%d", id, hand);
        logRequest("/api/blackjack/insurance/" + id, params);
        return handleStateRequest(id, hand, ActionType.INSURANCE);
    }

    @RequestMapping(value = "/decline-insurance/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public GameState declineInsurance(@PathVariable String id, @RequestParam int hand) {
        String params = String.format("id=%s, hand=%d", id, hand);
        logRequest("/api/blackjack/decline-insurance/" + id, params);
        return handleStateRequest(id, hand, ActionType.DECLINE_INSURANCE);
    }

    private GameState handleStateRequest(String id, Integer hand, ActionType action) {
        UUID gameId;
        try {
            gameId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid game id", ex);
        }

        try {
            return switch (action) {
                case STATE -> service.getState(gameId);
                case STAND -> service.stand(gameId, hand);
                case INSURANCE -> service.insurance(gameId, hand);
                case DECLINE_INSURANCE -> service.declineInsurance(gameId, hand);
                default -> throw new IllegalArgumentException("Unsupported action: " + action);
            };
        } catch (IllegalArgumentException ex) {
            HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            throw new ResponseStatusException(status, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    private enum ActionType {
        STATE,
        HIT,
        STAND,
        DOUBLE,
        SPLIT,
        INSURANCE,
        DECLINE_INSURANCE
    }
}
