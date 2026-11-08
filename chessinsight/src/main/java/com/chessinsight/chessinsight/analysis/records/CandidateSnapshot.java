package com.chessinsight.chessinsight.analysis.records;

import java.util.List;

public record CandidateSnapshot(
    int rank,
    int depth,
    Integer centipawns,
    Integer mateIn,
    List<String> principalVariation) {
}
