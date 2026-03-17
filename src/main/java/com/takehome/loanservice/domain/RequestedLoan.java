package com.takehome.loanservice.domain;

import java.math.BigDecimal;

public record RequestedLoan(
		BigDecimal amount,
		int tenureMonths,
		LoanPurpose purpose) {
}
