package demo.javacore.generic;

/**
 * Anything that has an identity. Used as the upper bound of {@link Repository}.
 *
 * @param <ID> type of the identifier (Long, String, UUID, ...)
 */
public interface Identifiable<ID> {

    ID id();
}
