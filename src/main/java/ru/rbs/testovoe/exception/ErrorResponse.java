package ru.rbs.testovoe.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        Map<String, Object> details,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(String code, String message) {
        return of(code, message, null);
    }

    public static ErrorResponse of(String code, String message, Map<String, Object> details) {
        return new ErrorResponse(code, message, details, LocalDateTime.now());
    }

}
