package com.voltrex.bank.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender) { this.mailSender = mailSender; }

    public void sendAccountApprovedEmail(String to, String name, String crn, String tempPassword, String accountNumber) {
        String subject = "Your banking account is ready";
        String text = String.format(
                "Hi %s,\n\nYour account has been approved.\nCRN: %s\nAccount Number: %s\nTemporary Password: %s\n\nPlease login and change your password immediately.\n\nRegards,\nBanking Team",
                name, crn, accountNumber, tempPassword
        );

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }
}

