package demo.javacore.model;

import java.math.BigDecimal;
import java.util.List;

/** Test data shared by several tests: row A is VIP, rows B and C are standard. */
public final class SampleSeats {

    private SampleSeats() {
    }

    public static List<Seat> all() {
        return List.of(
                new Seat("A1", SeatType.VIP, new BigDecimal("150000")),
                new Seat("A2", SeatType.VIP, new BigDecimal("150000")),
                new Seat("B1", SeatType.STANDARD, new BigDecimal("100000")),
                new Seat("B2", SeatType.STANDARD, new BigDecimal("100000")),
                new Seat("C1", SeatType.STANDARD, new BigDecimal("100000")));
    }
}
