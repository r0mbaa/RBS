package ru.rbs.testovoe.exception;

import org.springframework.http.HttpStatus;

public class InvalidBookingTimeException extends ApiException {

    public InvalidBookingTimeException() {
        super(HttpStatus.BAD_REQUEST, ErrorCode.BOOKING_INVALID_TIME);
    }

}
