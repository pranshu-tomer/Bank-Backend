package com.voltrex.bank.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * User / Customer entity.
 *
 * Business rules:
 *  - sketch mentions "Max 3 accounts + 1 credit card" — enforce in service layer during create.
 */
@Entity
@Table(name = "bank_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer reference number (CRN) — business identifier, unique.
     */
    @Column(unique = true)
    private String crn;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true,nullable = false)
    private String phone;

    @Embedded
    private Address address;

    /**
     * Store a password hash (never plain text). Hashing happens in service layer.
     */
    private String password;

    private LocalDate dob;

    /**
     * Age may be stored redundantly or computed; kept here since sketch included it.
     */
    private Integer age;

    private Gender gender;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private boolean twoFactorEnabled = false;

    private boolean loginAlert = false;

    private Status status = Status.PENDING;

    /**
     * User -> Accounts: one user may have multiple accounts (business limit to be enforced in services).
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    /**
     * Optional one-to-one credit card (sketch: max 1 credit card per user). Mapped on CreditCard side.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CreditCard creditCard;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.crn;
    }
}
