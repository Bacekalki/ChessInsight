package com.chessinsight.chessinsight.analytics.dto;

public record OpponentStrengthDto(
    String category,
    long totalGames,
    long wins,
    long losses,
    long draws,
    double winRate
) {
}
