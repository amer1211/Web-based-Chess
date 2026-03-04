package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(Color color, Coordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public List<Coordinate> calculatePseudoLegalMoves(Board board) {
        List<Coordinate> legalMoves = new ArrayList<>();

        Coordinate from = getCoordinate();
        int x = from.x();
        int y = from.y();
        Color color = getColor();

        int direction = (color == Color.WHITE) ? -1 : 1;

        int forwardY = y + direction;
        if (!board.isOutOfBounds(x, forwardY) && board.getPiece(x, forwardY) == null) {
            legalMoves.add(new Coordinate(x, forwardY));

            int startRank = (color == Color.WHITE) ? 6 : 1;
            int twoForwardY = y + 2 * direction;
            if (y == startRank && isFirstMove()
                    && !board.isOutOfBounds(x, twoForwardY)
                    && board.getPiece(x, twoForwardY) == null) {
                legalMoves.add(new Coordinate(x, twoForwardY));
            }
        }

        int[] captureDx = {-1, 1};
        int captureY = y + direction;
        for (int dx : captureDx) {
            int captureX = x + dx;
            if (board.isOutOfBounds(captureX, captureY)) {
                continue;
            }
            Piece target = board.getPiece(captureX, captureY);
            if (target != null && target.getColor() != color) {
                legalMoves.add(new Coordinate(captureX, captureY));
            }
        }

        return legalMoves;
    }
}
