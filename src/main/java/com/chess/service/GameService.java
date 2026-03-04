package com.chess.service;

import com.chess.dto.CoordinateDto;
import com.chess.dto.GameResponseDto;
import com.chess.dto.LegalMovesResponseDto;
import com.chess.dto.MoveRequestDto;
import com.chess.model.Coordinate;
import com.chess.model.GameEngine;
import com.chess.model.Move;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds the current game state and exposes operations for the API.
 */
@Service
public class GameService {

    private GameEngine engine;

    public GameService() {
        this.engine = new GameEngine();
    }

    /**
     * Initializes a new game and returns the board state and status.
     */
    public GameResponseDto startNewGame() {
        this.engine = new GameEngine();
        return toGameResponse();
    }

    /**
     * Returns the current board state and game status (e.g. after start or move).
     */
    public GameResponseDto getCurrentState() {
        return toGameResponse();
    }

    /**
     * Returns valid destination coordinates for the piece at (x, y), or empty list if none/no piece.
     */
    public LegalMovesResponseDto getLegalMoves(int x, int y) {
        if (engine.getBoard().isOutOfBounds(x, y)) {
            return new LegalMovesResponseDto(List.of());
        }
        var piece = engine.getBoard().getPiece(x, y);
        if (piece == null) {
            return new LegalMovesResponseDto(List.of());
        }
        boolean isCurrentPlayer = (engine.isWhiteTurn() && piece.getColor() == com.chess.model.Color.WHITE)
                || (!engine.isWhiteTurn() && piece.getColor() == com.chess.model.Color.BLACK);
        if (!isCurrentPlayer) {
            return new LegalMovesResponseDto(List.of());
        }

        List<Move> legalMoves = engine.generateStrictLegalMoves();
        List<CoordinateDto> destinations = legalMoves.stream()
                .filter(m -> m.getStart().x() == x && m.getStart().y() == y)
                .map(m -> new CoordinateDto(m.getEnd().x(), m.getEnd().y()))
                .distinct()
                .collect(Collectors.toList());

        return new LegalMovesResponseDto(destinations);
    }

    /**
     * Applies the move described by the request and returns updated board state and status.
     *
     * @throws IllegalArgumentException if no piece at start, or move is not legal
     */
    public GameResponseDto applyMove(MoveRequestDto request) {
        if (request == null || request.start() == null || request.end() == null) {
            throw new IllegalArgumentException("Move must have start and end coordinates");
        }
        Coordinate start = new Coordinate(request.start().x(), request.start().y());
        Coordinate end = new Coordinate(request.end().x(), request.end().y());

        Move move = findMatchingLegalMove(start, end, request.promotion());
        if (move == null) {
            throw new IllegalArgumentException("No legal move from (" + start.x() + "," + start.y() + ") to (" + end.x() + "," + end.y() + ")");
        }

        engine.applyMove(move);
        return toGameResponse();
    }

    private Move findMatchingLegalMove(Coordinate start, Coordinate end, String promotion) {
        List<Move> legal = engine.generateStrictLegalMoves();
        Move.PromotionType requestedPromo = parsePromotionType(promotion);

        for (Move m : legal) {
            if (!m.getStart().equals(start) || !m.getEnd().equals(end)) {
                continue;
            }
            if (m.getPromotionType() == Move.PromotionType.NONE) {
                return m;
            }
            if (requestedPromo == null || requestedPromo == Move.PromotionType.NONE) {
                return m; // use first promotion option (e.g. queen) as default
            }
            if (m.getPromotionType() == requestedPromo) {
                return m;
            }
        }
        return null;
    }

    private static Move.PromotionType parsePromotionType(String promotion) {
        if (promotion == null || promotion.isBlank()) {
            return Move.PromotionType.NONE;
        }
        return switch (promotion.trim().toUpperCase()) {
            case "QUEEN" -> Move.PromotionType.QUEEN;
            case "ROOK" -> Move.PromotionType.ROOK;
            case "BISHOP" -> Move.PromotionType.BISHOP;
            case "KNIGHT" -> Move.PromotionType.KNIGHT;
            default -> Move.PromotionType.NONE;
        };
    }

    private GameResponseDto toGameResponse() {
        boolean checkmate = engine.isCheckmate();
        boolean stalemate = engine.isStalemate();
        boolean gameOver = checkmate || stalemate;
        String message = gameOver
                ? (checkmate ? "Checkmate." : "Stalemate.")
                : null;

        return new GameResponseDto(
                BoardMapper.toBoardStateDto(engine.getBoard()),
                engine.isWhiteTurn(),
                gameOver,
                checkmate,
                stalemate,
                message
        );
    }
}
