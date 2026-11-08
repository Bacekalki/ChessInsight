package com.chessinsight.chessinsight.analytics.projection;

public interface OpeningAnalyticsProjection {
    String getOpeningName();
    Long getTotalGames();
    Long getWins();
    Long getLosses();
    Long getDraws();
}
