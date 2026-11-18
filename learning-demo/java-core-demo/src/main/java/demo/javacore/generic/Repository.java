package demo.javacore.generic;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * One storage contract for every entity type.
 *
 * <p>{@code T extends Identifiable<ID>} is a bounded type parameter: the compiler guarantees that every
 * stored object has {@code id()}, so a single implementation works for seats, customers, events, ...
 * Spring Data's {@code JpaRepository<T, ID>} in ticketbox-api follows the same idea.
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
public interface Repository<T extends Identifiable<ID>, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    int count();

    /**
     * "? extends T" (producer): we only READ T values out of the collection,
     * so a collection of T or of any subtype of T is accepted.
     */
    default void saveAll(Collection<? extends T> entities) {
        entities.forEach(this::save);
    }

    /**
     * "? super T" (consumer): the predicate only RECEIVES T values,
     * so a {@code Predicate<T>} works, and so does a more general {@code Predicate<Object>}.
     */
    default List<T> findAll(Predicate<? super T> filter) {
        return findAll().stream().filter(filter).toList();
    }
}
