package com.chess.dto;

/**
 * JSON-serializable coordinate on the board (0–7 for x and y).
 */
public record CoordinateDto(int x, int y) {}
