package com.chess.model;

/**
 * Represents a square on the chessboard. x and y are in range 0 to 7.
 */
public record Coordinate(int x, int y) {

    public Coordinate {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            throw new IllegalArgumentException("Coordinates must be between 0 and 7, got (" + x + ", " + y + ")");
        }
    }
}
