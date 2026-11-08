package com.chessinsight.chessinsight.analysis.records;

import java.util.List;

public record AnalysisResult(
    String fen,
    int depth,
    String bestMove,
    List<Candidate> candidates) {
}
