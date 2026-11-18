package com.noah.ticketbox.api.seat;

import com.noah.ticketbox.api.seat.dto.SeatResponse;
import com.noah.ticketbox.core.domain.SeatStatus;
import com.noah.ticketbox.core.domain.SeatType;
import com.noah.ticketbox.core.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice test: controller + JSON + exception handler, the service is a mock.
 */
@WebMvcTest(SeatController.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeatService seatService;

    @Test
    void list_returnsSeats_withoutExposingWhoHoldsThem() throws Exception {
        when(seatService.listSeats(1L, SeatStatus.AVAILABLE)).thenReturn(List.of(
                new SeatResponse("A1", "A", 1, SeatType.VIP, new BigDecimal("150000"), SeatStatus.AVAILABLE)));

        mockMvc.perform(get("/api/v1/events/1/seats").param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("A1"))
                .andExpect(jsonPath("$[0].type").value("VIP"))
                .andExpect(jsonPath("$[0].price").value(150000))
                .andExpect(jsonPath("$[0].heldBy").doesNotExist());
    }

    @Test
    void list_unknownEvent_returns404() throws Exception {
        when(seatService.listSeats(99L, null)).thenThrow(NotFoundException.event(99L));

        mockMvc.perform(get("/api/v1/events/99/seats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("EVENT_NOT_FOUND"));
    }

    @Test
    void list_unknownStatusValue_returns400_not500() throws Exception {
        mockMvc.perform(get("/api/v1/events/1/seats").param("status", "BROKEN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(seatService);
    }
}
