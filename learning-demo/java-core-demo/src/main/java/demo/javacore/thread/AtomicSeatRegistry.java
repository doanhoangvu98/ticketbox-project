package demo.javacore.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Fix #2: one atomic operation instead of two separate ones.
 *
 * <p>{@code putIfAbsent} checks and inserts in a single step inside ConcurrentHashMap,
 * without blocking threads that work on other seats.
 */
public class AtomicSeatRegistry implements SeatRegistry {

    private final ConcurrentMap<String, String> holders = new ConcurrentHashMap<>();

    @Override
    public boolean tryHold(String seatCode, String userId) {
        return holders.putIfAbsent(seatCode, userId) == null; // null = nobody had the seat before
    }

    @Override
    public String holderOf(String seatCode) {
        return holders.get(seatCode);
    }
}
