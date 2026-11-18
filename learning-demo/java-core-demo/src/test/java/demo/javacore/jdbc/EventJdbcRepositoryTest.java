package demo.javacore.jdbc;

import demo.javacore.jdbc.EventJdbcRepository.NewEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the plain JDBC code against H2, an in-memory database (no Docker needed).
 * Against the real PostgreSQL only the URL changes:
 * {@code new EventJdbcRepository("jdbc:postgresql://localhost:5433/ticketbox", "ticketbox", "ticketbox")}.
 */
class EventJdbcRepositoryTest {

    private EventJdbcRepository repository;

    @BeforeEach
    void setUp() {
        // A fresh database per test. DB_CLOSE_DELAY=-1 keeps it alive between our short-lived connections.
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        repository = new EventJdbcRepository(url, "sa", "");
        repository.createTable();
    }

    @Test
    void insert_returnsTheGeneratedId_andTheRowCanBeLoaded() {
        long id = repository.insert("Rock Night", new BigDecimal("200000"));

        EventRow row = repository.findById(id).orElseThrow();
        assertThat(row.name()).isEqualTo("Rock Night");
        assertThat(row.basePrice()).isEqualByComparingTo("200000");
        assertThat(repository.findById(id + 1000)).isEmpty();
    }

    @Test
    void findByNameContaining_isCaseInsensitive() {
        repository.insert("Rock Night", new BigDecimal("200000"));
        repository.insert("Jazz Evening", new BigDecimal("300000"));
        repository.insert("ROCK Legends", new BigDecimal("400000"));

        assertThat(repository.findByNameContaining("rock"))
                .extracting(EventRow::name)
                .containsExactly("Rock Night", "ROCK Legends");
    }

    @Test
    void preparedStatement_preventsSqlInjection() {
        repository.insert("Rock Night", new BigDecimal("200000"));

        // With string concatenation this input would turn the WHERE clause into "always true".
        assertThat(repository.findByNameContaining("' OR '1'='1")).isEmpty();
    }

    @Test
    void insertAll_savesEveryRowInOneTransaction() {
        repository.insertAll(List.of(
                new NewEvent("Rock Night", new BigDecimal("200000")),
                new NewEvent("Jazz Evening", new BigDecimal("300000"))));

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void insertAll_isAllOrNothing() {
        List<NewEvent> events = List.of(
                new NewEvent("Valid event", new BigDecimal("200000")),
                new NewEvent(null, new BigDecimal("300000"))); // violates NOT NULL

        assertThatThrownBy(() -> repository.insertAll(events))
                .isInstanceOf(JdbcException.class)
                .hasCauseInstanceOf(SQLException.class);
        assertThat(repository.count()).as("the valid row was rolled back too").isZero();
    }
}
