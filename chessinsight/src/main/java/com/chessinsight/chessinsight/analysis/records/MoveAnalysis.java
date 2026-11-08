package com.chessinsight.chessinsight.analysis.records;

import java.util.List;

public record MoveAnalysis(
    int ply,
    String playedMove,
    String mover,
    String engineBestMove,
    Integer centipawnLoss,
    String classification,
    PositionEvaluation positionEvaluation,
    List<Candidate> candidateLines
) {}
