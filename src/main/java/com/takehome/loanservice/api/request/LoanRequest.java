package com.takehome.loanservice.api.request;

import com.takehome.loanservice.domain.LoanPurpose;
import com.takehome.loanservice.domain.RequestedLoan;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record LoanRequest(
		@NotNull(message = "amount is required")
		@DecimalMin(value = "10000.00", message = "amount must be between 10000 and 5000000")
		@DecimalMax(value = "5000000.00", message = "amount must be between 10000 and 5000000")
		BigDecimal amount,
		@Min(value = 6, message = "tenureMonths must be between 6 and 360")
		@Max(value = 360, message = "tenureMonths must be between 6 and 360")
		int tenureMonths,
		@NotNull(message = "purpose is required")
		LoanPurpose purpose) {

	public RequestedLoan toDomain() {
		return new RequestedLoan(
				amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP),
				tenureMonths,
				purpose);
	}
}
