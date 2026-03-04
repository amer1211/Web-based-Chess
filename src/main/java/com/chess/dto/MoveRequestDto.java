package com.chess.dto;

/**
 * Request body for POST /api/game/move.
 */
public record MoveRequestDto(CoordinateDto start, CoordinateDto end, String promotion) {

    /**
     * Optional promotion piece: "QUEEN", "ROOK", "BISHOP", "KNIGHT". Ignored for non-promotion moves.
     */
    public String promotion() {
        return promotion == null || promotion.isBlank() ? null : promotion.trim().toUpperCase();
    }
}
