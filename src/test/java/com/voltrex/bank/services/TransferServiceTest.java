package com.voltrex.bank.services;

import com.voltrex.bank.dto.TransferByAccountRequest;
import com.voltrex.bank.dto.TransferByReceiverRequest;
import com.voltrex.bank.entities.*;
import com.voltrex.bank.entities.AccountType;
import com.voltrex.bank.exception.*;
import com.voltrex.bank.repositories.AccountRepository;
import com.voltrex.bank.repositories.TransactionRepository;
import com.voltrex.bank.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private TransactionRepository txnRepo;

    private User alice;
    private User bob;
    private Account aliceAcc;
    private Account bobAcc;

    @BeforeEach
    void setUp() {
        txnRepo.deleteAll();
        accountRepo.deleteAll();
        userRepo.deleteAll();

        alice = User.builder()
                .firstName("Alice").lastName("Wonder")
                .email("alice@test.com").crn("CRN1")
                .build();
        bob = User.builder()
                .firstName("Bob").lastName("Marley")
                .email("bob@test.com").crn("CRN2")
                .build();

        alice = userRepo.save(alice);
        bob = userRepo.save(bob);

        aliceAcc = Account.builder()
                .accountNumber("ACC1001")
                .balance(new BigDecimal("1000.00"))
                .owner(alice)
                .type(AccountType.SAVINGS)
                .primaryAccount(true)
                .build();
        bobAcc = Account.builder()
                .accountNumber("ACC2001")
                .balance(new BigDecimal("500.00"))
                .owner(bob)
                .type(AccountType.SAVINGS)
                .primaryAccount(true)
                .build();

        accountRepo.saveAll(List.of(aliceAcc, bobAcc));
    }

    @Test
    void whenValidTransferByAccount_thenBalancesAndTransactionOk() {
        TransferByAccountRequest req = new TransferByAccountRequest();
        req.setFromAccountNumber(aliceAcc.getAccountNumber());
        req.setToAccountNumber(bobAcc.getAccountNumber());
        req.setToAccountName("Bob Marley");
        req.setAmount(new BigDecimal("200.00"));
        req.setDescription("Gift");
        req.setType(TransactionType.Transfer);

        String ref = transferService.transferByAccount(req, alice);

        Account updatedAlice = accountRepo.findById(aliceAcc.getId()).orElseThrow();
        Account updatedBob = accountRepo.findById(bobAcc.getId()).orElseThrow();

        assertThat(updatedAlice.getBalance()).isEqualByComparingTo("800.00");
        assertThat(updatedBob.getBalance()).isEqualByComparingTo("700.00");

        Transaction tx = txnRepo.findAll().get(0);
        assertThat(tx.getReferenceNumber()).isEqualTo(ref);
        assertThat(tx.getDescription()).isEqualTo("Gift");
        assertThat(tx.getType()).isEqualTo(TransactionType.Transfer);
    }

    @Test
    void whenNotOwner_thenThrows() {
        TransferByAccountRequest req = new TransferByAccountRequest();
        req.setFromAccountNumber(aliceAcc.getAccountNumber());
        req.setToAccountNumber(bobAcc.getAccountNumber());
        req.setToAccountName("Bob Marley");
        req.setAmount(new BigDecimal("100.00"));
        req.setDescription("Hack");
        req.setType(TransactionType.Transfer);

        assertThatThrownBy(() -> transferService.transferByAccount(req, bob))
                .isInstanceOf(NotOwnerException.class);
    }

    @Test
    void whenInsufficientFunds_thenThrowsAndBalancesUnchanged() {
        TransferByAccountRequest req = new TransferByAccountRequest();
        req.setFromAccountNumber(aliceAcc.getAccountNumber());
        req.setToAccountNumber(bobAcc.getAccountNumber());
        req.setToAccountName("Bob Marley");
        req.setAmount(new BigDecimal("5000.00"));
        req.setDescription("Too much");
        req.setType(TransactionType.Transfer);

        assertThatThrownBy(() -> transferService.transferByAccount(req, alice))
                .isInstanceOf(InsufficientFundsException.class);

        // balances unchanged (ACID rollback)
        assertThat(accountRepo.findById(aliceAcc.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("1000.00");
        assertThat(accountRepo.findById(bobAcc.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("500.00");
    }

    @Test
    void whenReceiverNameMismatch_thenThrows() {
        TransferByAccountRequest req = new TransferByAccountRequest();
        req.setFromAccountNumber(aliceAcc.getAccountNumber());
        req.setToAccountNumber(bobAcc.getAccountNumber());
        req.setToAccountName("Wrong Name");
        req.setAmount(new BigDecimal("100.00"));
        req.setDescription("Gift");
        req.setType(TransactionType.Transfer);

        assertThatThrownBy(() -> transferService.transferByAccount(req, alice))
                .isInstanceOf(TransferException.class)
                .hasMessageContaining("Receiver details are incorrect");
    }

    @Test
    void whenDailyLimitExceeded_thenThrows() {
        // Alice has SAVINGS account with maxDailyWithdrawal = 5000
        // Insert a transaction today that already used up 4900
        Transaction existing = Transaction.builder()
                .fromAccount(aliceAcc)
                .toAccount(bobAcc)
                .amount(new BigDecimal("4900.00"))
                .status("COMPLETED")
                .type(TransactionType.Transfer)
                .build();
        txnRepo.save(existing);

        TransferByAccountRequest req = new TransferByAccountRequest();
        req.setFromAccountNumber(aliceAcc.getAccountNumber());
        req.setToAccountNumber(bobAcc.getAccountNumber());
        req.setToAccountName("Bob Marley");
        req.setAmount(new BigDecimal("200.00")); // this will exceed 5000
        req.setDescription("Test");
        req.setType(TransactionType.Transfer);

        assertThatThrownBy(() -> transferService.transferByAccount(req, alice))
                .isInstanceOf(LimitExceededException.class)
                .hasMessageContaining("Daily withdrawal limit exceeded");
    }


    @Test
    void whenValidTransferByReceiver_thenBalancesOk() {
        TransferByReceiverRequest req = new TransferByReceiverRequest();
        req.setFromAccountNumber(aliceAcc.getAccountNumber());
        req.setReceiverIdentifier("bob@test.com"); // email lookup
        req.setReceiverName("Bob Marley");
        req.setAmount(new BigDecimal("150.00"));
        req.setDescription("Salary");
        req.setType(TransactionType.Transfer);

        String ref = transferService.transferByReceiver(req, alice);

        Account updatedAlice = accountRepo.findById(aliceAcc.getId()).orElseThrow();
        Account updatedBob = accountRepo.findById(bobAcc.getId()).orElseThrow();

        assertThat(updatedAlice.getBalance()).isEqualByComparingTo("850.00");
        assertThat(updatedBob.getBalance()).isEqualByComparingTo("650.00");

        Transaction tx = txnRepo.findAll().get(0);
        assertThat(tx.getReferenceNumber()).isEqualTo(ref);
        assertThat(tx.getDescription()).isEqualTo("Salary");
        assertThat(tx.getType()).isEqualTo(TransactionType.Transfer);
    }
}


