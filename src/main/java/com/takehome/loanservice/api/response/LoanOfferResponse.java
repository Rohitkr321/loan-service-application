package com.takehome.loanservice.api.response;

import com.takehome.loanservice.domain.LoanOffer;
import java.math.BigDecimal;

public record LoanOfferResponse(
		BigDecimal interestRate,
		int tenureMonths,
		BigDecimal emi,
		BigDecimal totalPayable) {

	public static LoanOfferResponse from(LoanOffer offer) {
		return new LoanOfferResponse(
				offer.interestRate(),
				offer.tenureMonths(),
				offer.emi(),
				offer.totalPayable());
	}
}
