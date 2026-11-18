package demo.javacore.jdbc;

import java.math.BigDecimal;

/** One row of the demo_event table. */
public record EventRow(long id, String name, BigDecimal basePrice) {
}
