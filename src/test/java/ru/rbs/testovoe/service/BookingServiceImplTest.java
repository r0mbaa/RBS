package ru.rbs.testovoe.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rbs.testovoe.entity.Booking;
import ru.rbs.testovoe.enums.RoomType;
import ru.rbs.testovoe.exception.BookingConflictException;
import ru.rbs.testovoe.exception.BookingNotFoundException;
import ru.rbs.testovoe.exception.InvalidBookingTimeException;
import ru.rbs.testovoe.mapper.BookingMapper;
import ru.rbs.testovoe.mapper.BookingMapperImpl;
import ru.rbs.testovoe.repository.BookingRepository;
import ru.rbs.testovoe.request.BookingRequest;
import ru.rbs.testovoe.response.BookingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 12, 1, 10, 0);
    private static final LocalDateTime END = START.plusHours(1);

    @Mock
    private BookingRepository bookingRepository;

    @Spy
    private BookingMapper bookingMapper = new BookingMapperImpl();

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    @DisplayName("create: сохраняет бронь, если комната свободна")
    void create_savesBooking_whenNoOverlap() {
        BookingRequest request = request(RoomType.VEGA, START, END);
        when(bookingRepository.existsByRoomAndStartAtBeforeAndEndAtAfter(RoomType.VEGA, END, START))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        BookingResponse response = bookingService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.room()).isEqualTo(RoomType.VEGA);
        assertThat(response.title()).isEqualTo("Планёрка");
        assertThat(response.startAt()).isEqualTo(START);
        assertThat(response.endAt()).isEqualTo(END);
    }

    @Test
    @DisplayName("create: 409, если интервал пересекается с другой бронью этой комнаты")
    void create_throwsConflict_whenOverlap() {
        BookingRequest request = request(RoomType.VEGA, START, END);
        // endAt первым: startAt < :endAt и endAt > :startAt
        when(bookingRepository.existsByRoomAndStartAtBeforeAndEndAtAfter(RoomType.VEGA, END, START))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(BookingConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: 400, если endAt не позже startAt, в базу не ходим")
    void create_throwsInvalidTime_whenEndNotAfterStart() {
        BookingRequest request = request(RoomType.VEGA, START, START);

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(InvalidBookingTimeException.class);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("update: проверяет пересечение без самой брони, id и createdAt не меняются")
    void update_excludesItselfFromOverlapCheck_andKeepsIdAndCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.of(2030, 11, 1, 9, 0);
        Booking existing = new Booking();
        existing.setId(5L);
        existing.setRoom(RoomType.ORION);
        existing.setTitle("Старое название");
        existing.setOrganizer("Иван");
        existing.setStartAt(START);
        existing.setEndAt(END);
        existing.setCreatedAt(createdAt);

        LocalDateTime newEnd = END.plusMinutes(30);
        BookingRequest request = request(RoomType.ORION, START, newEnd);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(bookingRepository.existsByRoomAndStartAtBeforeAndEndAtAfterAndIdNot(RoomType.ORION, newEnd, START, 5L))
                .thenReturn(false);
        when(bookingRepository.save(existing)).thenReturn(existing);

        BookingResponse response = bookingService.update(5L, request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.title()).isEqualTo("Планёрка");
        assertThat(response.endAt()).isEqualTo(newEnd);
    }

    @Test
    @DisplayName("getById: 404, если брони нет")
    void getById_throwsNotFound_whenMissing() {
        when(bookingRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(42L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    @DisplayName("getAll: фильтр по дню ищет пересечение с [00:00, 00:00 следующего дня)")
    void getAll_withRoomAndDate_usesWholeDayRange() {
        LocalDate date = LocalDate.of(2030, 12, 1);
        LocalDateTime dayStart = LocalDateTime.of(2030, 12, 1, 0, 0);
        LocalDateTime dayEnd = LocalDateTime.of(2030, 12, 2, 0, 0);
        when(bookingRepository.findAllByRoomAndStartAtBeforeAndEndAtAfterOrderByStartAtAsc(
                RoomType.SIRIUS, dayEnd, dayStart))
                .thenReturn(List.of());

        List<BookingResponse> result = bookingService.getAll(RoomType.SIRIUS, date);

        assertThat(result).isEmpty();
        verify(bookingRepository).findAllByRoomAndStartAtBeforeAndEndAtAfterOrderByStartAtAsc(
                RoomType.SIRIUS, dayEnd, dayStart);
    }

    private static BookingRequest request(RoomType room, LocalDateTime startAt, LocalDateTime endAt) {
        return new BookingRequest(room, "Планёрка", "Роман", startAt, endAt);
    }
}
