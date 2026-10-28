package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.InvoiceEmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class InvoiceEmailService {

    // Optional: app starts normally when SMTP is not configured.
    // Sending will fail with a clear error message at call time.
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:}") private String from;
    @Value("${spring.mail.host:}") private String host;

    public void sendInvoice(InvoiceEmailRequest request) {
        if (mailSender == null || !StringUtils.hasText(host) || !StringUtils.hasText(from)) {
            throw new IllegalStateException("Email is not configured. Set MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD, and MAIL_FROM in the backend environment.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(request.getTo().trim());
        message.setSubject("TrackBill AI Invoice #BILL-" + request.getBillId());
        message.setText("Dear " + safe(request.getCustomerName(), "Customer") + ",\n\n"
                + "Please find your TrackBill AI sugarcane transportation invoice summary below.\n\n"
                + "Invoice: BILL-" + request.getBillId() + "\n"
                + "Invoice date: " + safe(request.getBillDate(), "Not specified") + "\n"
                + "Transporter: " + safe(request.getOwnerName(), "Tractor Owner") + "\n"
                + "Tractor registration: " + safe(request.getTractorNumber(), "-") + "\n"
                + "Trips: " + (request.getTripCount() == null ? 0 : request.getTripCount()) + "\n"
                + "Total weight: " + money(request.getTotalWeight()) + " tons\n"
                + "Rate per ton: INR " + money(request.getRatePerTon()) + "\n"
                + "Total amount payable: INR " + money(request.getTotalAmount()) + "\n\n"
                + "This invoice summary was sent using TrackBill AI. "
                + "Please contact your transporter if you have any questions.\n\nRegards,\nTrackBill AI");
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new IllegalStateException("Email could not be sent. Verify SMTP host, credentials, port, and app-password settings.");
        }
    }

    private String safe(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }
}
