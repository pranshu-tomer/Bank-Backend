package com.voltrex.bank.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Credit card owned by a user. Sketch specified "Max 1 credit card" per user — modeled as OneToOne with User.
 * Note: cardNumber is stored here (ideally must be tokenized/encrypted in production).
 */
@Entity
@Table(name = "credit_card")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCard {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 32)
    private String cardNumber; // consider tokenization/encryption in prod

    @CreationTimestamp
    private LocalDateTime issuedAt;

    private boolean active = true;

    /**
     * Credit limit
     */
    @DecimalMin("0.00")
    private BigDecimal creditLimit;

    private BigDecimal creditUsed;

    /**
     * Optional expiry date
     */
    private LocalDate expiryDate;

    /**
     * Link back to owning user. A user may have 0..1 credit card (enforce on service layer).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
