# TrackBill AI Frontend ↔ Backend Contract

The uploaded project archive contains the frontend only. No Java/Servlet/JSP backend source was present in the archive, so the frontend was updated without changing the existing API contract.

## Authentication
- `POST /api/auth/login`
- `POST /api/auth/register`

## Owner
- `GET /api/owner/profile`
- `PUT /api/owner/profile`

## Trips — CRUD
- `GET /api/trips`
- `POST /api/trips`
- `PUT /api/trips/{id}`
- `DELETE /api/trips/{id}`
- `GET /api/trips/unbilled`

## Labour — CRUD
- `GET /api/labour`
- `POST /api/labour`
- `PUT /api/labour/{id}`
- `DELETE /api/labour/{id}`

## Expenses — CRUD
- `GET /api/expenses`
- `POST /api/expenses`
- `PUT /api/expenses/{id}`
- `DELETE /api/expenses/{id}`

## Billing
- `GET /api/bills`
- `POST /api/bills`
- `GET /api/bills/{id}`
- `DELETE /api/bills/{id}`

## Labour Payments
- `POST /api/labour-payments/calculate`
- `POST /api/labour-payments/save`

## Dashboard & Reports
- `GET /api/dashboard/stats?filter={filter}`
- `GET /api/dashboard/visualizations?filter={filter}`
- Report endpoints already referenced by the existing `dashboard.js` are preserved.

## AI
- `POST /api/ai/chat`

The redesigned dashboard keeps these calls intact. Create, read, update, and delete operations for trips, labour, expenses, bills, and owner profile continue to use the existing authenticated API client.
