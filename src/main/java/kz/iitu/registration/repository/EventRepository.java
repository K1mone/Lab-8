package kz.iitu.registration.repository;

import kz.iitu.registration.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data creates the implementation of this interface at startup.
 * JpaRepository already gives save, findAll, findById, existsById, deleteById, ...
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Derived query: Spring builds the SQL from the method name, roughly
     * WHERE lower(title) LIKE lower('%title%') AND lower(location) LIKE lower('%location%') ORDER BY id.
     * An empty string matches every row, so one method covers title-only, location-only and both.
     */
    List<Event> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseOrderByIdAsc(
            String title, String location);
}
