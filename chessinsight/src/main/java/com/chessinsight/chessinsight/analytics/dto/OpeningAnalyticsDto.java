package com.chessinsight.chessinsight.analytics.dto;

public record OpeningAnalyticsDto(
    String openingName,
    long totalGames,
    long wins,
    long losses,
    long draws,
    double winRate
) {}