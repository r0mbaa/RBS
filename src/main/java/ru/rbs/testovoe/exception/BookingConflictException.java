package ru.rbs.testovoe.exception;

import org.springframework.http.HttpStatus;
import ru.rbs.testovoe.enums.RoomType;

public class BookingConflictException extends ApiException {

    public BookingConflictException(RoomType room) {
        super(HttpStatus.CONFLICT, ErrorCode.BOOKING_CONFLICT, room);
    }

}
