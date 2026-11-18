package demo.javacore.thread;

/**
 * Fix #1: exactly the same logic as {@link UnsafeSeatRegistry}, only marked {@code synchronized}.
 *
 * <p>{@code synchronized} lets one thread at a time run tryHold on this object, so "check" and "act"
 * become one indivisible step. Simple and correct, but every seat shares ONE lock:
 * threads that want different seats still wait for each other.
 */
public class SynchronizedSeatRegistry extends UnsafeSeatRegistry {

    public SynchronizedSeatRegistry(long delayMillis) {
        super(delayMillis);
    }

    @Override
    public synchronized boolean tryHold(String seatCode, String userId) {
        return super.tryHold(seatCode, userId);
    }
}
