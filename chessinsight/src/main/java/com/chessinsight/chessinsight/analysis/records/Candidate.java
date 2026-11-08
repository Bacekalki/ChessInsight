package com.chessinsight.chessinsight.analysis.records;

import java.util.List;

public record Candidate(
    int rank,
    int depth,
    String bestMove,
    List<String> principalVariation,
    Integer centipawns,
    Integer mateIn) {
}
