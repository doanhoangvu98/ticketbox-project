package demo.javacore.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeatTest {

    @Test
    void recordsWithTheSameValuesAreEqual() {
        Seat a = new Seat("A1", SeatType.VIP, new BigDecimal("150000"));
        Seat b = new Seat("A1", SeatType.VIP, new BigDecimal("150000"));

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.row()).isEqualTo('A');
    }

    @ParameterizedTest
    @ValueSource(strings = {"a1", "12", "A", "AA1"})
    void compactConstructorRejectsInvalidCodes(String code) {
        assertThatThrownBy(() -> new Seat(code, SeatType.STANDARD, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
