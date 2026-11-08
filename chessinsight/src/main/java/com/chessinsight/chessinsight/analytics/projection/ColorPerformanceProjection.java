package com.chessinsight.chessinsight.analytics.projection;

public interface ColorPerformanceProjection {

    String getColor();

    long getTotalGames();

    long getWins();

    long getLosses();

    long getDraws();
}
