package com.voltrex.bank.services;

import com.voltrex.bank.entities.Account;
import com.voltrex.bank.entities.JobRun;
import com.voltrex.bank.entities.Transaction;
import com.voltrex.bank.entities.TransactionType;
import com.voltrex.bank.repositories.AccountRepository;
import com.voltrex.bank.repositories.JobRunRepository;
import com.voltrex.bank.repositories.TransactionRepository;
import com.voltrex.bank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;
    private final JobRunRepository jobRunRepo;
    private final UserRepository userRepo;
    private final PlatformTransactionManager txManager;
    // config
    private final int pageSize = 200;

    // Use ShedLock annotation if added:
    // @SchedulerLock(name = "monthlyProcessorLock", lockAtLeastFor = "PT10M", lockAtMostFor = "PT1H")
    @Scheduled(cron = "0 0 2 1 * *") // 2:00 on the 1st day of month (server timezone)
    public void runMonthlyJobs() {

        YearMonth toProcess = YearMonth.now().minusMonths(1); // process previous month
        int year = toProcess.getYear();
        int month = toProcess.getMonthValue();
        String jobName = "monthly_account_processing";

        // idempotency check
        Optional<JobRun> existing = jobRunRepo.findByJobNameAndPeriodYearAndPeriodMonth(jobName, year, month);
        if (existing.isPresent()) {
            log.info("Monthly job for {}-{} already recorded: status={}", year, month, existing.get().getStatus());
            return;
        }

        JobRun run = new JobRun();
        run.setJobName(jobName);
        run.setPeriodYear(year);
        run.setPeriodMonth(month);
        run.setStartedAt(LocalDateTime.now());
        run.setStatus("IN_PROGRESS");
        jobRunRepo.save(run);

        try {
            processMonth(year, month);
            run.setFinishedAt(LocalDateTime.now());
            run.setStatus("COMPLETED");
            jobRunRepo.save(run);
        } catch (Exception ex) {
            log.error("Monthly processing failed for {}-{}", year, month, ex);
            run.setFinishedAt(LocalDateTime.now());
            run.setStatus("FAILED");
            run.setDetails(ex.getMessage());
            jobRunRepo.save(run);
            // optional: alert/notify
        }
    }

    private void processMonth(int year, int month) {
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<Account> page;

        do {
            page = accountRepo.findAll(pageable);
            for (Account a : page) {
                processAccountSafely(a.getId(), year, month); // pass id + period
            }
            if (page.hasNext()) {
                pageable = page.nextPageable();
            } else break;
        } while (true);
    }

    private void processAccountSafely(Long accountId,int year,int month) {
        // wrap per-account in its own transaction to limit rollback scope
        TransactionTemplate tt = new TransactionTemplate(txManager);
        try {
            tt.executeWithoutResult(status -> {
                // re-load account within transaction and lock it
                Account account = accountRepo.findByIdForUpdate(accountId)
                        .orElseThrow(() -> new IllegalStateException("Account disappeared: " + accountId));
                try {
                    processSingleAccount(account, year, month);
                } catch (Exception ex) {
                    // Log and swallow so other accounts still processed
                    log.error("Failed to process account {} for {}-{}: {}", accountId, year, month, ex.getMessage(), ex);
                    // Optionally record failure in JobRun details or a per-account error table
                }
            });
        } catch (Exception ex) {
            // This outer try catches transaction template errors (rare)
            log.error("TransactionTemplate failed for account {}: {}", accountId, ex.getMessage(), ex);
        }
    }

    private void processSingleAccount(Account account,int year,int month) {
        BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();

        // 1) monthly interest (annualRate stored as % e.g. 3.5)
        BigDecimal annualRatePercent = account.getInterestRate() == null ? BigDecimal.ZERO : account.getInterestRate();
        BigDecimal monthlyRate = annualRatePercent.divide(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            // credit interest to account (credit from bank interest expense account or system account)
            applyCredit(account, interest, "Monthly interest for " + yearMonthString(year, month));
        }

        // 2) monthly fee
        BigDecimal monthlyFee = account.getMonthlyFee() == null ? BigDecimal.ZERO : account.getMonthlyFee();
        if (monthlyFee.compareTo(BigDecimal.ZERO) > 0) {
            applyCharge(account, monthlyFee, "Monthly fee for " + yearMonthString(year, month));
        }

        // 3) minimum-balance penalty: check if account violated min during the month
        // Here you need logic to determine if min was maintained through month; simplest check:
        BigDecimal minimumBalance = account.getMinimumBalance() == null ? BigDecimal.ZERO : account.getMinimumBalance();
        BigDecimal todayBalance = account.getBalance();
        boolean violated = todayBalance.compareTo(minimumBalance) < 0;
        if (violated) {
            BigDecimal penalty = account.getMinimumBalancePenalty() == null ? BigDecimal.ZERO : account.getMinimumBalancePenalty();
            if (penalty.compareTo(BigDecimal.ZERO) > 0) {
                applyCharge(account, penalty, "Minimum balance penalty for " + yearMonthString(year, month));
            }
        }

        if(account.isTransactionAlert()){
            BigDecimal charge = BigDecimal.valueOf(25);
            applyCharge(account, charge, "Transaction Alert charge for " + yearMonthString(year, month));
        }

        // persist account (if mutations made by applyCharge/credit already saved, maybe no-op)
    }

    private void applyCredit(Account account, BigDecimal amount, String desc) {
        BigDecimal newBal = account.getBalance().add(amount);
        account.setBalance(newBal);
        accountRepo.save(account);

        Account bankAccount = accountRepo.findByAccountNumber("000000000000").orElseThrow();


        Transaction tx = new Transaction();

        tx.setReferenceNumber(UUID.randomUUID().toString());
        tx.setType(TransactionType.Intrest);
        tx.setAmount(amount);
        tx.setToAccount(account);
        tx.setFromAccount(bankAccount); // or a system bank account if you model it
        tx.setDescription(desc);
        tx.setStatus("COMPLETED");
        tx.setExecutedAt(LocalDateTime.now());
        tx.setFromAccountBalanceAfter(null); // no from account
        tx.setToAccountBalanceAfter(newBal);
        txnRepo.save(tx);
    }

    private void applyCharge(Account account, BigDecimal amount, String desc) {
        // charge to user's account (debit user, credit bank revenue)
        BigDecimal newBal = account.getBalance().subtract(amount);
        account.setBalance(newBal);
        accountRepo.save(account);

        Account bankAccount = accountRepo.findByAccountNumber("000000000000").orElseThrow();


        Transaction tx = new Transaction();
        tx.setReferenceNumber(UUID.randomUUID().toString());
        tx.setType(TransactionType.Fee);
        tx.setAmount(amount);
        tx.setFromAccount(account);
        tx.setToAccount(bankAccount); // or system revenue account
        tx.setDescription(desc);
        tx.setStatus("COMPLETED");
        tx.setExecutedAt(LocalDateTime.now());
        tx.setFromAccountBalanceAfter(newBal);
        tx.setToAccountBalanceAfter(null);
        txnRepo.save(tx);
    }

