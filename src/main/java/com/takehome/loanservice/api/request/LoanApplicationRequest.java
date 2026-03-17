package com.takehome.loanservice.api.request;

import com.takehome.loanservice.domain.LoanApplicationCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LoanApplicationRequest(
		@NotNull(message = "applicant is required")
		@Valid
		ApplicantRequest applicant,
		@NotNull(message = "loan is required")
		@Valid
		LoanRequest loan) {

	public LoanApplicationCommand toDomain() {
		return new LoanApplicationCommand(applicant.toDomain(), loan.toDomain());
	}
}
