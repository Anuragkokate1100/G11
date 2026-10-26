# TrackBill AI CRUD Frontend Update

The redesigned frontend is served directly by Spring Boot from `src/main/resources/static`.

## CRUD now exposed from the dashboard

- Labour: Add, Read, Update, Delete
- Trips: Add, Read, Update, Delete
- Expenses: Add, Read, Update, Delete
- Bills: Create, Read, Delete (the supplied backend has no Bill PUT endpoint)
- Owner Profile: Read, Update

## Dashboard synchronization

The Dashboard Overview now loads complete lists from:

- `GET /api/trips`
- `GET /api/labour`
- `GET /api/expenses`
- `GET /api/bills`

Every successful add/update/delete refreshes the dashboard record center, counters, charts/statistics, and module tables.

The record center has tabs for Trips, Labour, Expenses, and Bills and displays every returned backend record rather than only the five recent records used by the summary section.

## Running

```bash
mvn spring-boot:run
```

Then open `http://localhost:8080/`.
