package ru.rbs.testovoe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.rbs.testovoe.enums.RoomType;
import ru.rbs.testovoe.request.BookingRequest;
import ru.rbs.testovoe.response.BookingResponse;
import ru.rbs.testovoe.service.BookingService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request) {
        return bookingService.create(request);
    }

    @GetMapping("/{id}")
    public BookingResponse getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @GetMapping
    public List<BookingResponse> getAll(
            @RequestParam(required = false) RoomType room,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return bookingService.getAll(room, date);
    }

    @PutMapping("/{id}")
    public BookingResponse update(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        return bookingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookingService.delete(id);
    }

}
