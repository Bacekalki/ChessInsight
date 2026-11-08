package com.chessinsight.chessinsight.analysis.records;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AnalysisRequest(
    @NotBlank(message = "FEN must not be blank")
    String fen,

    @Min(value = 1, message = "Depth must be at least 1")
    @Max(value = 20, message = "Depth must not exceed 20")
    Integer depth
) {
}
