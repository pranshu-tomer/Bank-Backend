package com.voltrex.bank.listeners;

import com.voltrex.bank.events.UserApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailListener {

    private final JavaMailSender mailSender;

    @EventListener
    public void handleApprovedUser(UserApprovedEvent userEvent){
        String subject = "Your banking account is ready";
        String text = String.format(
                "Hi %s,\n\nYour account has been approved.\nCRN: %s\nAccount Number: %s\nTemporary Password: %s\n\nPlease login and change your password immediately.\n\nRegards,\nBanking Team",
                userEvent.getName(), userEvent.getCrn(), userEvent.getAccNumber(), userEvent.getTempPassword()
        );

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(userEvent.getEmail());
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }
}

