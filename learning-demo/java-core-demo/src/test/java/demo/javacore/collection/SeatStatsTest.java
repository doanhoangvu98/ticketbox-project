package demo.javacore.collection;

import demo.javacore.model.SampleSeats;
import demo.javacore.model.Seat;
import demo.javacore.model.SeatType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static demo.javacore.collection.SeatStats.cheaperThan;
import static demo.javacore.collection.SeatStats.isVip;
import static org.assertj.core.api.Assertions.assertThat;

class SeatStatsTest {

    private final List<Seat> seats = SampleSeats.all(); // A1, A2 VIP; B1, B2, C1 standard

    @Test
    void countByType() {
        Map<SeatType, Long> counts = SeatStats.countByType(seats);

        assertThat(counts).containsEntry(SeatType.VIP, 2L).containsEntry(SeatType.STANDARD, 3L);
    }

    @Test
    void byRow_isSortedByRow() {
        SortedMap<Character, List<Seat>> rows = SeatStats.byRow(seats);

        assertThat(rows.keySet()).containsExactly('A', 'B', 'C');
        assertThat(rows.get('A')).extracting(Seat::code).containsExactly("A1", "A2");
    }

    @Test
    void splitVip_hasBothTrueAndFalse() {
        Map<Boolean, List<Seat>> split = SeatStats.splitVip(seats);

        assertThat(split.get(true)).hasSize(2);
        assertThat(split.get(false)).hasSize(3);
    }

    @Test
    void totalPrice_withComposedPredicates() {
        assertThat(SeatStats.totalPrice(seats, seat -> true)).isEqualByComparingTo("600000");
        assertThat(SeatStats.totalPrice(seats, isVip())).isEqualByComparingTo("300000");
        // not VIP AND cheaper than 150k -> the three standard seats
        assertThat(SeatStats.totalPrice(seats, isVip().negate().and(cheaperThan(new BigDecimal("150000")))))
                .isEqualByComparingTo("300000");
    }

    @Test
    void distinctRows_removesDuplicatesAndSorts() {
        assertThat(SeatStats.distinctRows(seats)).containsExactly('A', 'B', 'C');
    }
}
