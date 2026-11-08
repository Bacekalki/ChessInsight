package com.chessinsight.chessinsight.analytics.projection;

public interface OpponentStrengthProjection {
    String getCategory();

    long getTotalGames();

    long getWins();

    long getLosses();

    long getDraws();
}
