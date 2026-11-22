package kz.iitu.registration.service;

import kz.iitu.registration.model.Event;
import kz.iitu.registration.repository.EventRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Business logic. Lab 7 kept events in an ArrayList with a manual ID counter;
 * now the data lives in PostgreSQL and is accessed through EventRepository.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;

    /** Constructor injection: Spring passes the repository bean it created. */
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public String getWelcomeMessage() {
        return "Welcome to the Event Registration API";
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll(Sort.by("id"));
    }

    public List<Event> searchEvents(String title, String location) {
        return eventRepository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseOrderByIdAsc(
                normalize(title), normalize(location));
    }

    public Optional<Event> findEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Transactional
    public Event createEvent(String title, String location) {
        // id is null here; PostgreSQL assigns it on INSERT and save() returns the saved entity
        return eventRepository.save(new Event(title.trim(), location.trim()));
    }

    @Transactional
    public Optional<Event> updateEvent(Long id, String title, String location) {
        // Find the existing row first, replace the fields, keep the same id, then save (UPDATE)
        return eventRepository.findById(id).map(event -> {
            event.setTitle(title.trim());
            event.setLocation(location.trim());
            return eventRepository.save(event);
        });
    }

    @Transactional
    public boolean deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            return false;
        }
        eventRepository.deleteById(id);
        return true;
    }

    private static String normalize(String query) {
        return query == null ? "" : query.trim();
    }
}
