package com.chessinsight.chessinsight.analysis.records;

import java.util.List;

public record GameAnalysisResponse(
    double whiteAccuracy,
    double blackAccuracy,
    PositionEvaluation initialPositionEvaluation,
    List<MoveAnalysis> moves
) {}
