package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    public Knight(Color color, Coordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public List<Coordinate> calculatePseudoLegalMoves(Board board) {
        int[][] offsets = {
                {1, 2}, {2, 1},
                {2, -1}, {1, -2},
                {-1, -2}, {-2, -1},
                {-2, 1}, {-1, 2}
        };

        List<Coordinate> legalMoves = new ArrayList<>();

        Coordinate from = getCoordinate();
        int startX = from.x();
        int startY = from.y();
        Color ownColor = getColor();

        for (int[] offset : offsets) {
            int x = startX + offset[0];
            int y = startY + offset[1];

            if (board.isOutOfBounds(x, y)) {
                continue;
            }

            Piece target = board.getPiece(x, y);
            if (target == null || target.getColor() != ownColor) {
                legalMoves.add(new Coordinate(x, y));
            }
        }

        return legalMoves;
    }
}
