package demo.javacore.model;

import demo.javacore.generic.Identifiable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A seat such as "A12". Immutable value shared by the examples of this project.
 *
 * <p>A record gives final fields, a constructor, accessors and equals/hashCode/toString for free.
 * The compact constructor validates the data before the fields are assigned.
 */
public record Seat(String code, SeatType type, BigDecimal price) implements Identifiable<String> {

    public Seat {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(price, "price");
        if (!code.matches("[A-Z][0-9]+")) {
            throw new IllegalArgumentException("Seat code must look like A12: " + code);
        }
    }

    /** Row letter, e.g. 'A' for "A12". */
    public char row() {
        return code.charAt(0);
    }

    /** The seat code is its identity, so a Seat can be stored in a generic Repository. */
    @Override
    public String id() {
        return code;
    }
}
