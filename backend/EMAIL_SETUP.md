# TrackBill AI invoice email setup

The Billing page now has a **Send Invoice by Email** section. It sends a saved invoice summary through the authenticated backend endpoint `POST /api/email/invoice`.

Configure SMTP environment variables before using it:

- `MAIL_HOST` — SMTP server, e.g. `smtp.gmail.com`
- `MAIL_PORT` — usually `587` for STARTTLS
- `MAIL_USERNAME` — mailbox login
- `MAIL_PASSWORD` — SMTP password or provider app password
- `MAIL_FROM` — sender email address (normally same as MAIL_USERNAME)

For Gmail, use an App Password with 2-Step Verification; do not put your normal Google password into project files. The application reports a clear configuration error when SMTP settings are missing. Email contains the invoice summary and totals, not a PDF attachment.
