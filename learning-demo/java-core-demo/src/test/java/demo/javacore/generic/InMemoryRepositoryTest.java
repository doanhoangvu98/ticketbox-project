package demo.javacore.generic;

import demo.javacore.model.SampleSeats;
import demo.javacore.model.Seat;
import demo.javacore.model.SeatType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRepositoryTest {

    /** A second, unrelated entity type: the same generic class stores it without any change. */
    record Customer(Long id, String name) implements Identifiable<Long> {
    }

    private final Repository<Seat, String> seats = new InMemoryRepository<>();

    @Test
    void saveAll_thenFindById() {
        seats.saveAll(SampleSeats.all());

        assertThat(seats.count()).isEqualTo(5);
        assertThat(seats.findById("B1")).map(Seat::type).contains(SeatType.STANDARD);
        assertThat(seats.findById("Z9")).isEmpty();
    }

    @Test
    void save_withTheSameId_replacesTheOldValue() {
        seats.save(new Seat("A1", SeatType.VIP, new BigDecimal("150000")));
        seats.save(new Seat("A1", SeatType.VIP, new BigDecimal("200000")));

        assertThat(seats.count()).isEqualTo(1);
        assertThat(seats.findById("A1").orElseThrow().price()).isEqualByComparingTo("200000");
    }

    @Test
    void findAll_acceptsAPredicateOfASupertype() {
        seats.saveAll(SampleSeats.all());
        // Predicate<Identifiable<String>> is not a Predicate<Seat>, but "? super Seat" accepts it.
        Predicate<Identifiable<String>> inRowA = item -> item.id().startsWith("A");

        assertThat(seats.findAll(inRowA)).extracting(Seat::code).containsExactly("A1", "A2");
    }

    @Test
    void findAll_returnsACopy() {
        seats.saveAll(SampleSeats.all());

        seats.findAll().clear();

        assertThat(seats.count()).isEqualTo(5);
    }

    @Test
    void theSameClassWorksForAnotherEntityType() {
        Repository<Customer, Long> customers = new InMemoryRepository<>();

        customers.save(new Customer(1L, "Alice"));

        assertThat(customers.findById(1L)).map(Customer::name).contains("Alice");
    }
}
