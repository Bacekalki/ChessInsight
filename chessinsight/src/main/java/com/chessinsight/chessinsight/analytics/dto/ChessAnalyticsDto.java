package com.chessinsight.chessinsight.analytics.dto;

public record ChessAnalyticsDto(String username,
                                long totalGames,
                                long wins,
                                long losses,
                                long draws,
                                double winRate) {
}
