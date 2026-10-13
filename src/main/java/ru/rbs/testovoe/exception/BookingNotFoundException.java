package ru.rbs.testovoe.exception;

import org.springframework.http.HttpStatus;

public class BookingNotFoundException extends ApiException {

    public BookingNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND, String.valueOf(id));
    }

}
