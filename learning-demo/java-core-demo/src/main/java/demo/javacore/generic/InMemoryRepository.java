package demo.javacore.generic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Generic in-memory storage.
 *
 * <p>LinkedHashMap: lookup by id in O(1), and findAll keeps the insertion order.
 * Not thread-safe; the thread package shows why shared mutable state needs care.
 */
public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {

    private final Map<ID, T> store = new LinkedHashMap<>();

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity");
        store.put(entity.id(), entity); // same id -> replaced (an "upsert")
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values()); // a copy: callers cannot change our map through it
    }

    @Override
    public int count() {
        return store.size();
    }
}
