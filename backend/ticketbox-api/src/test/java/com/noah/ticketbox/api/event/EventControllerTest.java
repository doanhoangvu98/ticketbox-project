package com.noah.ticketbox.api.event;

import com.noah.ticketbox.api.event.dto.EventResponse;
import com.noah.ticketbox.api.event.dto.EventSearchCriteria;
import com.noah.ticketbox.api.event.dto.EventSummaryResponse;
import com.noah.ticketbox.core.domain.EventStatus;
import com.noah.ticketbox.core.domain.SeatStatus;
import com.noah.ticketbox.core.exception.InvalidStateException;
import com.noah.ticketbox.core.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice test: only the MVC layer (controller + exception handler + JSON) is started.
 * Checks the HTTP contract: status codes and error body format.
 */
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void create_withInvalidBody_returns400WithFieldErrors() throws Exception {
        String body = """
            { "name": "", "startsAt": "2020-01-01T00:00:00Z", "basePrice": 5, "vipRows": 0 }
            """;

        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
            // name blank, venueId null, startsAt in the past, basePrice too small
            .andExpect(jsonPath("$.errors", hasSize(4)));

        verifyNoInteractions(eventService);
    }

    @Test
    void get_unknownEvent_returns404ProblemDetail() throws Exception {
        when(eventService.get(99L)).thenThrow(NotFoundException.event(99L));

        mockMvc.perform(get("/api/v1/events/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("EVENT_NOT_FOUND"))
            .andExpect(jsonPath("$.type").value("https://ticketbox.dev/errors/event-not-found"));
    }

    @Test
    void publish_invalidState_returns409() throws Exception {
        when(eventService.publish(1L)).thenThrow(new InvalidStateException("Only DRAFT events can be published"));

        mockMvc.perform(post("/api/v1/events/1/publish"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("INVALID_STATE"));
    }

    @Test
    void create_validBody_returns201WithLocationHeader() throws Exception {
        when(eventService.create(any())).thenReturn(sampleEvent(42L));
        String body = """
            { "name": "Rock Night", "venueId": 1, "startsAt": "2030-01-01T20:00:00+07:00",
              "basePrice": 200000, "vipRows": 2 }
            """;

        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/v1/events/42")))
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void search_usesDefaultPaging_andReturnsTheStablePageShape() throws Exception {
        EventSummaryResponse summary = new EventSummaryResponse(1L, "Rock Night", "Test Hall",
            OffsetDateTime.parse("2030-01-01T20:00:00+07:00"), new BigDecimal("200000"), EventStatus.PUBLISHED);
        when(eventService.search(any(), any()))
            .thenAnswer(call -> new PageImpl<>(List.of(summary), call.getArgument(1, Pageable.class), 1));

        mockMvc.perform(get("/api/v1/events").param("status", "PUBLISHED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("Rock Night"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.totalItems").value(1))
            .andExpect(jsonPath("$.hasNext").value(false));

        ArgumentCaptor<EventSearchCriteria> criteria = ArgumentCaptor.forClass(EventSearchCriteria.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(eventService).search(criteria.capture(), pageable.capture());
        assertThat(criteria.getValue().status()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20); // @PageableDefault
        assertThat(pageable.getValue().getSort().getOrderFor("startsAt"))
            .isNotNull()
            .extracting(Sort.Order::getDirection)
            .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void search_pageSizeIsCappedAt100() throws Exception {
        when(eventService.search(any(), any()))
            .thenAnswer(call -> new PageImpl<>(List.of(), call.getArgument(1, Pageable.class), 0));

        mockMvc.perform(get("/api/v1/events").param("size", "1000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(100)); // spring.data.web.pageable.max-page-size
    }

    private static EventResponse sampleEvent(long id) {
        return new EventResponse(id, "Rock Night", new EventResponse.VenueInfo(1L, "Test Hall", "1 Test Street"),
            OffsetDateTime.parse("2030-01-01T20:00:00+07:00"), new BigDecimal("200000"), 2, EventStatus.DRAFT,
            100, Map.of(SeatStatus.AVAILABLE, 100L), 0L,
            Instant.parse("2026-11-18T00:00:00Z"), Instant.parse("2026-11-18T00:00:00Z"));
    }
}
