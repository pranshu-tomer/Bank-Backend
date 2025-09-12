package com.voltrex.bank.repositories;

import com.voltrex.bank.entities.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    Optional<CreditCard> findByUserId(Long userId);
}
