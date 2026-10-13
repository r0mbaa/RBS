package ru.rbs.testovoe.response;

import ru.rbs.testovoe.enums.RoomType;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        RoomType room,
        String title,
        String organizer,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt
) {
}
