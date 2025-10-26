package com.veggieshop.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@veggieshop.com}")
    private String fromAddress;

    @Override
    public void sendPasswordReset(String toEmail, String name, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromAddress);
        message.setSubject("Password Reset Request");
        message.setText(
                "Hello " + name + ",\n\n"
                        + "We received a request to reset your VeggieShop password.\n"
                        + "Please click the link below (or copy-paste it into your browser):\n\n"
                        + resetLink + "\n\n"
                        + "If you did not request this, simply ignore this email.\n\n"
                        + "Thanks,\nVeggieShop Team"
        );

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
