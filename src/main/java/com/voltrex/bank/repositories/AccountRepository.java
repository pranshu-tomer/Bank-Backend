package com.voltrex.bank.repositories;

import com.voltrex.bank.entities.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    // Pessimistic lock to guarantee safe concurrent updates
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNumber = :acc")
    Optional<Account> findByAccountNumberForUpdate(@Param("acc") String accountNumber);

    List<Account> findAllByOwnerId(Long ownerId);
}


