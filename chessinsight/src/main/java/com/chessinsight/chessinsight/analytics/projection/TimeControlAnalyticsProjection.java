package com.chessinsight.chessinsight.analytics.projection;

public interface TimeControlAnalyticsProjection {

    String getTimeControl();

    Long getTotalGames();

    Long getWins();

    Long getLosses();

    Long getDraws();
}
