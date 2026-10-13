package ru.rbs.testovoe.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rbs.testovoe.enums.RoomType;
import ru.rbs.testovoe.exception.BookingConflictException;
import ru.rbs.testovoe.request.BookingRequest;
import ru.rbs.testovoe.response.BookingResponse;
import ru.rbs.testovoe.service.BookingService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 12, 1, 10, 0);
    private static final LocalDateTime END = START.plusHours(1);

    private static final String VALID_BODY = """
            {
              "room": "VEGA",
              "title": "Планёрка",
              "organizer": "Роман",
              "startAt": "2030-12-01T10:00:00",
              "endAt": "2030-12-01T11:00:00"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    @DisplayName("POST: 201 и JSON созданной брони")
    void create_returns201AndBooking() throws Exception {
        BookingRequest expectedRequest = new BookingRequest(RoomType.VEGA, "Планёрка", "Роман", START, END);
        when(bookingService.create(expectedRequest)).thenReturn(
                new BookingResponse(1L, RoomType.VEGA, "Планёрка", "Роман", START, END, START.minusDays(1)));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.room").value("VEGA"))
                .andExpect(jsonPath("$.startAt").value("2030-12-01T10:00:00"))
                .andExpect(jsonPath("$.endAt").value("2030-12-01T11:00:00"));

        verify(bookingService).create(expectedRequest);
    }

    @Test
    @DisplayName("POST: 400 с ошибками по полям, если не прошла валидация; сервис не вызывается")
    void create_returns400_whenValidationFails() throws Exception {
        String body = """
                {
                  "room": "VEGA",
                  "title": "",
                  "organizer": "Роман",
                  "startAt": "2030-12-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VA0001"))
                .andExpect(jsonPath("$.details.fields.title").exists())
                .andExpect(jsonPath("$.details.fields.endAt").exists());

        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("POST: 409 в едином формате ошибки, если комната занята")
    void create_returns409_whenRoomIsBusy() throws Exception {
        when(bookingService.create(any())).thenThrow(new BookingConflictException(RoomType.VEGA));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BK0002"))
                .andExpect(jsonPath("$.message").value("Комната VEGA уже забронирована на это время"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
