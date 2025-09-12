package com.voltrex.bank.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Transaction record (date/time, reference number, belongs to (account), type, amount).
 */
@Entity
@Table(name = "transaction_record") // transaction is a reserved word in some DBs
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Business-level reference number for the transaction (unique).
     */
    @Column(unique = true, nullable = false)
    private String referenceNumber;

    @CreationTimestamp
    private LocalDateTime executedAt;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @DecimalMin("0.00")
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * Which account this transaction belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private String description;

    /**
     * Example: bank charge or external status; if you want a strict status enum add it.
     */
    private String status;
}
