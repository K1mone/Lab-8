package kz.iitu.registration.config;

import kz.iitu.registration.model.Event;
import kz.iitu.registration.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Inserts three sample events on startup, but only when the table is empty
 * (first run on a fresh database). On later restarts the existing rows are kept.
 */
@Component
public class SampleDataLoader implements CommandLineRunner {

    private final EventRepository eventRepository;

    public SampleDataLoader(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            return;
        }
        eventRepository.saveAll(List.of(
                new Event("Web Design Workshop", "IITU, Room 301"),
                new Event("JavaScript Quiz", "IITU, Computer Lab 205"),
                new Event("IT Career Talk", "IITU, Main Hall")
        ));
    }
}
