package com.chess.dto;

import java.util.List;

/**
 * Response for GET /api/game/legal-moves: list of valid destination coordinates.
 */
public class LegalMovesResponseDto {

    private final List<CoordinateDto> moves;

    public LegalMovesResponseDto(List<CoordinateDto> moves) {
        this.moves = moves;
    }

    public List<CoordinateDto> getMoves() {
        return moves;
    }
}
