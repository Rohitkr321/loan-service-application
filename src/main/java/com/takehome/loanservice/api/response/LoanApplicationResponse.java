package com.takehome.loanservice.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.takehome.loanservice.domain.ApplicationStatus;
import com.takehome.loanservice.domain.LoanDecision;
import com.takehome.loanservice.domain.RejectionReason;
import com.takehome.loanservice.domain.RiskBand;
import java.util.List;
import java.util.UUID;

/**
 * Public API response returned after an application is evaluated.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoanApplicationResponse(
		UUID applicationId,
		ApplicationStatus status,
		@JsonInclude(JsonInclude.Include.ALWAYS)
		RiskBand riskBand,
		LoanOfferResponse offer,
		List<RejectionReason> rejectionReasons) {

	public static LoanApplicationResponse from(LoanDecision decision) {
		return new LoanApplicationResponse(
				decision.applicationId(),
				decision.status(),
				decision.riskBand(),
				decision.offer() == null ? null : LoanOfferResponse.from(decision.offer()),
				decision.rejectionReasons().isEmpty() ? null : decision.rejectionReasons());
	}
}
