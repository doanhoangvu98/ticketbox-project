package com.noah.ticketbox.api.event;

import com.noah.ticketbox.api.event.dto.EventSearchCriteria;
import com.noah.ticketbox.core.domain.EventStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dynamic filters for the event search.
 *
 * <p>{@link Specification} is a functional interface: {@code (root, query, cb) -> Predicate}.
 * Each filter below is a small lambda, and {@link Specification#allOf} combines only the filters
 * the client actually sent. Same idea as composing {@code java.util.function.Predicate}.
 */
final class EventSpecifications {

    private EventSpecifications() {
    }

    static Specification<EventEntity> from(EventSearchCriteria c) {
        List<Specification<EventEntity>> filters = new ArrayList<>();
        if (c.status() != null) {
            filters.add(hasStatus(c.status()));
        }
        if (c.keyword() != null && !c.keyword().isBlank()) {
            filters.add(nameContains(c.keyword()));
        }
        if (c.from() != null) {
            filters.add(startsFrom(c.from()));
        }
        if (c.to() != null) {
            filters.add(startsBefore(c.to()));
        }
        return Specification.allOf(filters);
    }

    static Specification<EventEntity> hasStatus(EventStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    static Specification<EventEntity> nameContains(String keyword) {
        String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get("name")), pattern);
    }

    static Specification<EventEntity> startsFrom(OffsetDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<OffsetDateTime>get("startsAt"), from);
    }

    static Specification<EventEntity> startsBefore(OffsetDateTime to) {
        return (root, query, cb) -> cb.lessThan(root.<OffsetDateTime>get("startsAt"), to);
    }
}
