package kz.iitu.registration;

import kz.iitu.registration.model.Event;
import kz.iitu.registration.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EventServiceTests {

    @Autowired
    private EventService eventService;

    @Test
    void crudThroughRepository() {
        Event created = eventService.createEvent("  Spring Data Talk ", "Room 404");
        assertNotNull(created.getId());
        assertEquals("Spring Data Talk", created.getTitle());

        Event updated = eventService.updateEvent(created.getId(), "JPA Talk", "Main Hall").orElseThrow();
        assertEquals(created.getId(), updated.getId());
        assertEquals("JPA Talk", eventService.findEventById(created.getId()).orElseThrow().getTitle());

        assertEquals(1, eventService.searchEvents("jpa", null).size());
        assertTrue(eventService.searchEvents("no-such-event", null).isEmpty());

        assertTrue(eventService.deleteEvent(created.getId()));
        assertFalse(eventService.deleteEvent(created.getId()));
        assertTrue(eventService.findEventById(created.getId()).isEmpty());
        assertTrue(eventService.updateEvent(created.getId(), "x", "y").isEmpty());
    }

    @Test
    void sampleDataIsLoaded() {
        assertEquals(1, eventService.searchEvents("QUIZ", null).size());
    }
}
