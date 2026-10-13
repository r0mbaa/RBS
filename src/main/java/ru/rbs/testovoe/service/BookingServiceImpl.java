package ru.rbs.testovoe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rbs.testovoe.entity.Booking;
import ru.rbs.testovoe.enums.RoomType;
import ru.rbs.testovoe.exception.BookingConflictException;
import ru.rbs.testovoe.exception.BookingNotFoundException;
import ru.rbs.testovoe.exception.InvalidBookingTimeException;
import ru.rbs.testovoe.mapper.BookingMapper;
import ru.rbs.testovoe.repository.BookingRepository;
import ru.rbs.testovoe.request.BookingRequest;
import ru.rbs.testovoe.response.BookingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse create(BookingRequest request) {
        validateTime(request);

        if (bookingRepository.existsByRoomAndStartAtBeforeAndEndAtAfter(
                request.room(), request.endAt(), request.startAt())) {
            throw new BookingConflictException(request.room());
        }

        Booking booking = bookingMapper.toEntity(request);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return bookingMapper.toResponse(findBooking(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAll(RoomType room, LocalDate date) {
        List<Booking> bookings;

        if (date == null) {
            bookings = room == null
                    ? bookingRepository.findAllByOrderByStartAtAsc()
                    : bookingRepository.findAllByRoomOrderByStartAtAsc(room);
        } else {
            // брони, которые пересекаются с днём [date 00:00, date+1 00:00)
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            bookings = room == null
                    ? bookingRepository.findAllByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(dayEnd, dayStart)
                    : bookingRepository.findAllByRoomAndStartAtBeforeAndEndAtAfterOrderByStartAtAsc(room, dayEnd, dayStart);
        }

        return bookingMapper.toResponseList(bookings);
    }

    @Override
    @Transactional
    public BookingResponse update(Long id, BookingRequest request) {
        Booking booking = findBooking(id);
        validateTime(request);

        // сама изменяемая бронь не считается пересечением
        if (bookingRepository.existsByRoomAndStartAtBeforeAndEndAtAfterAndIdNot(
                request.room(), request.endAt(), request.startAt(), id)) {
            throw new BookingConflictException(request.room());
        }

        bookingMapper.updateEntity(request, booking);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new BookingNotFoundException(id);
        }
        bookingRepository.deleteById(id);
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    private void validateTime(BookingRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new InvalidBookingTimeException();
        }
    }
}
