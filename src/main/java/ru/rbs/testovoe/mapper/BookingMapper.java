package ru.rbs.testovoe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.rbs.testovoe.entity.Booking;
import ru.rbs.testovoe.request.BookingRequest;
import ru.rbs.testovoe.response.BookingResponse;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookingMapper {

    // id генерирует база, createdAt проставляет @PrePersist
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking toEntity(BookingRequest request);

    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    // Редактирование id и createdAt не трогает
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(BookingRequest request, @MappingTarget Booking booking);

}
