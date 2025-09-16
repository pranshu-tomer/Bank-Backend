package com.voltrex.bank.listeners;

import com.voltrex.bank.events.TransactionEvent;
import com.voltrex.bank.events.UserApprovedEvent;
import com.voltrex.bank.events.UserLoginEvent;
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

    @EventListener
    public void handleLoginEvent(UserLoginEvent userLoginEvent){
        String subject = "Login Alert";
        String text = String.format(
                "Hello %s,\n\nWelcome back to Voltrex Bank. \nYou’ve successfully logged in to your account.",userLoginEvent.getName()
        );

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(userLoginEvent.getEmail());
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }

    @EventListener
    public void handleTxnEvent(TransactionEvent txnEvent){
        String subject = "Transaction Alert";
        String text = String.format(
                "Hello %s,\n\n" +
                        "A %s transaction has been made on your %s account (****%s).\n\n" +
                        "Amount: %s\n" +
                        "New Balance: %s\n\n" +
                        "If you did not authorize this transaction, please contact Voltrex Bank support immediately.\n\n" +
                        "Thank you,\n" +
                        "Voltrex Bank",
                txnEvent.getName(),
                txnEvent.getDirection(), // e.g., "debit" or "credit"
                txnEvent.getType(),      // e.g., "SAVINGS"
                txnEvent.getNumber().substring(txnEvent.getNumber().length() - 4),
                txnEvent.getAmount().toPlainString(),
                txnEvent.getNewBalance().toPlainString()
        );


        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(txnEvent.getEmail());
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }
}

