package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

    public Queen(Color color, Coordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public List<Coordinate> calculatePseudoLegalMoves(Board board) {
        int[][] directions = {
                {1, 0}, {-1, 0},
                {0, 1}, {0, -1},
                {-1, -1}, {-1, 1},
                {1, -1}, {1, 1}
        };

        List<Coordinate> legalMoves = new ArrayList<>();

        Coordinate from = getCoordinate();
        int startX = from.x();
        int startY = from.y();
        Color ownColor = getColor();

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            int x = startX + dx;
            int y = startY + dy;

            while (!board.isOutOfBounds(x, y)) {
                Piece target = board.getPiece(x, y);

                if (target == null) {
                    legalMoves.add(new Coordinate(x, y));
                } else {
                    if (target.getColor() != ownColor) {
                        legalMoves.add(new Coordinate(x, y));
                    }
                    break;
                }

                x += dx;
                y += dy;
            }
        }

        return legalMoves;
    }
}
