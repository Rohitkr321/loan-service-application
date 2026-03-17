package com.takehome.loanservice.domain;

import java.math.BigDecimal;

public record LoanOffer(
		BigDecimal interestRate,
		int tenureMonths,
		BigDecimal emi,
		BigDecimal totalPayable) {
}
