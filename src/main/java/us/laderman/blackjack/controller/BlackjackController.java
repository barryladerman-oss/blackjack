package us.laderman.blackjack.controller;

import us.laderman.blackjack.model.GameState;
import us.laderman.blackjack.service.BlackjackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/blackjack")
public class BlackjackController {
    private final BlackjackService service;

    public BlackjackController(BlackjackService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public GameState start(
            @RequestParam int hands,
            @RequestParam java.util.Map<String, String> allParams) {
        try {
            return service.startGame(hands, allParams);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/state/{id}")
    public GameState state(@PathVariable String id) {
        return handleStateRequest(id, null, ActionType.STATE);
    }

    @PostMapping("/hit/{id}")
    public GameState hit(
            @PathVariable String id,
            @RequestParam int hand,
            @RequestParam(required = false) String rank,
            @RequestParam(required = false) String suit) {
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

    @PostMapping("/stand/{id}")
    public GameState stand(@PathVariable String id, @RequestParam int hand) {
        return handleStateRequest(id, hand, ActionType.STAND);
    }

    @PostMapping("/double/{id}")
    public GameState doubleDown(@PathVariable String id, @RequestParam int hand) {
        return handleStateRequest(id, hand, ActionType.DOUBLE);
    }

    @PostMapping("/split/{id}")
    public GameState split(@PathVariable String id, @RequestParam int hand,
            @RequestParam(required = false) String card1Rank,
            @RequestParam(required = false) String card1Suit,
            @RequestParam(required = false) String card2Rank,
            @RequestParam(required = false) String card2Suit) {
        System.out.println("DEBUG: Controller split called with hand=" + hand + ", card1Rank=" + card1Rank
                + ", card1Suit=" + card1Suit + ", card2Rank=" + card2Rank + ", card2Suit=" + card2Suit);
        UUID gameId;
        try {
            gameId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid game id", ex);
        }

        try {
            if (card1Rank != null || card1Suit != null || card2Rank != null || card2Suit != null) {
                System.out.println("DEBUG: Calling service.split with card parameters");
                return service.split(gameId, hand, card1Rank, card1Suit, card2Rank, card2Suit);
            } else {
                System.out.println("DEBUG: Calling service.split without card parameters");
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

    @PostMapping("/insurance/{id}")
    public GameState insurance(@PathVariable String id, @RequestParam int hand) {
        return handleStateRequest(id, hand, ActionType.INSURANCE);
    }

    @PostMapping("/decline-insurance/{id}")
    public GameState declineInsurance(@PathVariable String id, @RequestParam int hand) {
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
                case HIT -> service.hit(gameId, hand);
                case STAND -> service.stand(gameId, hand);
                case DOUBLE -> service.doubleDown(gameId, hand);
                case SPLIT -> service.split(gameId, hand);
                case INSURANCE -> service.insurance(gameId, hand);
                case DECLINE_INSURANCE -> service.declineInsurance(gameId, hand);
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
