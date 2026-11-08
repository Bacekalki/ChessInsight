package com.chessinsight.chessinsight.chess.pgn;

public record PgnImportReport(
    int imported,
    int skipped,
    int malformed,
    int totalProcessed
) {}