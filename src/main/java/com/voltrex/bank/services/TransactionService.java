package com.voltrex.bank.services;

import com.voltrex.bank.dto.TransactionResponse;
import com.voltrex.bank.entities.Transaction;
import com.voltrex.bank.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// imports omitted for brevity
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository txnRepo;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsForUser(long userId,
                                                            LocalDateTime from,
                                                            LocalDateTime to,
                                                            int page,
                                                            int size,
                                                            String sortBy,
                                                            Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Transaction> pageTx = txnRepo.findForUserBetween(userId, from, to, pageable);

        List<TransactionResponse> content = pageTx.stream()
                .map(t -> toDto(t, userId))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, pageTx.getTotalElements());
    }

    private TransactionResponse toDto(Transaction t, long currentUserId) {
        TransactionResponse r = new TransactionResponse();
        r.setId(t.getId());
        r.setReferenceNumber(t.getReferenceNumber());
        r.setType(t.getType());
        r.setAmount(t.getAmount());
        r.setDescription(t.getDescription());
        r.setStatus(t.getStatus());
        r.setExecutedAt(t.getExecutedAt());

        // account numbers
        r.setFromAccountNumber(t.getFromAccount() != null ? t.getFromAccount().getAccountNumber() : null);
        r.setToAccountNumber(t.getToAccount() != null ? t.getToAccount().getAccountNumber() : null);

        // sender / receiver names
        r.setSenderName(ownerFullNameSafe(t.getFromAccount() != null ? t.getFromAccount().getOwner() : null));
        r.setReceiverName(ownerFullNameSafe(t.getToAccount() != null ? t.getToAccount().getOwner() : null));

        r.setFromAccountBalanceAfter(t.getFromAccountBalanceAfter());
        r.setToAccountBalanceAfter(t.getToAccountBalanceAfter());

        // direction logic from current user's perspective
        boolean isFromOwnedByUser = t.getFromAccount() != null &&
                t.getFromAccount().getOwner() != null &&
                t.getFromAccount().getOwner().getId() != null &&
                t.getFromAccount().getOwner().getId().equals(currentUserId);

        boolean isToOwnedByUser = t.getToAccount() != null &&
                t.getToAccount().getOwner() != null &&
                t.getToAccount().getOwner().getId() != null &&
                t.getToAccount().getOwner().getId().equals(currentUserId);

        if (isFromOwnedByUser && isToOwnedByUser) {
            r.setDirection("INTERNAL"); // transfer between user's own accounts
        } else if (isFromOwnedByUser) {
            r.setDirection("DEBIT"); // money left user's account
        } else if (isToOwnedByUser) {
            r.setDirection("CREDIT"); // money entered user's account
        } else {
            // Shouldn't happen because query filters by userId, but fallback:
            r.setDirection("UNKNOWN");
        }

        // optional balances after (if stored on Transaction)
        // r.setFromAccountBalanceAfter(t.getFromAccountBalanceAfter());
        // r.setToAccountBalanceAfter(t.getToAccountBalanceAfter());

        return r;
    }

    private String ownerFullNameSafe(com.voltrex.bank.entities.User u) {
        if (u == null) return null;
        String fn = u.getFirstName() == null ? "" : u.getFirstName().trim();
        String ln = u.getLastName() == null ? "" : u.getLastName().trim();
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? null : full;
    }
}
