package com.voltrex.bank.configs;

import com.voltrex.bank.entities.Account;
import com.voltrex.bank.entities.AccountType;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.entities.Status;
import com.voltrex.bank.repositories.UserRepository;
import com.voltrex.bank.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepo,
                                          AccountRepository accountRepo,
                                          BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepo.count() == 0) {
                // Create Voltrex Bank user
                User bankUser = User.builder()
                        .crn("CRN0000001")
                        .firstName("Voltrex")
                        .lastName("Bank")
                        .email("tomerpranshu11@gmail.com")
                        .phone("9026401421")
                        .password(passwordEncoder.encode("voltrex@1234"))
                        .status(Status.APPROVED)
                        .build();

                userRepo.save(bankUser);

                // Create account 00000000
                Account bankAccount = Account.builder()
                        .accountNumber("000000000000")
                        .balance(BigDecimal.ZERO)
                        .owner(bankUser)
                        .type(AccountType.SAVINGS)
                        .build();

                accountRepo.save(bankAccount);

                System.out.println("✅ Voltrex Bank user and account created.");
            } else {
                System.out.println("ℹ️ Users already exist, skipping initialization.");
            }
        };
    }
}
