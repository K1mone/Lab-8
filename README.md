# Lab 8 — Event Registration API: Spring Data JPA + PostgreSQL

Spring Boot backend for the Event Registration scenario. Lab 7 built full REST CRUD on an
in-memory `ArrayList`; Lab 8 keeps the same endpoints and JSON, but stores events in
**PostgreSQL** through **Spring Data JPA**. Data now survives application and database restarts.

## Requirements
- **JDK 17 or later** (tested on JDK 23)
- Gradle — the wrapper (`gradlew`) is included
- **PostgreSQL** 14+ (local), pgAdmin or `psql`
- Spring Boot 4.1.2; dependencies: Spring Web, Spring Data JPA, PostgreSQL driver, springdoc-openapi (Swagger UI)
- Postman / Swagger UI / IntelliJ HTTP Client (`requests.http`) for POST, PUT, DELETE

## 1. PostgreSQL setup
Create an empty database `event_db` (pgAdmin: *Databases → Create → Database*, or psql):
```sql
CREATE DATABASE event_db;
```
Use a local account that can connect to `event_db` and create tables in it. The `events`
table is created automatically by Hibernate on the first run (`spring.jpa.hibernate.ddl-auto=update`).

## 2. Credentials (never committed)
`application.properties` contains only placeholders:
```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/event_db}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

| Variable | Required | Meaning |
|---|---|---|
| `DB_USER` | yes | PostgreSQL user name |
| `DB_PASSWORD` | yes | PostgreSQL password |
| `DB_URL` | no | JDBC URL, default `jdbc:postgresql://localhost:5432/event_db` |

Set them in **one** of these ways:
- **Terminal:** `export DB_USER=... DB_PASSWORD=...` (Windows PowerShell: `$env:DB_USER="..."`)
- **IntelliJ IDEA:** Run → Edit Configurations → *Environment variables*
- **Local file:** copy `application-local.properties.example` to `application-local.properties`
  and fill in the values. That file is in `.gitignore` and is never pushed.

## 3. Run
```bash
./gradlew bootRun        # Windows: gradlew.bat bootRun
```
Base URL: `http://localhost:8080`. Swagger UI: http://localhost:8080/swagger-ui/index.html

On the very first start (empty table) three sample events are inserted:
Web Design Workshop, JavaScript Quiz, IT Career Talk. Later restarts keep whatever is in the database.

Tests (`./gradlew test`) use an in-memory H2 database and do not need PostgreSQL.

## Endpoints

| Method | URL | Body | Success | Errors |
|---|---|---|---|---|
| GET | `/api/events` | – | **200**, all events | – |
| GET | `/api/events?title=quiz` | – | **200**, title contains `quiz` (ignoring case); `[]` if none | – |
| GET | `/api/events/{id}` | – | **200**, one event | **404** unknown id |
| POST | `/api/events` | `{"title", "location"}` | **201**, created event with generated id | **400** missing/blank title or location |
| PUT | `/api/events/{id}` | `{"title", "location"}` | **200**, same id, fields replaced | **404** unknown id, **400** missing/blank field |
| DELETE | `/api/events/{id}` | – | **204**, no body | **404** unknown id |

Optional filter kept from Lab 7: `?location=hall`, and `?title=workshop&location=room` (both must match).
Also kept: `GET /api/hello`, `GET /api/info`.

The client never chooses the id: the request body only has `title` and `location`; an `id` sent in
JSON is ignored. PostgreSQL generates ids (identity column). A 400 response never touches the database.

### Sample POST body
```json
{
  "title": "Spring Data Workshop",
  "location": "IITU, Room 101"
}
```
Response **201 Created**:
```json
{
  "id": 4,
  "title": "Spring Data Workshop",
  "location": "IITU, Room 101"
}
```
Error, e.g. `GET /api/events/999` → **404**:
```json
{
  "status": 404,
  "error": "Event not found",
  "message": "Event with id 999 does not exist"
}
```

## Checking persistence
1. `POST /api/events` → note the returned `id`.
2. In psql / pgAdmin: `SELECT id, title, location FROM events;` → the row is there.
3. Stop the app (`Ctrl+C`), start it again, `GET /api/events/{id}` → still returned.
4. `DELETE /api/events/{id}` → 204, again → 404, and the row is gone from the SQL query.

## Project structure
```
src/main/java/kz/iitu/registration/
  EventRegistrationApplication.java      – entry point
  controller/RegistrationController.java – HTTP: URLs, body, validation, status codes
  service/EventService.java              – business logic, uses EventRepository
  repository/EventRepository.java        – JpaRepository<Event, Long>
  model/Event.java                       – @Entity mapped to table "events"
  model/EventRequest.java                – JSON body of POST/PUT (title, location)
  config/SampleDataLoader.java           – inserts sample events if the table is empty
src/main/resources/application.properties
application-local.properties.example     – template for local credentials
requests.http                            – ready-made test requests
```

## How it works
- **Entity — `Event`** (`@Entity`, `@Table(name = "events")`): a Java class mapped to a table;
  each object is one row. `@Id` marks the primary key, `@GeneratedValue(strategy = IDENTITY)`
  lets PostgreSQL generate it. JPA needs the no-arg constructor to build objects from rows.
- **Repository — `EventRepository extends JpaRepository<Event, Long>`**: an interface only;
  Spring Data generates the implementation with `save`, `findAll`, `findById`, `existsById`,
  `deleteById`. The title search is a *derived query*:
  `findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseOrderByIdAsc` → SQL `LIKE` ignoring case.
- **Service — `EventService`** receives the repository through its constructor. PUT first loads
  the event with `findById`, sets the new title/location, and `save` does an `UPDATE` (same id).
  Write methods are `@Transactional`.
- **Controller — `RegistrationController`** is unchanged from Lab 7 except for the type
  (`Event`, `Long id`): it validates input and maps results to 200 / 201 / 204 / 400 / 404.
- **ArrayList → PostgreSQL:** no manual id counter, no `synchronized`; the database guarantees
  unique ids and concurrent access, and data is kept on disk between restarts.
