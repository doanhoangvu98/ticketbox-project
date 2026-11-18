package demo.javacore.thread;

/**
 * "Give this seat to this user if nobody has it yet." Called by many threads at the same time.
 */
public interface SeatRegistry {

    /**
     * @return true if the user got the seat, false if somebody already holds it
     */
    boolean tryHold(String seatCode, String userId);

    /**
     * @return the user who holds the seat, or null if it is free
     */
    String holderOf(String seatCode);
}
