# TrackBill AI Farmer Portal — implementation and verification notes

## Implemented in source
- Farmer registration now returns a profile without a JWT. Frontend clears stale authentication and redirects to the login page; login is required to receive a token.
- BCrypt password hashing, duplicate email/phone checks, role-bearing Farmer JWTs, and Farmer-only API guards.
- Farmer-scoped crop CRUD (including edit/delete UI), income/expense CRUD, date summaries, monthly reports.
- Open-Meteo geocoding/current weather/5-day forecast integration.
- Gemini farmer assistant with farmer crop context and explicit errors when unconfigured/unavailable.
- Farmer tractor service request submission/cancellation; owner API for reviewing requests and changing statuses.
- Farmer notifications, unread counts, mark-read and mark-all-read. Request creation/status changes generate real notifications.
- Farmer bills are visible only after explicit owner-to-farmer bill linking; the legacy schema's payment status is not inferred.
- Existing Tractor Owner source files were not rewritten. Added owner-side APIs only for service requests and bill linking; `frontend/js/api.js` changed to handle HTTP 204 responses so DELETE operations work.

## Database
Run `backend/db/migration/V1__create_farmer_portal_tables.sql` only after backing up the existing database and verifying `billing` and `tractor_owner` exist. It creates farmer/crop/transaction/notification/service-request/bill-link tables and does not update or delete existing owner records. Alternatively, `spring.jpa.hibernate.ddl-auto=update` may create JPA tables, but review schema changes first.

## Local run
1. Install JDK 21, Maven, and MySQL 8.
2. Back up/import your existing `trackbill_ai` database.
3. Set environment variables from `.env.example`; replace every placeholder. `.env.example` is not automatically loaded by Spring.
4. Set `GEMINI_API_KEY` to enable AI; weather uses Open-Meteo and requires network access.
5. From `backend`, run `mvn clean test` and then `mvn spring-boot:run`.
6. Open `http://localhost:8080/tractorlogin.html`, choose Farmer, register, then manually log in.
7. For a Farmer bill, the bill's tractor owner must call `POST /api/owner/bill-links/{billId}` with the registered farmer email. Existing bills cannot safely be matched to a farmer by display name alone.
8. Owner service requests can be reviewed through `GET /api/owner/service-requests` and updated through `PATCH /api/owner/service-requests/{id}/status?status=ACCEPTED` (or `IN_PROGRESS`, `COMPLETED`, `REJECTED`).

## API and data isolation
All `/api/farmer/**` routes require a Farmer token; all `/api/owner/**` and existing `/api/**` owner endpoints require a Tractor Owner token. Crop, finance, bill, service-request and notification reads are scoped to the authenticated farmer. Owners can only link bills that belong to their own account.

## Test report — not executed in this environment
- Registration returns no token: source logic updated; browser/API integration not executed.
- Manual login required after registration: frontend flow updated; not browser-tested.
- Java compilation and Spring context tests: **not run** (Maven unavailable).
- MySQL migrations/JPA schema and data isolation: **not run** (no live database supplied).
- Open-Meteo: endpoint implemented; live request not executed here.
- Gemini: endpoint implemented; no API key supplied, so generation not verified.
- Owner regression tests (login, trips, labour, expenses, billing): **not run**.
- Service requests, transaction CRUD, bills and notifications: code added; end-to-end persistence not verified.

Do not interpret source edits as passing runtime tests. Run the test checklist against a backed-up development database before deployment.
