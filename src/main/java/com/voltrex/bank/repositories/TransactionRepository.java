package com.voltrex.bank.repositories;

import com.voltrex.bank.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // sum withdrawn (fromAccount) between given timestamps
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromAccount.id = :accountId AND t.executedAt BETWEEN :from AND :to")
    BigDecimal sumWithdrawnBetween(@Param("accountId") Long accountId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    // sum deposited (toAccount) between given timestamps
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toAccount.id = :accountId AND t.executedAt BETWEEN :from AND :to")
    BigDecimal sumDepositedBetween(@Param("accountId") Long accountId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    @Query("""
        SELECT t FROM Transaction t
        WHERE (t.fromAccount.owner.id = :userId OR t.toAccount.owner.id = :userId)
          AND t.executedAt BETWEEN :from AND :to
        ORDER BY t.executedAt DESC
        """)
    Page<Transaction> findForUserBetween(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
