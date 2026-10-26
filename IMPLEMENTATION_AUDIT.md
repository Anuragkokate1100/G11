# TrackBill AI — source audit and change log

## Existing project inspected
- `frontend/`: static HTML/CSS/vanilla JavaScript, including the existing Tractor Owner pages and Farmer registration/dashboard.
- `backend/`: Spring Boot 3.3.5, Java 21, Spring Security/JWT, Spring Data JPA, BCrypt, MySQL.
- `database/original_dump/`: several historical SQL exports with overlapping/singular/plural table variants. Do not import all of them blindly.
- Existing owner models/controllers include `TractorOwner`, `Trip`, `Labour`, `Expense`, `Billing` and related repositories/services.

## Registration bug found
`FarmerAuthService.register()` returned the same `FarmerAuthResponse` builder used for login, which generated a JWT during registration. `frontend/js/farmer-register.js` then explicitly required that token, stored it, saved the farmer profile and role in localStorage, and redirected to `farmerhome.html`. That combination silently authenticated new users.

### Fix applied
- Registration now saves the BCrypt-hashed account and returns a profile with `token: null`.
- The registration frontend no longer expects or stores a token; it clears stale auth state and redirects to `tractorlogin.html?role=farmer&email=...`.
- The login page selects the Farmer role from the query string and prefills the email; the Farmer login endpoint is the only flow that issues a Farmer JWT.
- `GET /api/auth/farmer/me` exposes the authenticated profile.
- Existing role claims and legacy Tractor Owner tokens without a role claim remain supported by the current JWT filter.

## Added/modified files

### Modified
- `.env.example` — added environment variable template without real secrets.
- `README.md` — updated setup, endpoints, limitations and verification status.
- `FARMER_PORTAL_IMPLEMENTATION.md` — updated implementation and test notes.
- `backend/src/main/resources/application.properties` — uses environment variables for DB password/JWT secret and adds weather API URLs.
- `backend/db/migration/V1__create_farmer_portal_tables.sql` — adds farmer transactions, notifications, service requests and explicit bill-link table in addition to farmer/crop tables.
- `backend/src/main/java/com/example/trackbill/AIBackend/service/FarmerAuthService.java` — registration no longer returns a token.
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/FarmerAuthController.java` — current farmer profile endpoint.
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/FarmerServiceRequestController.java` — real event notification on submit/cancel.
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/OwnerServiceRequestController.java` — added owner request queue/status handling; farmer contact is only exposed after assignment.
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/OwnerBillLinkController.java` — explicit owner-authorized bill-to-farmer linking.
- `frontend/farmer-register.html` — retained existing form structure; frontend flow updated in its script.
- `frontend/farmerhome.html` — responsive modules, finance/weather/AI/service/bill/report/notification UI and navigation sections.
- `frontend/js/api.js` — handles HTTP 204 responses correctly (important for DELETE).
- `frontend/js/login.js` — honors Farmer role/email query parameters.
- `frontend/js/farmer-register.js` — no automatic login after registration.
- `frontend/js/farmerhome.js` — handles wrapped crop API responses, crop edit/delete and module navigation.
- `frontend/js/farmer-modules.js` — finance, reports, weather, AI, service requests, bills and notification UI integrations.

### Added
- `backend/src/main/java/com/example/trackbill/AIBackend/model/FarmerTransaction.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/repository/FarmerTransactionRepository.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/dto/FarmerTransactionRequest.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/FarmerTransactionController.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/FarmerWeatherController.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/FarmerAiController.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/model/FarmerNotification.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/repository/FarmerNotificationRepository.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/FarmerNotificationController.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/model/TractorServiceRequest.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/repository/TractorServiceRequestRepository.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/FarmerReportsController.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/model/FarmerBillLink.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/repository/FarmerBillLinkRepository.java`
- `backend/src/main/java/com/example/trackbill/AIBackend/controller/FarmerBillsController.java`

No files were intentionally deleted. Existing Tractor Owner trip, labour, expense, billing and dashboard implementations were not rewritten. Owner-side additions are limited to service request handling and explicit bill linking; `api.js` and the shared login page were updated for shared frontend behavior.

## Known gaps / safety decisions
- Legacy billing stores the customer as a name string and has no reliable farmer foreign key or payment-status field. Bills are only displayed after an owner explicitly links the bill ID to a registered farmer email; payment status is displayed as untracked, never inferred.
- Owner-side service request actions are API-backed but there is no dedicated owner UI screen for them yet.
- Weather and Gemini require network access; Gemini requires `GEMINI_API_KEY`. API failures produce errors rather than fake values.
- Automated weather alerts, richer charts, PDF export, pagination, and notifications for every possible system event are not complete.
- `JWT_SECRET` and database credentials must be set in the runtime environment. `.env.example` is a template and Spring Boot does not auto-load it.

## Verification
- JavaScript syntax checks (`node --check`) passed for all frontend JavaScript files.
- Maven is not installed in the current execution environment. Java compilation, Spring context tests, MySQL migration/integration tests, real weather/Gemini calls and regression tests for Tractor Owner functionality were not executed. They must be run locally before deployment.
