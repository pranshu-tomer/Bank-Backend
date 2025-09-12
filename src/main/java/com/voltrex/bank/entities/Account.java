package com.voltrex.bank.entities;

import com.voltrex.bank.entities.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Account entity. Uses the fixed AccountType enum for fee & limit values.
 * Business rule: each User may create up to three accounts — one of each type (enforce in service layer).
 */
@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Business-visible account number (unique).
     */
    @Column(unique = true, nullable = false)
    private String accountNumber;

    @DecimalMin("0.00")
    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @CreationTimestamp
    private LocalDateTime openedAt;

    private boolean primaryAccount;

    private boolean active = true;

    private LocalDateTime closedAt;

    private boolean transactionAlert;

    // Owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Transactions where this account is the sender
    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Transaction> outgoingTransactions = new ArrayList<>();

    // Transactions where this account is the receiver
    @OneToMany(mappedBy = "toAccount", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Transaction> incomingTransactions = new ArrayList<>();

    /**
     * Convenience helper to get opening charge from the enum (non-persistent).
     */
    public BigDecimal getOpeningCharge() {
        return type.getOpeningCharge();
    }

    public BigDecimal getInterestRate() {
        return type.getInterestRate();
    }

    public BigDecimal getMonthlyFee() {
        return type.getMonthlyFee();
    }

    public BigDecimal getMinimumBalance() {
        return type.getMinimumBalance();
    }

    public BigDecimal getMinimumBalancePenalty() {
        return type.getMinimumBalancePenalty();
    }

    public BigDecimal getMaxDailyWithdrawal() {
        return type.getMaxDailyWithdrawal();
    }

    public BigDecimal getMaxDailyDeposit() {
        return type.getMaxDailyDeposit();
    }
}
