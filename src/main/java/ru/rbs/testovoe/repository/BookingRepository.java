package ru.rbs.testovoe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rbs.testovoe.entity.Booking;
import ru.rbs.testovoe.enums.RoomType;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // пересечение при создании
    boolean existsByRoomAndStartAtBeforeAndEndAtAfter(
            RoomType room, LocalDateTime endAt, LocalDateTime startAt);

    // пересечение при изменении, без самой этой брони
    boolean existsByRoomAndStartAtBeforeAndEndAtAfterAndIdNot(
            RoomType room, LocalDateTime endAt, LocalDateTime startAt, Long id);

    //4 комбинации необязательных фильтров
    List<Booking> findAllByOrderByStartAtAsc();

    List<Booking> findAllByRoomOrderByStartAtAsc(RoomType room);

    List<Booking> findAllByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(
            LocalDateTime dayEnd, LocalDateTime dayStart);

    List<Booking> findAllByRoomAndStartAtBeforeAndEndAtAfterOrderByStartAtAsc(
            RoomType room, LocalDateTime dayEnd, LocalDateTime dayStart);

}
