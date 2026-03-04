package com.chess.controller;

import com.chess.dto.GameResponseDto;
import com.chess.dto.LegalMovesResponseDto;
import com.chess.dto.MoveRequestDto;
import com.chess.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for the chess game.
 */
@RestController
@RequestMapping("/api/game")
public class ChessController {

    private final GameService gameService;

    public ChessController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Initializes a new game and returns the board state and game status.
     */
    @GetMapping("/start")
    public GameResponseDto start() {
        return gameService.startNewGame();
    }

    /**
     * Returns valid destination coordinates for the piece at (x, y).
     * Query params: x (0–7), y (0–7).
     */
    @GetMapping("/legal-moves")
    public ResponseEntity<LegalMovesResponseDto> legalMoves(
            @RequestParam int x,
            @RequestParam int y
    ) {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            return ResponseEntity.badRequest().build();
        }
        LegalMovesResponseDto dto = gameService.getLegalMoves(x, y);
        return ResponseEntity.ok(dto);
    }

    /**
     * Executes a move and returns the updated board state and game status.
     * Body: { "start": { "x": 0, "y": 0 }, "end": { "x": 1, "y": 1 }, "promotion": "QUEEN" (optional) }
     */
    @PostMapping("/move")
    public ResponseEntity<?> move(@RequestBody MoveRequestDto request) {
        try {
            GameResponseDto response = gameService.applyMove(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
