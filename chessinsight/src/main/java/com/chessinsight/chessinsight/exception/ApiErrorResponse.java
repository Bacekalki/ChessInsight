package com.chessinsight.chessinsight.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    int status,
    String error,
    String message,
    LocalDateTime timestamp
) {
}
