package ru.rbs.testovoe.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    // 400 404 409 из сервиса
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return build(ex.getStatus(), ex.getErrorCode(), null, ex.getArgs());
    }

    // @Valid на теле
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), messageSource.getMessage(error, LocaleContextHolder.getLocale()));
        }
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, Map.of("fields", fields));
    }

    // Битый JSON
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_BAD_REQUEST, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        // Стандартные ошибки Spring MVC
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            return build(springError.getStatusCode(), ErrorCode.REQUEST_NOT_SUPPORTED, null);
        }
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatusCode status, String code,
                                                Map<String, Object> details, Object... args) {
        String message = messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
        return ResponseEntity.status(status).body(ErrorResponse.of(code, message, details));
    }
}
