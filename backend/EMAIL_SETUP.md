# TrackBill AI Invoice Email Setup & Configuration

TrackBill AI includes an email delivery system for sugarcane billing. When you generate an invoice on the **Billing** page, you can email the summary and receipt directly to farmers, sugar mills, or transport partners.

---

## 1. How It Works

1. **Rich HTML & Plain-Text Receipt**: Invoices are formatted with a branded TrackBill header, breakdown table (trips, tonnage, rate, and total amount payable in ₹), and transporter details.
2. **Direct SMTP Delivery**: When SMTP credentials are provided, emails are delivered over encrypted TLS to any recipient inbox.
3. **Automatic Farmer Linking & Notification**: If the recipient's email is registered as a Farmer on TrackBill AI, the invoice is automatically linked to their portal and an in-app notification is sent to their dashboard under **Bills**.
4. **Resilient Local Outbox**: In local development or before SMTP credentials are configured, invoices are safely recorded into the verified system outbox (`/api/email/history`) without failing.

---

## 2. Configuring Real SMTP Delivery (e.g., Gmail)

To send real emails to external mailboxes, add your SMTP credentials to your `.env` file (or system environment):

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_16_character_app_password
MAIL_FROM=your_email@gmail.com
```

### For Gmail Users:
1. Go to your **Google Account** -> **Security**.
2. Ensure **2-Step Verification** is turned ON.
3. Search for **App Passwords** (or navigate to `https://myaccount.google.com/apppasswords`).
4. Generate a new app password (e.g. named "TrackBill AI").
5. Copy the generated 16-character code (without spaces) and paste it into `MAIL_PASSWORD` in `.env`.
6. Restart the backend server.

### For Custom SMTP (Brevo, SendGrid, Outlook, AWS SES):
```env
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=your_login
MAIL_PASSWORD=your_smtp_key
MAIL_FROM=billing@yourdomain.com
```

---

## 3. Endpoints

- `POST /api/email/invoice` — Authenticated invoice email dispatch (requires Tractor Owner token).
- `GET /api/email/history` — View list of recently dispatched invoice emails.
- `GET /api/email/status` — Check SMTP gateway configuration and status.
