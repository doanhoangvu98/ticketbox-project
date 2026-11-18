package demo.javacore.collection;

import demo.javacore.model.Seat;
import demo.javacore.model.SeatType;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Collections + Lambda + Stream on a list of seats.
 *
 * <p>Each method picks the collection that fits the question:
 * EnumMap for enum keys, TreeMap/TreeSet when the result must be sorted,
 * partitioningBy when the answer is a true/false split.
 */
public final class SeatStats {

    private SeatStats() {
    }

    // Small reusable predicates (lambdas). Callers combine them with and(), or(), negate().

    public static Predicate<Seat> isVip() {
        return seat -> seat.type() == SeatType.VIP;
    }

    public static Predicate<Seat> cheaperThan(BigDecimal limit) {
        return seat -> seat.price().compareTo(limit) < 0;
    }

    /** groupingBy + counting, collected into an EnumMap (compact, ordered like the enum). */
    public static Map<SeatType, Long> countByType(Collection<Seat> seats) {
        return seats.stream()
                .collect(Collectors.groupingBy(
                        Seat::type,
                        () -> new EnumMap<>(SeatType.class),
                        Collectors.counting()));
    }

    /** TreeMap keeps the rows sorted: A, B, C, ... */
    public static SortedMap<Character, List<Seat>> byRow(Collection<Seat> seats) {
        return seats.stream()
                .collect(Collectors.groupingBy(Seat::row, TreeMap::new, Collectors.toList()));
    }

    /** partitioningBy = groupingBy with a boolean key; the map always has both true and false. */
    public static Map<Boolean, List<Seat>> splitVip(Collection<Seat> seats) {
        return seats.stream().collect(Collectors.partitioningBy(isVip()));
    }

    /** filter + map + reduce: sum the prices of the seats that match the filter. */
    public static BigDecimal totalPrice(Collection<Seat> seats, Predicate<Seat> filter) {
        return seats.stream()
                .filter(filter)
                .map(Seat::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** A Set drops duplicates; a TreeSet also sorts. */
    public static Set<Character> distinctRows(Collection<Seat> seats) {
        return seats.stream()
                .map(Seat::row)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
