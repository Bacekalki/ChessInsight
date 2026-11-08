package com.chessinsight.chessinsight.analytics.dto;

public record ColorPerformanceDto(
    String color,
    long totalGames,
    long wins,
    long losses,
    long draws,
    double winRate
) {
}
