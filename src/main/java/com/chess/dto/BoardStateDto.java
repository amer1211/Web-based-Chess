package com.chess.dto;

/**
 * Board state as an 8×8 grid. Rows are indexed 0–7 (rank); columns 0–7 (file).
 * Each cell is a piece code: "wP" (White Pawn), "bK" (Black King), etc., or "" for empty.
 */
public class BoardStateDto {

    /**
     * Grid [row][col] = piece code or "". row 0 = Black's back rank, row 7 = White's back rank.
     */
    private final String[][] squares;

    public BoardStateDto(String[][] squares) {
        this.squares = squares;
    }

    public String[][] getSquares() {
        return squares;
    }
}
