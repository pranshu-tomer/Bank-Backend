package com.voltrex.bank.services;

import com.voltrex.bank.entities.Account;
import com.voltrex.bank.entities.AccountType;
import com.voltrex.bank.entities.Status;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.repositories.AccountRepository;
import com.voltrex.bank.repositories.UserRepository;
import com.voltrex.bank.utils.AccountNumberGenerator;
import com.voltrex.bank.utils.CRNGenerator;
import com.voltrex.bank.utils.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService{

    private final UserRepository userRepo;
    private final AccountNumberGenerator accGen;
    private final AccountRepository accountRepo;
    private final CRNGenerator crnGen;
    private final PasswordGenerator passGen;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

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
        user.setCreditCard(null); // enforce no card initially

        userRepo.save(user);

        // --- Send email ---
        emailService.sendAccountApprovedEmail(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                crn,
                tempPassword,
                accNumber
        );

        // TODO: record audit with adminName if needed
    }
}


//public class UserService implements UserDetailsService {
//    private final UserRepository userRepo;
//
//
//
//
//
//
//
//    public UserService(UserRepository userRepo,
//                       AccountRepository accountRepo,
//                       AccountNumberGenerator accGen,
//                       CRNGenerator crnGen,
//                       PasswordGenerator passGen,
//                       EmailService emailService, PasswordEncoder passwordEncoder) {
//        this.userRepo = userRepo;
//        this.accountRepo = accountRepo;
//        this.accGen = accGen;
//        this.crnGen = crnGen;
//        this.passGen = passGen;
//        this.emailService = emailService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    public User registerUser(String name, String email, String phone, String address,
//                             String gender, Integer age,String dob) {
//        // basic checks
//        userRepo.findByEmail(email).ifPresent(u -> {
//            throw new IllegalArgumentException("Email already in use");
//        });
//        User u = new User();
//        u.setName(name);
//        u.setEmail(email);
//        u.setPhone(phone);
//        u.setAddress(address);
//        u.setGender(gender);
//        u.setAge(age);
//        u.setDob(dob);
//        u.setStatus(RegistrationStatus.PENDING);
//        userRepo.save(u);
//
//        // Optionally publish event or push notification to admin here.
//        return u;
//    }
//

//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return userRepo.findByCrn(username)
//                .orElseThrow(null);
//    }
//
//    public User getUserById(Long id){
//        return userRepo.findById(id).orElseThrow(null);
//    }
//
//    public Optional<User> findDomainUserByCrnOrEmail(String principalName) {
//        return userRepo.findByCrn(principalName).or(() -> userRepo.findByEmail(principalName));
//    }
//
//    public String getUserNameById(Long userId) {
//        User user = userRepo.findById(userId).orElseThrow(null);
//        return user.getName();
//    }
//}
//
//
