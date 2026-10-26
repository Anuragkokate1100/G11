# Database notes

The application is configured to use one MySQL schema, `trackbill_ai` by default, for both Tractor Owner and Farmer records.

The `original_dump/` directory preserves the supplied export. It contains separate files with names including `tractor_owner`, `trip`, `trips`, `expense`, `expenses`, and `users`. Some names overlap conceptually and may represent different versions of the schema.

Before importing any dump into an existing database:
1. Take a backup.
2. Inspect each SQL file's `CREATE TABLE`, constraints and inserts.
3. Confirm which schema matches the entities/repositories in `backend/src/main/java`.
4. Import only the necessary files into a test schema first.
5. Verify owner registration/login, trips, expenses, labour, and billing before applying any change to the main schema.

The Farmer and FarmCrop entities are mapped into the same configured schema. With the current `spring.jpa.hibernate.ddl-auto=update`, Hibernate can create/update those mapped tables at startup. This setting is convenient for local development but should not replace reviewed migrations for production.
