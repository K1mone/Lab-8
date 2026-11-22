package kz.iitu.registration.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity: one object of this class = one row in the "events" table.
 * Hibernate creates/updates the table from this class (ddl-auto=update).
 */
@Entity
@Table(name = "events")
@JsonPropertyOrder({"id", "title", "location"})
public class Event {

    /** Primary key. PostgreSQL generates it (IDENTITY column), the client never sets it. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String location;

    /** JPA needs a no-argument constructor to create objects from database rows. */
    protected Event() {
    }

    public Event(String title, String location) {
        this.title = title;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
