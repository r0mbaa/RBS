package ru.rbs.testovoe.service;

import ru.rbs.testovoe.enums.RoomType;
import ru.rbs.testovoe.request.BookingRequest;
import ru.rbs.testovoe.response.BookingResponse;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingResponse create(BookingRequest request);

    BookingResponse getById(Long id);

    //room и date необязательные
    List<BookingResponse> getAll(RoomType room, LocalDate date);

    BookingResponse update(Long id, BookingRequest request);

    void delete(Long id);

}
