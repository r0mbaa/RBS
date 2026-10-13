package ru.rbs.testovoe.service;

import org.springframework.stereotype.Service;
import ru.rbs.testovoe.enums.RoomType;
import ru.rbs.testovoe.request.BookingRequest;
import ru.rbs.testovoe.response.BookingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    @Override
    public BookingResponse create(BookingRequest request) {
        //  проверить endAt > startAt (иначе 400),
        //  проверить пересечение с другими бронями этой комнаты (иначе 409),
        //  сохранить в репозиторий с createdAt = now
        return new BookingResponse(
                1L,
                request.room(),
                request.title(),
                request.organizer(),
                request.startAt(),
                request.endAt(),
                LocalDateTime.now()
        );
    }

    @Override
    public BookingResponse getById(Long id) {
        // найти в репозитории, если нет 404
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        return new BookingResponse(
                id,
                RoomType.ORION,
                "Заглушка",
                "Организатор",
                start,
                start.plusHours(1),
                LocalDateTime.now()
        );
    }

    @Override
    public List<BookingResponse> getAll(RoomType room, LocalDate date) {
        return List.of();
    }

    @Override
    public BookingResponse update(Long id, BookingRequest request) {
        // тоже самое что и создать ток над найти запись еще
        return new BookingResponse(
                id,
                request.room(),
                request.title(),
                request.organizer(),
                request.startAt(),
                request.endAt(),
                LocalDateTime.now()
        );
    }

    @Override
    public void delete(Long id) {
        // брони нет 404, иначе удалить из репо
    }
}
