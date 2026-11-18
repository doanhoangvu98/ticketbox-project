package demo.javacore.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WRONG on purpose: "check-then-act" in two separate steps.
 *
 * <p>Two threads can both pass the check before either one acts, so both "win" the same seat
 * (a race condition). A ConcurrentHashMap does not help: each single call is thread-safe,
 * but the PAIR of calls is not atomic.
 *
 * <p>The delay simulates real work between the two steps (e.g. a database call) and makes the race visible.
 */
public class UnsafeSeatRegistry implements SeatRegistry {

    private final Map<String, String> holders = new ConcurrentHashMap<>();
    private final long delayMillis;

    public UnsafeSeatRegistry(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    @Override
    public boolean tryHold(String seatCode, String userId) {
        if (holders.containsKey(seatCode)) { // 1. check
            return false;
        }
        pause();                             // other threads can run here and pass the check too
        holders.put(seatCode, userId);       // 2. act
        return true;
    }

    @Override
    public String holderOf(String seatCode) {
        return holders.get(seatCode);
    }

    private void pause() {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore the flag so the caller can see the interrupt
        }
    }
}
