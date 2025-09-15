package com.voltrex.bank.bootstrap;

import com.voltrex.bank.entities.*;
import com.voltrex.bank.entities.AccountType;
import com.voltrex.bank.repositories.*;
import com.voltrex.bank.utils.CardNumberGenerator;
import com.voltrex.bank.utils.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Seed sample data for local/dev use.
 *
 * Put this class in the project and start the app. It will create 6 approved users,
 * accounts, credit cards (some), and transactions across different dates.
 *
 * It only runs if userRepo.count() == 0 (safe to include).
 */
@Component
@RequiredArgsConstructor
public class SampleDataLoader implements CommandLineRunner {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final CreditCardRepository creditCardRepo;
    private final TransactionRepository txnRepo;

    // optional helper generator beans (if you have them). If you don't, comment constructor injection and use fallback.
    private final Optional<AccountNumberGenerator> accountNumberGenerator = Optional.empty();
    private final Optional<CardNumberGenerator> cardNumberGenerator = Optional.empty();
    private final Optional<ReferenceGenerator> referenceGenerator = Optional.empty();

    // If you have a PasswordEncoder bean, Spring will inject it. If not, use null and set a placeholder.
    private final Optional<PasswordEncoder> passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepo.count() > 0) {
            // already have data, skip seeding
            return;
        }

        System.out.println("=== SampleDataLoader: seeding example users/accounts/transactions ===");

        // helper lists of sample people
        List<SeedUser> seeds = List.of(
                new SeedUser("Alice", "Wonder", "pranshu.tomerad@gmail.com", "9000000001"),
                new SeedUser("Bob", "Marley", "pranshu.tomeradd@gmail.com", "9000000002"),
                new SeedUser("Charlie", "Parker", "pranshu.tomeraddd@gmail.com", "9000000003"),
                new SeedUser("Diana", "Prince", "diana@example.com", "9000000004"),
                new SeedUser("Ethan", "Hunt", "ethan@example.com", "9000000005"),
                new SeedUser("Fiona", "Gallagher", "fiona@example.com", "9000000006")
        );

        List<User> users = new ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            SeedUser s = seeds.get(i);
            User u = User.builder()
                    .firstName(s.firstName)
                    .lastName(s.lastName)
                    .email(s.email)
                    .phone(s.phone)
                    .password(passwordEncoder.map(pe -> pe.encode("Password@123")).orElse("password"))
                    .status(Status.APPROVED)
                    .crn("CRN" + (1000 + i))
                    .build();
            users.add(userRepo.save(u));
        }

        // create a mix of account ownership:
        // - user0: all 3 accounts + credit card
        // - user1: saving + salary
        // - user2: saving only
        // - user3: salary + current
        // - user4: current only + credit card
        // - user5: all 3 accounts
        createAccountFor(users.get(0), AccountType.SAVINGS, new BigDecimal("12000.00"), true);
        createAccountFor(users.get(0), AccountType.SALARY, new BigDecimal("3000.00"), false);
        createAccountFor(users.get(0), AccountType.CURRENT, new BigDecimal("25000.00"), false);
        createCreditCardFor(users.get(0), new BigDecimal("100000.00"));

        createAccountFor(users.get(1), AccountType.SAVINGS, new BigDecimal("5000.00"), true);
        createAccountFor(users.get(1), AccountType.SALARY, new BigDecimal("1500.00"), false);

        createAccountFor(users.get(2), AccountType.SAVINGS, new BigDecimal("800.00"), true);

        createAccountFor(users.get(3), AccountType.SALARY, new BigDecimal("4000.00"), true);
        createAccountFor(users.get(3), AccountType.CURRENT, new BigDecimal("6000.00"), false);

        createAccountFor(users.get(4), AccountType.CURRENT, new BigDecimal("18000.00"), true);
        createCreditCardFor(users.get(4), new BigDecimal("75000.00"));

        createAccountFor(users.get(5), AccountType.SAVINGS, new BigDecimal("200.00"), true);
        createAccountFor(users.get(5), AccountType.SALARY, new BigDecimal("1000.00"), false);
        createAccountFor(users.get(5), AccountType.CURRENT, new BigDecimal("3000.00"), false);

        // create transactions with different dates between those users
        // we'll create transfers: some old (2 months ago), some middle (30 days), some recent
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoMonthsAgo = now.minusDays(60);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime tenDaysAgo = now.minusDays(10);
        LocalDateTime threeDaysAgo = now.minusDays(3);

        // utility: pick accounts
        List<Account> allAccounts = accountRepo.findAll();

        // create some historical transfers
        // 1) two months ago: user0 SAVINGS -> user1 SAVINGS  (large)
        createTransferAndSave(
                findAccount(allAccounts, users.get(0), AccountType.SAVINGS),
                findAccount(allAccounts, users.get(1), AccountType.SAVINGS),
                new BigDecimal("2000.00"),
                "Rent share - older",
                twoMonthsAgo.plusHours(10)
        );

        // 2) thirty days ago: user4 CURRENT -> user0 CURRENT
        createTransferAndSave(
                findAccount(allAccounts, users.get(4), AccountType.CURRENT),
                findAccount(allAccounts, users.get(0), AccountType.CURRENT),
                new BigDecimal("5000.00"),
                "Payment",
                thirtyDaysAgo.plusHours(14)
        );

        // 3) ten days ago: user2 SAVINGS -> user5 SALARY
        createTransferAndSave(
                findAccount(allAccounts, users.get(2), AccountType.SAVINGS),
                findAccount(allAccounts, users.get(5), AccountType.SALARY),
                new BigDecimal("100.00"),
                "Loan payback",
                tenDaysAgo.plusHours(9)
        );

        // 4) three days ago: user0 SAVINGS -> user3 CURRENT
        createTransferAndSave(
                findAccount(allAccounts, users.get(0), AccountType.SAVINGS),
                findAccount(allAccounts, users.get(3), AccountType.CURRENT),
                new BigDecimal("300.00"),
                "Transfer recent",
                threeDaysAgo.plusHours(15)
        );

        // 5) recent: user1 SALARY -> user0 SAVINGS
        createTransferAndSave(
                findAccount(allAccounts, users.get(1), AccountType.SALARY),
                findAccount(allAccounts, users.get(0), AccountType.SAVINGS),
                new BigDecimal("250.00"),
                "Refund recent",
                now.minusHours(30)
        );

        // additional random transfers to create variety
        Random rnd = new Random(12345);
        for (int i = 0; i < 15; i++) {
            Account from = allAccounts.get(rnd.nextInt(allAccounts.size()));
            Account to = allAccounts.get(rnd.nextInt(allAccounts.size()));
            if (from.getId().equals(to.getId())) continue;
            BigDecimal amount = BigDecimal.valueOf(50 + rnd.nextInt(2000));
            LocalDateTime time = now.minusDays(rnd.nextInt(50)).withHour(8 + rnd.nextInt(10));
            try {
                createTransferAndSave(from, to, amount, "Seed transfer " + i, time);
            } catch (Exception ex) {
                // ignore individual failures (e.g., insufficient funds) in seed loop
            }
        }

        System.out.println("=== SampleDataLoader: seeding complete. Users created: " + users.size() + " ===");
    }

    // ---------- helpers ----------

    private void createAccountFor(User user, AccountType type, BigDecimal initialBalance, boolean primary) {
        String accNumber = generateAccountNumber();
        Account a = Account.builder()
                .accountNumber(accNumber)
                .owner(user)
                .type(type)
                .balance(initialBalance)
                .primaryAccount(primary)
                .openedAt(LocalDateTime.now().minusDays(90))
                .build();
        accountRepo.save(a);

        // make sure user's accounts list is initialized
        if (user.getAccounts() == null) {
            user.setAccounts(new ArrayList<>());
        }
        user.getAccounts().add(a);
        userRepo.save(user);
    }


    private void createCreditCardFor(User user, BigDecimal limit) {
        // avoid duplicate
        if (creditCardRepo.findByUserId(user.getId()).isPresent()) return;

        String cardNumber = generateCardNumber();
        CreditCard c = CreditCard.builder()
                .cardNumber(cardNumber)
                .user(user)
                .issuedAt(LocalDateTime.now().minusMonths(3))
                .expiryDate(LocalDate.now().plusYears(3))
                .active(true)
                .creditLimit(limit)
                .creditUsed(BigDecimal.ZERO)
                .build();
        creditCardRepo.save(c);
    }

    private String generateAccountNumber() {
        return accountNumberGenerator.map(AccountNumberGenerator::generate12Digit)
                .orElseGet(() -> UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12));
    }

    private String generateCardNumber() {
        return cardNumberGenerator.map(CardNumberGenerator::generate16Digit)
                .orElseGet(() -> "4" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15));
    }

    private String generateReference() {
        return referenceGenerator.map(ReferenceGenerator::generate).orElseGet(() ->
                UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toUpperCase());
    }

    private Account findAccount(List<Account> all, User owner, AccountType type) {
        return all.stream()
                .filter(a -> a.getOwner() != null && a.getOwner().getId().equals(owner.getId()))
                .filter(a -> a.getType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing account for " + owner.getEmail() + " type " + type));
    }

    /**
     * Create transfer between accounts (does not re-check owner security) — simple seed:
     * - debits fromAccount.balance
     * - credits toAccount.balance
     * - records Transaction with snapshot balances
     */
    private void createTransferAndSave(Account fromAccount, Account toAccount, BigDecimal amount, String description, LocalDateTime executedAt) {
        // reload fresh entities (managed)
        Account from = accountRepo.findById(fromAccount.getId()).orElseThrow();
        Account to = accountRepo.findById(toAccount.getId()).orElseThrow();

        // simple balance check: allow negative balances for current accounts, otherwise skip if insufficient
        boolean allowNegative = from.getType() == AccountType.CURRENT;
        if (!allowNegative && (from.getBalance() == null || from.getBalance().compareTo(amount) < 0)) {
            throw new IllegalStateException("Insufficient funds for seed transfer");
        }

        // perform debit/credit
        BigDecimal fromNew = (from.getBalance() == null ? BigDecimal.ZERO : from.getBalance()).subtract(amount);
        BigDecimal toNew = (to.getBalance() == null ? BigDecimal.ZERO : to.getBalance()).add(amount);
        from.setBalance(fromNew);
        to.setBalance(toNew);
        accountRepo.save(from);
        accountRepo.save(to);

        // create transaction record with snapshots
        Transaction tx = Transaction.builder()
                .referenceNumber(generateReference())
                .fromAccount(from)
                .toAccount(to)
                .amount(amount)
                .description(description)
                .status("COMPLETED")
                .type(TransactionType.Transfer)
                .executedAt(executedAt)
                .fromAccountBalanceAfter(fromNew)
                .toAccountBalanceAfter(toNew)
                .build();
        txnRepo.save(tx);
    }

    // small local holder
    private static class SeedUser {
        final String firstName;
        final String lastName;
        final String email;
        final String phone;
        SeedUser(String f, String l, String e, String p) { firstName = f; lastName = l; email = e; phone = p; }
    }

    // Optional generator interface placeholders if project hasn't defined them:
    public interface ReferenceGenerator { String generate(); }
}
