package com.chessinsight.chessinsight.analytics.dto;

public record TimeControlAnalyticsDto(
    String timeControl,
    Long totalGames,
    Long wins,
    Long losses,
    Long draws,
    double winRate
) {
}