//    private boolean checkMinimumBalanceViolation(Long accountId, int year, int month, BigDecimal minimum) {
//        if (minimum == null || minimum.compareTo(BigDecimal.ZERO) <= 0) return false;
//        // Efficient solution: maintain daily balance snapshots in separate table during day-end process.
//        YearMonth ym = YearMonth.of(year, month);
//        LocalDateTime from = ym.atDay(1).atStartOfDay();
//        LocalDateTime to = ym.atEndOfMonth().atTime(23,59,59);
//
//        // get transactions involving account sorted by executedAt ascending
//        List<Transaction> txs = txnRepo.findForAccountBetween(accountId, from, to);
//        // compute running starting from account balance before first txn in period:
//        BigDecimal running = getAccountBalanceBefore(accountId, from);
//        BigDecimal minSeen = running;
//        for (Transaction t : txs) {
//            if (t.getFromAccount() != null && t.getFromAccount().getId().equals(accountId)) {
//                running = running.subtract(t.getAmount());
//            }
//            if (t.getToAccount() != null && t.getToAccount().getId().equals(accountId)) {
//                running = running.add(t.getAmount());
//            }
//            if (running.compareTo(minSeen) < 0) minSeen = running;
//            if (minSeen.compareTo(minimum) < 0) return true; // early exit
//        }
//        return minSeen.compareTo(minimum) < 0;
//    }

//    private BigDecimal getAccountBalanceBefore(Long accountId, LocalDateTime at) {
//        // compute balance before 'at' by taking current balance and reversing transactions after `at`, OR by summing transactions before at starting from zero if you have opening balance data.
//        // Simpler method: if you store per-transaction balance snapshots, find the latest snapshot before 'at'
//        Optional<Transaction> maybe = txnRepo.findTopByAccountIdAndExecutedAtBeforeOrderByExecutedAtDesc(accountId, at);
//        if (maybe.isPresent()) {
//            Transaction t = maybe.get();
//            if (t.getToAccount() != null && t.getToAccount().getId().equals(accountId) && t.getToAccountBalanceAfter() != null) {
//                return t.getToAccountBalanceAfter();
//            }
//            if (t.getFromAccount() != null && t.getFromAccount().getId().equals(accountId) && t.getFromAccountBalanceAfter() != null) {
//                return t.getFromAccountBalanceAfter();
//            }
//        }
//        // fallback: return accountRepo.findById(accountId).map(Account::getBalance).orElse(BigDecimal.ZERO);
//        return accountRepo.findById(accountId).map(Account::getBalance).orElse(BigDecimal.ZERO);
//    }

    private String yearMonthString(int year, int month) {
        return String.format("%d-%02d", year, month);
    }
}

