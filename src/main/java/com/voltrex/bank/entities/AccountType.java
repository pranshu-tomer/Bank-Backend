package com.voltrex.bank.entities;

import java.math.BigDecimal;

/**
 * Fixed account types with immutable plan values encoded here.
 * Values are examples — replace with your bank's actual values.
 *
 * NOTE: these values are constants in code. If you ever need them configurable at runtime,
 * switch to a DB-backed AccountPlan entity.
 */
public enum AccountType {

    SAVINGS(
            "Savings Account",
            // openingCharge
            new BigDecimal("0.00"),
            // annual interest rate (percent)
            new BigDecimal("3.50"),
            // monthlyFee
            new BigDecimal("0.00"),
            // minimumBalance
            new BigDecimal("500.00"),
            // minimumBalancePenalty
            new BigDecimal("50.00"),
            // maxDailyWithdrawal
            new BigDecimal("5000.00"),
            // maxDailyDeposit
            new BigDecimal("100000.00")
    ),

    SALARY(
            "Salary Account",
            new BigDecimal("0.00"),      // openingCharge (often zero)
            new BigDecimal("1.50"),      // interestRate (salary accounts may have low/no interest)
            new BigDecimal("500.00"),      // monthlyFee
            new BigDecimal("0.00"),      // minimumBalance (salary accounts typically have none)
            new BigDecimal("0.00"),      // minimumBalancePenalty
            new BigDecimal("20000.00"),  // maxDailyWithdrawal
            new BigDecimal("200000.00")
    ),

    CURRENT(
            "Current Account",
            new BigDecimal("500.00"),    // openingCharge
            new BigDecimal("0.00"),      // interestRate (usually 0 or very low)
            new BigDecimal("500.00"),    // monthlyFee
            new BigDecimal("5000.00"),   // minimumBalance
            new BigDecimal("250.00"),    // minimumBalancePenalty
            new BigDecimal("100000.00"), // maxDailyWithdrawal
            new BigDecimal("2000000.00")
    );

    private final String displayName;
    private final BigDecimal openingCharge;
    private final BigDecimal interestRate;
    private final BigDecimal monthlyFee;
    private final BigDecimal minimumBalance;
    private final BigDecimal minimumBalancePenalty;
    private final BigDecimal maxDailyWithdrawal;
    private final BigDecimal maxDailyDeposit;

    AccountType(
            String displayName,
            BigDecimal openingCharge,
            BigDecimal interestRate,
            BigDecimal monthlyFee,
            BigDecimal minimumBalance,
            BigDecimal minimumBalancePenalty,
            BigDecimal maxDailyWithdrawal,
            BigDecimal maxDailyDeposit
    ) {
        this.displayName = displayName;
        this.openingCharge = openingCharge;
        this.interestRate = interestRate;
        this.monthlyFee = monthlyFee;
        this.minimumBalance = minimumBalance;
        this.minimumBalancePenalty = minimumBalancePenalty;
        this.maxDailyWithdrawal = maxDailyWithdrawal;
        this.maxDailyDeposit = maxDailyDeposit;
    }

    public String getDisplayName() { return displayName; }
    public BigDecimal getOpeningCharge() { return openingCharge; }
    public BigDecimal getInterestRate() { return interestRate; }
    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public BigDecimal getMinimumBalancePenalty() { return minimumBalancePenalty; }
    public BigDecimal getMaxDailyWithdrawal() { return maxDailyWithdrawal; }
    public BigDecimal getMaxDailyDeposit() { return maxDailyDeposit; }
}
