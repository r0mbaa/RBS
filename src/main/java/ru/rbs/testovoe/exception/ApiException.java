package ru.rbs.testovoe.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    private final String errorCode;

    private final transient Object[] args;

    protected ApiException(HttpStatus status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }

}
