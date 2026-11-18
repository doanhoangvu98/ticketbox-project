package demo.javacore.thread;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 100 users try to hold the same seat at the same moment.
 */
class SeatRegistryRaceTest {

    private static final int USERS = 100;
    private static final long DELAY_MILLIS = 5;

    @Test
    void unsafe_givesTheSameSeatToSeveralUsers() throws InterruptedException {
        SeatRegistry registry = new UnsafeSeatRegistry(DELAY_MILLIS);

        int winners = RaceRunner.run(USERS, i -> registry.tryHold("A1", "user-" + i));

        // Probabilistic by nature, but with 32 threads and a 5 ms gap the race is practically certain.
        assertThat(winners).as("race condition: more than one winner").isGreaterThan(1);
    }

    @Test
    void synchronized_hasExactlyOneWinner() throws InterruptedException {
        SeatRegistry registry = new SynchronizedSeatRegistry(DELAY_MILLIS);

        int winners = RaceRunner.run(USERS, i -> registry.tryHold("A1", "user-" + i));

        assertThat(winners).isEqualTo(1);
        assertThat(registry.holderOf("A1")).startsWith("user-");
    }

    @Test
    void atomicPutIfAbsent_hasExactlyOneWinner() throws InterruptedException {
        SeatRegistry registry = new AtomicSeatRegistry();

        int winners = RaceRunner.run(USERS, i -> registry.tryHold("A1", "user-" + i));

        assertThat(winners).isEqualTo(1);
    }

    @Test
    void differentSeats_canAllBeHeldAtTheSameTime() throws InterruptedException {
        SeatRegistry registry = new AtomicSeatRegistry();

        int winners = RaceRunner.run(USERS, i -> registry.tryHold("seat-" + i, "user-" + i));

        assertThat(winners).isEqualTo(USERS);
    }
}
