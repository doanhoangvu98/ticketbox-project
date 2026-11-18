package com.noah.ticketbox.api.seat;

import com.noah.ticketbox.api.event.EventEntity;
import com.noah.ticketbox.api.event.EventRepository;
import com.noah.ticketbox.api.seat.dto.SeatResponse;
import com.noah.ticketbox.api.venue.VenueEntity;
import com.noah.ticketbox.core.domain.SeatPosition;
import com.noah.ticketbox.core.domain.SeatStatus;
import com.noah.ticketbox.core.domain.SeatType;
import com.noah.ticketbox.core.domain.StandardSeat;
import com.noah.ticketbox.core.domain.VipSeat;
import com.noah.ticketbox.core.exception.ErrorCode;
import com.noah.ticketbox.core.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test: no Spring context, no database. Repositories are Mockito mocks.
 */
@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    private static final BigDecimal BASE_PRICE = new BigDecimal("100000");

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventRepository eventRepository;

    private SeatService seatService;
    private EventEntity event;

    @BeforeEach
    void setUp() {
        seatService = new SeatService(seatRepository, eventRepository);
        VenueEntity venue = new VenueEntity("Test Hall", "1 Test Street", 2, 2);
        event = new EventEntity("Show", venue, OffsetDateTime.now().plusMonths(1), BASE_PRICE, 1);
    }

    @Test
    void unknownEvent_throwsNotFound_andDoesNotQuerySeats() {
        when(eventRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> seatService.listSeats(99L, null))
                .isInstanceOfSatisfying(NotFoundException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(ErrorCode.EVENT_NOT_FOUND));
        verifyNoInteractions(seatRepository);
    }

    @Test
    void withoutStatus_returnsEverySeat_mappedToDto() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(1L)).thenReturn(List.of(
                SeatEntity.of(event, new VipSeat(SeatPosition.parse("A1"), BASE_PRICE)),
                SeatEntity.of(event, new StandardSeat(SeatPosition.parse("B1"), BASE_PRICE))));

        List<SeatResponse> seats = seatService.listSeats(1L, null);

        assertThat(seats).extracting(SeatResponse::code).containsExactly("A1", "B1");
        assertThat(seats.get(0).type()).isEqualTo(SeatType.VIP);
        assertThat(seats.get(0).price()).isEqualByComparingTo("150000"); // VIP price computed by ticketbox-core
        assertThat(seats).allSatisfy(seat -> assertThat(seat.status()).isEqualTo(SeatStatus.AVAILABLE));
        verify(seatRepository, never()).findByEventIdAndStatusOrderByRowLabelAscSeatNumberAsc(anyLong(), any());
    }

    @Test
    void withStatus_usesTheFilteredQuery() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(seatRepository.findByEventIdAndStatusOrderByRowLabelAscSeatNumberAsc(1L, SeatStatus.SOLD))
                .thenReturn(List.of());

        assertThat(seatService.listSeats(1L, SeatStatus.SOLD)).isEmpty();
        verify(seatRepository, never()).findByEventIdOrderByRowLabelAscSeatNumberAsc(anyLong());
    }
}
