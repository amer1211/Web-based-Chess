package com.chess.model;

import java.util.List;

/**
 * Abstract base for all chess pieces.
 */
public abstract class Piece {

    private final Color color;
    private Coordinate coordinate;
    private boolean isFirstMove;

    protected Piece(Color color, Coordinate coordinate) {
        this.color = color;
        this.coordinate = coordinate;
        this.isFirstMove = true;
    }

    public Color getColor() {
        return color;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public boolean isFirstMove() {
        return isFirstMove;
    }

    public void setFirstMove(boolean firstMove) {
        isFirstMove = firstMove;
    }

    /**
     * Calculates all pseudo-legal moves for this piece on the given board.
     * Does not consider whether the move would leave the own King in check.
     *
     * @param board the current board state
     * @return list of destination coordinates that are pseudo-legal
     */
    public abstract List<Coordinate> calculatePseudoLegalMoves(Board board);
}
