package com.voltrex.bank.repositories;

import com.voltrex.bank.entities.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accNumber") String accNumber);

    // Find primary account of a user
    Optional<Account> findByOwnerIdAndPrimaryAccountTrue(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.owner.id = :ownerId AND a.primaryAccount = true")
    Optional<Account> findPrimaryByOwnerIdForUpdate(@Param("ownerId") Long ownerId);

//    List<Account> findAllByOwnerId(Long ownerId);
    List<Account> findByOwnerId(Long ownerId);
}





