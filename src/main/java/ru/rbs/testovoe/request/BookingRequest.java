package ru.rbs.testovoe.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.rbs.testovoe.enums.RoomType;

import java.time.LocalDateTime;

public record BookingRequest(
        @NotNull
        RoomType room,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 100)
        String organizer,

        @NotNull
        @FutureOrPresent
        LocalDateTime startAt,

        @NotNull
        LocalDateTime endAt
) {
}
