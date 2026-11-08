package com.chessinsight.chessinsight.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
        ResponseStatusException exception) {

        ApiErrorResponse error = new ApiErrorResponse(
            exception.getStatusCode().value(),
            HttpStatus.valueOf(exception.getStatusCode().value())
                .getReasonPhrase(),
            exception.getReason(),
            LocalDateTime.now()
        );

        return ResponseEntity
            .status(exception.getStatusCode())
            .body(error);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
        MissingServletRequestParameterException exception) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Missing required parameter: " + exception.getParameterName(),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Invalid value for parameter: " + exception.getName(),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestPart(
        MissingServletRequestPartException exception) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Missing required part: " + exception.getRequestPartName(),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

}
