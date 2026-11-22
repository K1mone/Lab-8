package kz.iitu.registration.controller;

import kz.iitu.registration.model.Event;
import kz.iitu.registration.model.EventRequest;
import kz.iitu.registration.service.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final EventService eventService;
    private final String applicationName;
    private final String course;
    private final int semester;

    public RegistrationController(EventService eventService,
                                  @Value("${spring.application.name}") String applicationName,
                                  @Value("${app.course}") String course,
                                  @Value("${app.semester}") int semester) {
        this.eventService = eventService;
        this.applicationName = applicationName;
        this.course = course;
        this.semester = semester;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", eventService.getWelcomeMessage());
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", applicationName);
        info.put("course", course);
        info.put("semester", semester);
        return info;
    }

    @GetMapping("/events")
    public List<Event> getEvents(@RequestParam(required = false) String title,
                                     @RequestParam(required = false) String location) {
        if (title == null && location == null) {
            return eventService.getAllEvents();
        }
        return eventService.searchEvents(title, location);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        return eventService.findEventById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> notFound(id));
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request) {
        String error = validate(request);
        if (error != null) {
            return badRequest(error);
        }
        Event created = eventService.createEvent(request.title(), request.location());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventRequest request) {
        String error = validate(request);
        if (error != null) {
            return badRequest(error);
        }
        return eventService.updateEvent(id, request.title(), request.location())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> notFound(id));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        if (eventService.deleteEvent(id)) {
            return ResponseEntity.noContent().build();
        }
        return notFound(id);
    }

    private String validate(EventRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            return "Title is required";
        }
        if (request.location() == null || request.location().isBlank()) {
            return "Location is required";
        }
        return null;
    }

    private ResponseEntity<Map<String, Object>> notFound(Long id) {
        return error(HttpStatus.NOT_FOUND, "Event not found", "Event with id " + id + " does not exist");
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, "Bad request", message);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
