package com.voltrex.bank.services;

import com.voltrex.bank.dto.UserDataRequest;
import com.voltrex.bank.dto.UserResponse;
import com.voltrex.bank.dto.UserSecurityRequest;
import com.voltrex.bank.entities.*;
import com.voltrex.bank.events.UserApprovedEvent;
import com.voltrex.bank.exception.EmailAlreadyExistsException;
import com.voltrex.bank.exception.PhoneAlreadyExistsException;
import com.voltrex.bank.repositories.AccountRepository;
import com.voltrex.bank.repositories.UserRepository;
import com.voltrex.bank.utils.AccountNumberGenerator;
import com.voltrex.bank.utils.CRNGenerator;
import com.voltrex.bank.utils.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService{

    private final UserRepository userRepo;
    private final AccountNumberGenerator accGen;
    private final AccountRepository accountRepo;
    private final CRNGenerator crnGen;
    private final PasswordGenerator passGen;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByCrn(username)
                .orElseThrow(null);
    }

    @Transactional
    public void approveUser(Long userId, String adminName) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStatus() != Status.PENDING) {
            throw new IllegalStateException("User is not pending approval");
        }

        // --- Ensure user does not already have a Savings account ---
        boolean hasSavings = user.getAccounts().stream()
                .anyMatch(acc -> acc.getType() == AccountType.SAVINGS);
        if (hasSavings) {
            throw new IllegalStateException("User already has a savings account");
        }

        // --- Generate unique account number ---
        String accNumber;
        int attempts = 0;
        do {
            accNumber = accGen.generate12Digit();
            attempts++;
            if (attempts > 10_000) {
                throw new IllegalStateException("Cannot generate unique account number");
            }
        } while (accountRepo.existsByAccountNumber(accNumber));

        // --- Create savings account ---
        Account savingsAccount = new Account();
        savingsAccount.setAccountNumber(accNumber);
        savingsAccount.setBalance(BigDecimal.ZERO);
        savingsAccount.setOwner(user);
        savingsAccount.setPrimaryAccount(true);
        savingsAccount.setActive(true);
        savingsAccount.setType(AccountType.SAVINGS);

        accountRepo.save(savingsAccount);

        // --- Generate CRN and temp password ---
        String crn = crnGen.generate();
        String tempPassword = passGen.generate(12);
        String hash = passwordEncoder.encode(tempPassword);

        user.setCrn(crn);
        user.setPassword(hash);
        user.setStatus(Status.APPROVED);
        user.getAccounts().add(savingsAccount);

        userRepo.save(user);


        publisher.publishEvent(new UserApprovedEvent(user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                crn,
                tempPassword,
                accNumber
        ));

        // TODO: record audit with adminName if needed
    }

    public User getUserById(Long id){
        return userRepo.findById(id).orElseThrow(null);
    }

    public ResponseEntity<Map<String,Object>> updateUser(UserDataRequest req, User currentUser){
        if (!currentUser.getEmail().equals(req.getEmail()) && userRepo.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (!currentUser.getPhone().equals(req.getPhone()) && userRepo.existsByPhone(req.getPhone())) {
            throw new PhoneAlreadyExistsException("Phone Number already exists");
        }

        currentUser.setFirstName(req.getFirstName());
        currentUser.setLastName(req.getLastName());
        currentUser.setEmail(req.getEmail());
        currentUser.setPhone(req.getPhone());

        Address address = new Address();
        address.setState(req.getState());
        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setPincode(req.getPincode());

        currentUser.setAddress(address);

        userRepo.save(currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
